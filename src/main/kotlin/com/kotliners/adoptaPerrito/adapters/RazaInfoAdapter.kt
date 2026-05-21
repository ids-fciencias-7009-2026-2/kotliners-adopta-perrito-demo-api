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
 * usando ftapi.pythonanywhere.com.
 *
 * Al estar en el backend, no hay restricciones de CORS.
 * Usa RestTemplate (incluido en Spring Boot) para las llamadas HTTP.
 */
@Service
class RazaInfoAdapter {

    private val logger = LoggerFactory.getLogger(RazaInfoAdapter::class.java)
    private val restTemplate = RestTemplate()

    // Cache en memoria: "PERRO:labrador" → resultado (o null si no se encontro)
    private val cache = java.util.concurrent.ConcurrentHashMap<String, RazaInfoResult?>()

    @Value("\${razainfo.dog-api-key:}")
    lateinit var dogApiKey: String

    @Value("\${razainfo.cat-api-key:}")
    lateinit var catApiKey: String

    companion object {
        private const val DOG_API = "https://api.thedogapi.com/v1"
        private const val CAT_API = "https://api.thecatapi.com/v1"
        private const val TRANSLATE_API = "https://api.mymemory.translated.net/get"

        /** Campos a ignorar de la respuesta */
        private val CAMPOS_IGNORAR = setOf(
            "id", "species_id", "reference_image_id", "country_codes",
            "country_code", "image", "name"
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
            "hypoallergenic"    to "Hipoalergenico",
            "alt_names"         to "Nombres alternativos",
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
     * Busca informacion de una raza dado el nombre y la especie.
     * Busca directamente con el nombre original (las razas se guardan en ingles).
     * Traduce solo los resultados al espanol.
     */
    fun buscarRaza(raza: String, especie: String): RazaInfoResult? {
        val esPerro = especie.uppercase().contains("PERRO") || especie.uppercase().contains("DOG")
        val baseUrl = if (esPerro) DOG_API else CAT_API
        val apiKey  = if (esPerro) dogApiKey else catApiKey

        // Revisar cache primero
        val cacheKey = "${especie.uppercase()}:${raza.lowercase().trim()}"
        if (cache.containsKey(cacheKey)) {
            logger.info("Cache hit para '$raza'")
            return cache[cacheKey]
        }

        // Buscar directamente con el nombre original (las razas ya estan en ingles)
        logger.info("Buscando raza: '$raza'")
        val resultados = buscarEnApi(baseUrl, raza.trim(), apiKey) ?: run {
            cache[cacheKey] = null
            return null
        }
        if (resultados.isEmpty()) {
            logger.info("Raza no encontrada: $raza")
            cache[cacheKey] = null
            return null
        }
        val r = resultados[0]

        // Obtener imagen
        val imagenUrl = obtenerImagen(r, baseUrl, apiKey)

        // Recopilar campos no nulos como strings
        val entradas = mutableListOf<Pair<String, String>>()
        for ((key, value) in r) {
            if (key in CAMPOS_IGNORAR) continue
            val valorStr = valorAString(value) ?: continue
            entradas.add(key to valorStr)
        }

        // Traducir todos los valores en una sola llamada
        val separador = " ||| "
        val textoJunto = entradas.joinToString(separador) { it.second }
        val textoTraducido = traducir(textoJunto, "en", "es") ?: textoJunto
        val valoresTraducidos = textoTraducido.split(separador)

        val campos = entradas.mapIndexed { i, (key, valorRaw) ->
            RazaCampo(
                etiqueta = ETIQUETAS[key] ?: key,
                valor    = valoresTraducidos.getOrNull(i)?.trim() ?: valorRaw
            )
        }

        return RazaInfoResult(
            nombre    = r["name"]?.toString() ?: raza,
            imagenUrl = imagenUrl,
            campos    = campos
        ).also { cache[cacheKey] = it }
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

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
        // Algunos registros traen image.url directo
        (r["image"] as? Map<String, Any?>)?.get("url")?.toString()?.let { return it }

        // Otros traen reference_image_id
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
            // MyMemory: GET /get?q=texto&langpair=en|es
            val url = "$TRANSLATE_API?q=${encode(texto)}&langpair=$de|$a"
            val res = restTemplate.getForObject(url, Map::class.java) as? Map<String, Any?>
            val responseData = res?.get("responseData") as? Map<String, Any?>
            responseData?.get("translatedText")?.toString()
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
            is Map<*, *> -> {
                // Objetos como weight/height: { metric: "...", imperial: "..." }
                value["metric"]?.toString()
            }
            else -> null
        }
    }

    private fun encode(text: String) = java.net.URLEncoder.encode(text, "UTF-8")
}

/** Resultado de busqueda de raza */
data class RazaInfoResult(
    val nombre: String,
    val imagenUrl: String?,
    val campos: List<RazaCampo>
)

/** Par etiqueta-valor de un campo de raza */
data class RazaCampo(
    val etiqueta: String,
    val valor: String
)
