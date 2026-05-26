package com.kotliners.adoptaPerrito.adapters

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod

/**
 * Adaptador para consultar informacion de razas desde APIs externas
 * (The Dog API / The Cat API) y traducir los resultados al espanol
 * usando MyMemory API.
 *
 * Al estar en el backend, no hay restricciones de CORS.
 */
@Service
class RazaInfoAdapter {

    private val logger = LoggerFactory.getLogger(RazaInfoAdapter::class.java)
    private val restTemplate = RestTemplate().apply {
        val factory = org.springframework.http.client.SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(4000)
        factory.setReadTimeout(8000)  // 8s para dar tiempo a MyMemory
        requestFactory = factory
    }

    /** Cache de resultados con traduccion exitosa */
    private val cache = java.util.concurrent.ConcurrentHashMap<String, RazaInfoResult>()
    /** Cache de resultados sin traducir — se reintenta en la proxima llamada */
    private val cachePendiente = java.util.concurrent.ConcurrentHashMap<String, RazaInfoResult>()
    /** Cache de razas no encontradas — no reintentar */
    private val cacheNoEncontrado = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

    @Value("\${razainfo.dog-api-key:}")
    lateinit var dogApiKey: String

    @Value("\${razainfo.cat-api-key:}")
    lateinit var catApiKey: String

    companion object {
        private const val DOG_API = "https://api.thedogapi.com/v1"
        private const val CAT_API = "https://api.thecatapi.com/v1"
        private const val TRANSLATE_API = "https://translate.googleapis.com/translate_a/single"
        private const val MAX_QUERY_CHARS = 450  // MyMemory limite 500, dejamos margen

        /** Campos a ignorar completamente */
        private val CAMPOS_IGNORAR = setOf(
            "id", "species_id", "reference_image_id", "country_codes", "country_code",
            "image", "name", "wikipedia_url",
            // Campos tecnicos sin valor para el usuario
            "experimental", "rex", "suppressed_tail",
            "alt_names", "cfa_url", "vetstreet_url", "vcahospitals_url"
        )

        /**
         * Campos numericos en escala 1-5 — se muestran con estrellas.
         */
        private val CAMPOS_SCORE = setOf(
            "adaptability", "affection_level", "child_friendly", "dog_friendly",
            "energy_level", "grooming", "health_issues", "intelligence",
            "shedding_level", "social_needs", "stranger_friendly", "vocalisation"
        )

        /**
         * Campos booleanos (0/1) — se muestran con like/dislike.
         */
        private val CAMPOS_BOOL = setOf(
            "indoor", "lap", "natural", "rare", "hairless", "short_legs", "hypoallergenic"
        )

        /** Etiquetas en espanol para campos conocidos */
        private val ETIQUETAS = mapOf(
            "life_span"         to "Esperanza de vida",
            "temperament"       to "Temperamento",
            "origin"            to "Origen",
            "bred_for"          to "Criado para",
            "perfect_for"       to "Ideal para",
            "breed_group"       to "Grupo",
            "history"           to "Historia",
            "description"       to "Descripcion",
            "weight"            to "Peso promedio",
            "height"            to "Altura promedio",
            "energy_level"      to "Nivel de energia",
            "adaptability"      to "Adaptabilidad",
            "affection_level"   to "Nivel de afecto",
            "child_friendly"    to "Amigable con ninos",
            "dog_friendly"      to "Amigable con perros",
            "stranger_friendly" to "Amigable con extraños",
            "grooming"          to "Necesidad de aseo",
            "intelligence"      to "Inteligencia",
            "shedding_level"    to "Nivel de muda",
            "social_needs"      to "Necesidades sociales",
            "vocalisation"      to "Vocalizacion",
            "health_issues"     to "Problemas de salud",
            "hypoallergenic"    to "Hipoalergenico",
            "country_of_origin" to "Pais de origen",
            "indoor"            to "Apto para interior",
            "lap"               to "Gato de regazo",
            "natural"           to "Natural",
            "rare"              to "Raro",
            "hairless"          to "Sin pelo",
            "short_legs"        to "Patas cortas"
        )
    }

