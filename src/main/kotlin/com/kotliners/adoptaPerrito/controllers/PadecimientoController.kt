package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.dto.request.CreatePadecimientoRequest
import com.kotliners.adoptaPerrito.dto.response.PadecimientoResponse

import com.kotliners.adoptaPerrito.services.PadecimientoService
import com.kotliners.adoptaPerrito.services.UsuarioService

import jakarta.validation.Valid

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired

import org.springframework.http.ResponseEntity

import org.springframework.web.bind.annotation.*

/**
 * Controlador REST para gestionar los padecimientos de los animales.
 * 
 * Solo cuidadores pueden crear nuevos padecimientos, pero cualquier usuario puede listar los padecimientos disponibles. 
 */
@RestController
@RequestMapping("/api/padecimientos")
class PadecimientoController {

    /* Logger para registrar eventos y errores */
    private val logger: Logger = LoggerFactory.getLogger(PadecimientoController::class.java)

    /* Servicio de padecimientos inyectado por Spring */
    @Autowired
    lateinit var padecimientoService: PadecimientoService

    /* Servicio de usuarios para validar autenticación y roles */
    @Autowired
    lateinit var usuarioService: UsuarioService
    
    /**
     * Endpoint para listar todos los padecimientos disponibles en el sistema.
     * 
     * URL: GET /api/padecimientos
     * Headers: Authorization: Bearer <token de autenticación>
     * 
     * @return ResponseEntity con la lista de padecimientos
     */
    @GetMapping
    fun listPadecimientos(
        @RequestHeader("Authorization", required = true) token: String?,
    ): ResponseEntity<List<PadecimientoResponse>> {
        if (token == null) {
            logger.warn("Intento de acceso a lista de padecimientos sin token de autenticación")
            return ResponseEntity.status(401).build()
        }
        val cleanToken = token.removePrefix("Bearer ").trim()
        val userFound = usuarioService.findByToken(cleanToken)
        if (userFound == null) {
            logger.warn("Token de autenticación inválido al intentar acceder a lista de padecimientos")
            return ResponseEntity.status(401).build()
        }
        logger.info("Acceso a lista de padecimientos autorizado para el usuario: {}", userFound.id)
        return ResponseEntity.ok(padecimientoService.listPadecimientos())
    }

    /**
     * Endpoint para crear un nuevo padecimiento en el sistema.
     * - Solo los usuarios con rol de cuidador pueden crear padecimientos.
     *
     * URL: POST /api/padecimientos
     * Headers: Authorization: Bearer <token de autenticación>
     * 
     * @param request Datos para crear el padecimiento
     * @return ResponseEntity con el padecimiento creado o un error si ocurre un problema al crear el padecimiento
     */
    @PostMapping
    fun createPadecimiento(
        @RequestHeader("Authorization", required = true) token: String?,
        @Valid @RequestBody request: CreatePadecimientoRequest
    ): ResponseEntity<PadecimientoResponse> {
        if (token == null) {
            logger.warn("Intento de creación de padecimiento sin token de autenticación")
            return ResponseEntity.status(401).build()
        }
        val cleanToken = token.removePrefix("Bearer ").trim()
        val userFound = usuarioService.findByToken(cleanToken)
        if (userFound == null) {
            logger.warn("Token de autenticación inválido al intentar crear padecimiento")
            return ResponseEntity.status(401).build()
        }
        try{
            val createdPadecimiento = padecimientoService.createPadecimiento(request, userFound.rol)
            logger.info("Padecimiento creado exitosamente con ID: {} por usuario: {}", createdPadecimiento.id, userFound.id)
            return ResponseEntity.ok(createdPadecimiento)
        } catch (e: Exception) {
            logger.error("Error al crear padecimiento", e)
            return ResponseEntity.status(500).build()
        }
    }
}