package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.domain.Rol
import com.kotliners.adoptaPerrito.services.ReporteService
import com.kotliners.adoptaPerrito.services.UsuarioService
import com.kotliners.adoptaPerrito.utils.TokenExtractor
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/reportes")
class ReporteController(
    private val reporteService: ReporteService,
    private val usuarioService: UsuarioService
) {

    private val logger = LoggerFactory.getLogger(ReporteController::class.java)

    /**
     * GET /api/reportes/check/{animalId}
     * Verifica si el usuario actual ya reportó este animal.
     */
    @GetMapping("/check/{animalId}")
    fun checkReporte(
        @RequestHeader("Authorization", required = false) token: String?,
        @PathVariable animalId: String
    ): ResponseEntity<Any> {
        val usuario = TokenExtractor.resolveUser(token, usuarioService)
            ?: return ResponseEntity.status(401).body("Token requerido o invalido")
        val existe = reporteService.usuarioYaReporto(UUID.fromString(usuario.id!!), UUID.fromString(animalId))
        return ResponseEntity.ok(mapOf("reportado" to existe))
    }

    /**
     * DELETE /api/reportes/{animalId}
     * El usuario retira su reporte de un animal.
     */
    @DeleteMapping("/{animalId}")
    fun retirarReporte(
        @RequestHeader("Authorization", required = false) token: String?,
        @PathVariable animalId: String
    ): ResponseEntity<Any> {
        val usuario = TokenExtractor.resolveUser(token, usuarioService)
            ?: return ResponseEntity.status(401).body("Token requerido o invalido")
        return try {
            reporteService.retirarReporte(UUID.fromString(usuario.id!!), UUID.fromString(animalId))
            ResponseEntity.ok(mapOf("mensaje" to "Reporte retirado"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    /**
     * POST /api/reportes
     * Crea un reporte con motivo. Cualquier usuario autenticado puede reportar.
     * Body: { "animalId": "uuid", "motivo": "texto" }
     */
    @PostMapping
    fun crearReporte(
        @RequestHeader("Authorization", required = false) token: String?,
        @RequestBody body: Map<String, String>
    ): ResponseEntity<Any> {
        val usuario = TokenExtractor.resolveUser(token, usuarioService)
            ?: return ResponseEntity.status(401).body("Token requerido o invalido")

        val animalId = body["animalId"] ?: return ResponseEntity.badRequest().body("animalId es requerido")
        val motivo = body["motivo"] ?: return ResponseEntity.badRequest().body("motivo es requerido")

        return try {
            val uuid = UUID.fromString(animalId)
            val reporte = reporteService.crearReporte(UUID.fromString(usuario.id!!), uuid, motivo)
            ResponseEntity.status(201).body(reporte)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    /**
     * GET /api/reportes/pendientes
     * Lista reportes pendientes. Solo ADMINISTRADOR.
     */
    @GetMapping("/pendientes")
    fun listarPendientes(
        @RequestHeader("Authorization", required = false) token: String?
    ): ResponseEntity<Any> {
        val usuario = TokenExtractor.resolveUser(token, usuarioService)
            ?: return ResponseEntity.status(401).body("Token requerido o invalido")
        if (usuario.rol != Rol.ADMINISTRADOR) {
            return ResponseEntity.status(403).body("Acceso denegado")
        }
        return ResponseEntity.ok(reporteService.listarPendientes())
    }

    /**
     * POST /api/reportes/{id}/resolver
     * Resuelve un reporte: elimina la publicación y envía correo. Solo ADMINISTRADOR.
     */
    @PostMapping("/{id}/resolver")
    fun resolver(
        @RequestHeader("Authorization", required = false) token: String?,
        @PathVariable id: String
    ): ResponseEntity<Any> {
        val usuario = TokenExtractor.resolveUser(token, usuarioService)
            ?: return ResponseEntity.status(401).body("Token requerido o invalido")
        if (usuario.rol != Rol.ADMINISTRADOR) {
            return ResponseEntity.status(403).body("Acceso denegado")
        }
        return try {
            val reporte = reporteService.resolver(UUID.fromString(id))
            ResponseEntity.ok(reporte)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(e.message)
        }
    }

    /**
     * POST /api/reportes/{id}/desestimar
     * Desestima un reporte sin eliminar la publicación. Solo ADMINISTRADOR.
     */
    @PostMapping("/{id}/desestimar")
    fun desestimar(
        @RequestHeader("Authorization", required = false) token: String?,
        @PathVariable id: String
    ): ResponseEntity<Any> {
        val usuario = TokenExtractor.resolveUser(token, usuarioService)
            ?: return ResponseEntity.status(401).body("Token requerido o invalido")
        if (usuario.rol != Rol.ADMINISTRADOR) {
            return ResponseEntity.status(403).body("Acceso denegado")
        }
        return try {
            val reporte = reporteService.desestimar(UUID.fromString(id))
            ResponseEntity.ok(reporte)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(e.message)
        }
    }
}