    /**
     * Busca informacion de una raza dado el nombre (en ingles) y la especie.
     * Traduce solo los campos de texto al espanol.
     * Los campos numericos se muestran directamente.
     */
    fun buscarRaza(raza: String, especie: String): RazaInfoResult? {
        val esPerro = especie.uppercase().contains("PERRO") || especie.uppercase().contains("DOG")
        val baseUrl = if (esPerro) DOG_API else CAT_API
        val apiKey  = if (esPerro) dogApiKey else catApiKey

        val cacheKey = "${especie.uppercase()}:${raza.lowercase().trim()}"

        // 1. Resultado con traduccion exitosa — devolver directo
        cache[cacheKey]?.let {
            logger.info("Cache hit (traducido) para '$raza'")
            return it
        }

        // 2. Raza no encontrada — no reintentar
        if (cacheNoEncontrado.containsKey(cacheKey)) {
            logger.info("Cache hit (no encontrado) para '$raza'")
            return null
        }

        // 3. Resultado pendiente de traduccion — reintentar traduccion
        val pendiente = cachePendiente[cacheKey]
        if (pendiente != null) {
            logger.info("Reintentando traduccion para '$raza'")
            val camposTraducidos = traducirEnChunks(
                pendiente.campos.filter { it.tipo == "TEXT" }.map { it.etiqueta to it.valor }
            )
            if (camposTraducidos.any { it.valor != pendiente.campos.find { c -> c.etiqueta == it.etiqueta }?.valor }) {
                // Traduccion exitosa — mover a cache definitivo
                val resultado = pendiente.copy(campos = camposTraducidos + pendiente.campos.filter { it.tipo != "TEXT" })
                cache[cacheKey] = resultado
                cachePendiente.remove(cacheKey)
                logger.info("Traduccion exitosa en reintento para '$raza'")
                return resultado
            }
            // Sigue sin traducir — devolver pendiente igual
            return pendiente
        }

        logger.info("Buscando raza: '$raza'")
        val resultados = buscarEnApi(baseUrl, raza.trim(), apiKey) ?: run {
            logger.info("API no respondio para '$raza' — no cachear, puede ser error de red")
            return null  // No guardar en cacheNoEncontrado — puede ser error temporal
        }
        if (resultados.isEmpty()) {
            logger.info("Raza no encontrada en API: $raza")
            cacheNoEncontrado[cacheKey] = true; return null
        }
        logger.info("Raza encontrada: ${resultados[0]["name"]} — traduciendo campos...")
        val r = resultados[0]

        val imagenUrl = obtenerImagen(r, baseUrl, apiKey)

        // Extraer wikipedia_url si existe
        val wikipediaUrl = r["wikipedia_url"]?.toString()

        // Separar campos de texto (a traducir), score (1-5) y bool (0/1)
        val camposTexto    = mutableListOf<Pair<String, String>>()
        val camposDirectos = mutableListOf<RazaCampo>()

        for ((key, value) in r) {
            if (key in CAMPOS_IGNORAR) continue
            val valorBase = valorAString(value) ?: continue

            // Agregar unidades segun el campo
            val valorStr = when (key) {
                "life_span" -> "$valorBase anos"
                else        -> valorBase
            }

            when {
                key in CAMPOS_SCORE          -> camposDirectos.add(RazaCampo(ETIQUETAS[key] ?: key, valorStr, "SCORE"))
                key in CAMPOS_BOOL           -> camposDirectos.add(RazaCampo(ETIQUETAS[key] ?: key, valorStr, "BOOL"))
                key == "life_span"           -> camposDirectos.add(RazaCampo(ETIQUETAS[key] ?: key, valorStr, "TEXT"))
                else                         -> camposTexto.add(key to valorStr)
            }
        }

        val camposTraducidos = traducirEnChunks(camposTexto)
        val campos = camposTraducidos + camposDirectos

        val resultado = RazaInfoResult(
            nombre       = r["name"]?.toString() ?: raza,
            imagenUrl    = imagenUrl,
            wikipediaUrl = wikipediaUrl,
            campos       = campos
        )

        // Si todos los campos de texto siguen en ingles, guardar como pendiente para reintento
        val hayTextoSinTraducir = camposTexto.isNotEmpty() &&
            campos.filter { it.tipo == "TEXT" }.any { campo ->
                camposTexto.any { (_, valorOriginal) -> campo.valor == valorOriginal }
            }

        if (hayTextoSinTraducir) {
            logger.warn("Traduccion incompleta para '$raza' — guardando como pendiente")
            cachePendiente[cacheKey] = resultado
        } else {
            logger.info("Traduccion exitosa para '$raza'")
            cache[cacheKey] = resultado
        }
        return resultado
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /**
     * Traduce una lista de pares (key, valor) dividiendo en chunks
     * para no superar el limite de MyMemory (500 chars).
     */
    private fun traducirEnChunks(entradas: List<Pair<String, String>>): List<RazaCampo> {
        if (entradas.isEmpty()) return emptyList()

        val separador = " §§§ "  // separador poco comun, no se URL-encodea
        val resultado = mutableListOf<RazaCampo>()
        val chunk = mutableListOf<Pair<String, String>>()
        var chunkLen = 0

        fun flushChunk() {
            if (chunk.isEmpty()) return
            val textoJunto = chunk.joinToString(separador) { it.second }
            val traducidoRaw = traducir(textoJunto, "en", "es") ?: textoJunto
            // MyMemory a veces devuelve el separador URL-encoded — decodificar
            val traducido = java.net.URLDecoder.decode(traducidoRaw, "UTF-8")
            val partes = traducido.split(separador)
            chunk.forEachIndexed { i, (key, valorRaw) ->
                resultado.add(RazaCampo(
                    etiqueta = ETIQUETAS[key] ?: key,
                    valor    = partes.getOrNull(i)?.trim() ?: valorRaw,
                    tipo     = "TEXT"
                ))
            }
            chunk.clear()
            chunkLen = 0
        }

        for ((key, valor) in entradas) {
            val addLen = valor.length + separador.length
            if (chunkLen + addLen > MAX_QUERY_CHARS && chunk.isNotEmpty()) {
                flushChunk()
            }
            chunk.add(key to valor)
            chunkLen += addLen
        }
        flushChunk()

        return resultado
    }

    @Suppress("UNCHECKED_CAST")
    private fun buscarEnApi(baseUrl: String, raza: String, apiKey: String): List<Map<String, Any?>>? {
        return try {
            val headers = HttpHeaders().apply { set("x-api-key", apiKey) }
            val entity  = HttpEntity<Void>(headers)
            val url     = "$baseUrl/breeds/search?q=${encode(raza)}"
            val res     = restTemplate.exchange(url, HttpMethod.GET, entity, List::class.java)
            res.body as? List<Map<String, Any?>>
        } catch (e: HttpClientErrorException) {
            logger.warn("API razas error ${e.statusCode} para '$raza'")
            null
        } catch (e: Exception) {
            logger.warn("Error consultando API de razas: ${e.message}")
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun obtenerImagen(r: Map<String, Any?>, baseUrl: String, apiKey: String): String? {
        (r["image"] as? Map<String, Any?>)?.get("url")?.toString()?.let { return it }
        val refId = r["reference_image_id"]?.toString() ?: return null
        return try {
            val headers = HttpHeaders().apply { set("x-api-key", apiKey) }
            val entity  = HttpEntity<Void>(headers)
            val res     = restTemplate.exchange("$baseUrl/images/$refId", HttpMethod.GET, entity, Map::class.java)
            (res.body as? Map<String, Any?>)?.get("url")?.toString()
        } catch (e: Exception) { null }
    }

    @Suppress("UNCHECKED_CAST")
    private fun traducir(texto: String, de: String, a: String): String? {
        return try {
            // Google Translate API no oficial (gtx) — rapida, sin key, sin limite estricto
            val url = "$TRANSLATE_API?client=gtx&sl=$de&tl=$a&dt=t&q=${encode(texto)}"
            val res = restTemplate.getForObject(url, List::class.java) as? List<*>
            // Respuesta: [[["traduccion","original",null,null,1],...],...]
            val partes = (res?.get(0) as? List<*>)
                ?.filterIsInstance<List<*>>()
                ?.mapNotNull { it.firstOrNull()?.toString() }
                ?: return null
            partes.joinToString("")
        } catch (e: Exception) {
            logger.warn("Error traduciendo: ${e.message}")
            null
        }
    }

    private fun valorAString(value: Any?): String? {
        if (value == null) return null
        return when (value) {
            is Boolean -> if (value) "Si" else "No"
            is Number  -> value.toString()
            is String  -> value.trim().ifEmpty { null }
            is Map<*, *> -> value["metric"]?.toString()?.let { "$it kg" }
            else -> null
        }
    }

    private fun encode(text: String) = java.net.URLEncoder.encode(text, "UTF-8")
}

data class RazaInfoResult(val nombre: String, val imagenUrl: String?, val wikipediaUrl: String?, val campos: List<RazaCampo>)
data class RazaCampo(val etiqueta: String, val valor: String, val tipo: String = "TEXT")
