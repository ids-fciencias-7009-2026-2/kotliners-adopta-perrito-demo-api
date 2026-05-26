package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.adapters.RazaInfoAdapter
import com.kotliners.adoptaPerrito.dto.response.RazaCampoResponse
import com.kotliners.adoptaPerrito.dto.response.RazaInfoResponse
import com.kotliners.adoptaPerrito.dto.response.RazaResponse
import com.kotliners.adoptaPerrito.dto.response.TipoCampo
import com.kotliners.adoptaPerrito.repositories.RazaRepository
import com.kotliners.adoptaPerrito.services.UsuarioService
import com.kotliners.adoptaPerrito.utils.TokenExtractor

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controlador REST para razas.
 *
 * Endpoints:
 * GET /api/razas?especie=PERRO        - Catalogo de razas para el select
 * GET /api/razas/info?razaId=...      - Info detallada desde API externa
 */
@RestController
@RequestMapping("/api/razas")
class RazaInfoController {

    private val logger = LoggerFactory.getLogger(RazaInfoController::class.java)

    @Autowired lateinit var razaInfoAdapter: RazaInfoAdapter
    @Autowired lateinit var razaRepository: RazaRepository
    @Autowired lateinit var usuarioService: UsuarioService

    /**
     * Lista el catalogo de razas filtrado por especie.
     * URL: GET /api/razas?especie=PERRO
     */
    @GetMapping
    fun listarRazas(
        @RequestHeader("Authorization", required = false) token: String?,
        @RequestParam especie: String
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        TokenExtractor.resolveUser(token, usuarioService)
            ?: return ResponseEntity.status(401).body("Token invalido")

        val razas = razaRepository.findAllByEspecieOrderByNombreEs(especie.uppercase())
            .map { RazaResponse(it.id.toString(), it.especie, it.nombreEs, it.nombreEn) }
        return ResponseEntity.ok(razas)
    }

    /**
     * Obtiene informacion detallada de una raza desde la API externa.
     * Usa el nombre_en de la BD para buscar sin necesidad de traduccion.
     * URL: GET /api/razas/info?razaId=UUID&especie=PERRO
     */
    @GetMapping("/info")
    fun getRazaInfo(
        @RequestHeader("Authorization", required = false) token: String?,
        @RequestParam razaId: String?,
        @RequestParam(required = false) raza: String?,
        @RequestParam especie: String
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        TokenExtractor.resolveUser(token, usuarioService)
            ?: return ResponseEntity.status(401).body("Token invalido")

        // Obtener nombre_en desde la BD si se pasa razaId
        val nombreEnBusqueda: String = if (razaId != null) {
            try {
                val uuid = java.util.UUID.fromString(razaId)
                razaRepository.findById(uuid).orElse(null)?.nombreEn
                    ?: return ResponseEntity.status(404).body("Raza no encontrada")
            } catch (e: IllegalArgumentException) {
                return ResponseEntity.status(400).body("razaId invalido")
            }
        } else if (raza != null) {
            raza.trim()
        } else {
            return ResponseEntity.status(400).body("Se requiere razaId o raza")
        }

        logger.info("Consultando info de raza: '$nombreEnBusqueda' ($especie)")

        val resultado = razaInfoAdapter.buscarRaza(nombreEnBusqueda, especie.uppercase())
            ?: return ResponseEntity.status(404).body("No se encontro informacion para la raza '$nombreEnBusqueda'")

        val response = RazaInfoResponse(
            nombre       = resultado.nombre,
            imagenUrl    = resultado.imagenUrl,
            wikipediaUrl = resultado.wikipediaUrl,
            campos       = resultado.campos.map { RazaCampoResponse(it.etiqueta, it.valor, TipoCampo.valueOf(it.tipo)) }
        )
        return ResponseEntity.ok(response)
    }
}
