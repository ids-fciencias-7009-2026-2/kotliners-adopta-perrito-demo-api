package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.services.InteresService
import com.kotliners.adoptaPerrito.services.UsuarioService

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controlador REST para gestionar el interés de usuarios en animales.
 *
 * Expone los endpoints:
 * - POST   /api/animales/{id}/interes  - Manifestar interés en un animal
 * - DELETE /api/animales/{id}/interes  - Eliminar interés en un animal
 * - GET    /api/usuarios/me/intereses  -  Listar animales de interés del usuario autenticado
 */
@RestController
class InteresController {

    private val logger: Logger = LoggerFactory.getLogger(InteresController::class.java)

    @Autowired
    lateinit var interesService: InteresService

    @Autowired
    lateinit var usuarioService: UsuarioService

    /**
     * Extrae y valida el token del header Authorization.
     * @return el usuario encontrado o null si el token es inválido/ausente
     */
    private fun resolveUser(token: String?) =
        token?.replace("Bearer ", "")?.trim()?.let { usuarioService.findByToken(it) }

    /**
     * Manifestar interés en un animal.
     * URL:    POST /api/animales/{id}/interes
     * Header: Authorization: Bearer <token>
     *
     * @param token Token de sesión del usuario autenticado
     * @param animalId ID del animal en el que se desea manifestar interés
     * @return 201 Created con el interés registrado, 401 si no autenticado, 400 si ya existe
     */
    @PostMapping("/api/animales/{id}/interes")
    fun manifestarInteres(
        @RequestHeader("Authorization", required = false) token: String?,
        @PathVariable("id") animalId: String
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        val usuario = resolveUser(token) ?: return ResponseEntity.status(401).body("Token inválido")

        logger.info("Usuario ${usuario.id} manifiesta interés en animal $animalId")
        val interes = interesService.manifestarInteres(usuario.id!!, animalId)
        return ResponseEntity.status(201).body(interes)
    }

    /**
     * Eliminar interés en un animal.
     * URL:    DELETE /api/animales/{id}/interes
     * Header: Authorization: Bearer <token>
     *
     * @param token Token de sesión del usuario autenticado
     * @param animalId ID del animal del que se desea eliminar el interés
     * @return 200 OK si se eliminó, 401 si no autenticado, 400 si no existía el interés
     */
    @DeleteMapping("/api/animales/{id}/interes")
    fun eliminarInteres(
        @RequestHeader("Authorization", required = false) token: String?,
        @PathVariable("id") animalId: String
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        val usuario = resolveUser(token) ?: return ResponseEntity.status(401).body("Token inválido")

        logger.info("Usuario ${usuario.id} elimina interés en animal $animalId")
        interesService.eliminarInteres(usuario.id!!, animalId)
        return ResponseEntity.ok("Interés eliminado correctamente")
    }

    /**
     * Listar los animales de interés del usuario autenticado.
     * URL:    GET /api/usuarios/me/intereses
     * Header: Authorization: Bearer <token>
     *
     * @param token Token de sesión del usuario autenticado
     * @return 200 OK con la lista de animales de interés, 401 si no autenticado
     */
    @GetMapping("/api/usuarios/me/intereses")
    fun listarIntereses(
        @RequestHeader("Authorization", required = false) token: String?
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        val usuario = resolveUser(token) ?: return ResponseEntity.status(401).body("Token inválido")

        logger.info("Listando intereses del usuario ${usuario.id}")
        val intereses = interesService.listarIntereses(usuario.id!!)
        return ResponseEntity.ok(intereses)
    }
}
