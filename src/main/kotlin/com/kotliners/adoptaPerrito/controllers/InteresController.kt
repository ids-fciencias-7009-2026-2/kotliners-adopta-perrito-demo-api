package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.dto.response.InteresRecibidoResponse
import com.kotliners.adoptaPerrito.services.InteresService
import com.kotliners.adoptaPerrito.services.UsuarioService
import com.kotliners.adoptaPerrito.utils.TokenExtractor

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controlador REST para gestionar el interes de usuarios en animales.
 *
 * Expone los endpoints:
 * - POST   /api/animales/{id}/interes   - Manifestar interes en un animal
 * - DELETE /api/animales/{id}/interes   - Eliminar interes en un animal
 * - GET    /api/usuarios/me/intereses   - Listar animales de interes del adoptante autenticado
 * - GET    /api/animales/me/intereses   - Listar adoptantes interesados en animales del cuidador
 */
@RestController
class InteresController {

    private val logger: Logger = LoggerFactory.getLogger(InteresController::class.java)

    @Autowired
    lateinit var interesService: InteresService

    @Autowired
    lateinit var usuarioService: UsuarioService

    /**
     * Extrae y valida el token del header Authorization usando TokenExtractor.
     * @return el usuario encontrado o null si el token es invalido/ausente
     */
    private fun resolveUser(token: String?) = TokenExtractor.resolveUser(token, usuarioService)

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
        return try {
            val interes = interesService.manifestarInteres(usuario.id!!, usuario.rol, animalId)
            ResponseEntity.status(201).body(interes)
        } catch (e: IllegalArgumentException) {
            logger.warn("Error al manifestar interes: ${e.message}")
            ResponseEntity.badRequest().body(e.message)
        } catch (e: IllegalStateException) {
            logger.warn("Error de estado al manifestar interes: ${e.message}")
            ResponseEntity.status(503).body(e.message)
        }
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
        return try {
            interesService.eliminarInteres(usuario.id!!, animalId)
            ResponseEntity.ok("Interes eliminado correctamente")
        } catch (e: IllegalArgumentException) {
            logger.warn("Error al eliminar interes: ${e.message}")
            ResponseEntity.badRequest().body(e.message)
        }
    }

    /**
     * Listar los animales de interes del adoptante autenticado (paginado).
     * URL:    GET /api/usuarios/me/intereses?limit=20&offset=0
     * Header: Authorization: Bearer <token>
     *
     * @param token  Token de sesion del usuario autenticado
     * @param limit  Maximo de resultados (default 20, max 100)
     * @param offset Numero de resultados a saltar (default 0)
     * @return 200 OK con la lista paginada de animales de interes, 401 si no autenticado
     */
    @GetMapping("/api/usuarios/me/intereses")
    fun listarIntereses(
        @RequestHeader("Authorization", required = false) token: String?,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        val usuario = resolveUser(token) ?: return ResponseEntity.status(401).body("Token invalido")

        logger.info("Listando intereses del usuario ${usuario.id} (limit=$limit, offset=$offset)")
        val intereses = interesService.listarIntereses(usuario.id!!, limit, offset)
        return ResponseEntity.ok(intereses)
    }

    /**
     * Listar los adoptantes interesados en los animales del cuidador autenticado (paginado).
     * URL:    GET /api/animales/me/intereses?limit=20&offset=0
     * Header: Authorization: Bearer <token>
     *
     * @param token  Token de sesion del cuidador autenticado
     * @param limit  Maximo de resultados (default 20, max 100)
     * @param offset Numero de resultados a saltar (default 0)
     * @return 200 OK con la lista de InteresRecibidoResponse, 401 si no autenticado, 403 si no es cuidador
     */
    @GetMapping("/api/animales/me/intereses")
    fun listarInteresesRecibidos(
        @RequestHeader("Authorization", required = false) token: String?,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        val usuario = resolveUser(token) ?: return ResponseEntity.status(401).body("Token invalido")

        logger.info("Listando intereses recibidos por cuidador ${usuario.id} (limit=$limit, offset=$offset)")
        val intereses: List<InteresRecibidoResponse> = interesService.listarInteresesRecibidos(usuario.id!!, limit, offset)
        return ResponseEntity.ok(intereses)
    }

    /**
     * Listar adoptantes interesados en un animal especifico del cuidador.
     * URL:    GET /api/animales/{id}/interesados
     * Header: Authorization: Bearer <token>
     */
    @GetMapping("/api/animales/{id}/interesados")
    fun listarInteresadosPorAnimal(
        @RequestHeader("Authorization", required = false) token: String?,
        @PathVariable("id") animalId: String
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        val usuario = resolveUser(token) ?: return ResponseEntity.status(401).body("Token invalido")

        val intereses = interesService.listarInteresesRecibidos(usuario.id!!, 100, 0)
            .filter { it.animalId == animalId }
        return ResponseEntity.ok(intereses)
    }
}
