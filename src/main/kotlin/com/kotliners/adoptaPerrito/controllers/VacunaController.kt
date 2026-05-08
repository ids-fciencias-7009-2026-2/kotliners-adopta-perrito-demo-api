package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.dto.request.CreateVacunaRequest
import com.kotliners.adoptaPerrito.dto.response.VacunaResponse

import com.kotliners.adoptaPerrito.services.VacunaService
import com.kotliners.adoptaPerrito.services.UsuarioService

import jakarta.validation.Valid

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired

import org.springframework.http.ResponseEntity

import org.springframework.web.bind.annotation.*

/**
 * Controlador REST para gestionar las vacunas de los animales.
 * 
 * Solo cuidadores pueden crear nuevas vacunas, pero cualquier usuario puede listar las vacunas disponibles.
 */
@RestController
@RequestMapping("/api/vacunas")
class VacunaController {

    /* Logger para registrar eventos y errores */
    private val logger: Logger = LoggerFactory.getLogger(VacunaController::class.java)

    /* Servicio de vacunas inyectado por Spring */
    @Autowired
    lateinit var vacunaService: VacunaService

    /* Servicio de usuarios para validar autenticación y roles */
    @Autowired
    lateinit var usuarioService: UsuarioService

    /**
     * Endpoint para listar todas las vacunas disponibles en el sistema.
     * 
     * URL: GET /api/vacunas
     * Headers: Authorization: Bearer <token de autenticación>
     * 
     * @return ResponseEntity con la lista de vacunas
     */
    @GetMapping
    fun listVacunas(
        @RequestHeader("Authorization", required = true) token: String?,
    ): ResponseEntity<List<VacunaResponse>> {
        if (token == null) {
            logger.warn("Intento de acceso a lista de vacunas sin token de autenticación")
            return ResponseEntity.status(401).build()
        }
        val cleanToken = token.removePrefix("Bearer ").trim()
        val userFound = usuarioService.findByToken(cleanToken)
        if (userFound == null) {
            logger.warn("Token de autenticación inválido al intentar acceder a lista de vacunas")
            return ResponseEntity.status(401).build()
        }
        logger.info("Usuario autenticado accediendo a lista de vacunas")
        return ResponseEntity.ok(vacunaService.listVacunas())
    }

    /**
     * Endpoint para crear una nueva vacuna en el sistema.
     * - Solo los usuarios con rol de cuidador pueden crear vacunas. 
     * 
     * URL: POST /api/vacunas
     * Headers: Authorization: Bearer <token de autenticación>
     * 
     * @param request Datos para crear la vacuna
     * @return ResponseEntity con la vacuna creada
     */
    @PostMapping
    fun createVacuna(
        @RequestHeader("Authorization", required = true) token: String?,
        @Valid @RequestBody request: CreateVacunaRequest
    ): ResponseEntity<VacunaResponse> {
        if (token == null) {
            logger.warn("Intento de creación de vacuna sin token de autenticación")
            return ResponseEntity.status(401).build()
        }
        val cleanToken = token.removePrefix("Bearer ").trim()
        val userFound = usuarioService.findByToken(cleanToken)
        if (userFound == null) {
            logger.warn("Token de autenticación inválido al intentar crear vacuna")
            return ResponseEntity.status(401).build()
        }
        try {
            val created = vacunaService.createVacuna(request, userFound.rol)
            logger.info("Vacuna creada exitosamente por usuario ${userFound.username}")
            return ResponseEntity.ok(created)
        } catch (e: Exception) {
            logger.error("Error al crear vacuna: ${e.message}", e)
            return ResponseEntity.status(500).build()
        }
    }
}