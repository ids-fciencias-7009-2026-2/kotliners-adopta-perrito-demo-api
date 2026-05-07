package com.kotliners.adoptaPerrito.controllers

// import com.kotliners.adoptaPerrito.dto.request.CreateAnimalRequest
import com.kotliners.adoptaPerrito.dto.request.DeleteAnimalRequest
// import com.kotliners.adoptaPerrito.dto.request.UpdateAnimalRequest
import com.kotliners.adoptaPerrito.services.AnimalService
import com.kotliners.adoptaPerrito.services.UsuarioService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import jakarta.validation.Valid

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.web.bind.annotation.*

/**
 * Controlador REST para manejar las operaciones relacionadas con los animales disponibles para adopción.
 * 
 * Este controlador expone endpoints para 
 * - Crear un nuevo animal
 * - Obtener la lista de animales disponibles
 * - Obtener detalles de un animal específico
 * - Actualizar la información de un animal
 * - Eliminar un animal del sistema
 */
@RestController
@RequestMapping("/api/animales")
class AnimalController {
    
    /* Logger para registrar información relevante durante la ejecución de las operaciones del controlador */
    private val logger: Logger = LoggerFactory.getLogger(AnimalController::class.java)

    /* Servicio para gestionar operaciones relacionadas con animales */
    @Autowired
    lateinit var animalService: AnimalService

    /* Servicio para validar token y obtener el usuario autenticado */
    @Autowired
    lateinit var userService: UsuarioService

    /**
     * Crear un nuevo animal
     * - URL: POST /api/animales
     * - Requisitos: header `Authorization: Bearer <token>` obligatorio
     * - TODOs:
     *   - Validar y limpiar token (quitar "Bearer ") y buscar usuario con `UsuarioService`
     *   - Validar DTO `CreateAnimalRequest` (usar `@Valid`)
     *   - Mapear DTO -> `Animal` (domain) y llamar `animalService.createAnimal`
     *   - Retornar `201 Created` con el recurso creado o error 400/401 según corresponda
     */
    // @PostMapping
    // fun createAnimal(
    //     @RequestHeader("Authorization", required = true) token: String?,
    //     @Valid @RequestBody createRequest: CreateAnimalRequest
    // ): ResponseEntity<Any> {
    //     // TODO: Implementar: validar token, mapear DTO, llamar servicio y devolver ResponseEntity
    //     return ResponseEntity.status(501).body("Not implemented")
    // }

    /**
     * Obtener la lista de animales disponibles
     * - URL: GET /api/animales
     * - Requisitos: token opcional (listar público), soportar filtros en query params
     * - TODOs:
     *   - Implementar parámetros de paginación/filtros (opcional)
     *   - Llamar `animalService.listAllAnimals()` y mapear a DTOs de respuesta
     */
    // @GetMapping
    // fun listAnimals(
    //     @RequestHeader("Authorization", required = false) token: String?
    // ): ResponseEntity<Any> {
    //     // TODO: Implementar: obtener lista desde servicio y devolver 200
    //     return ResponseEntity.status(501).body("Not implemented")
    // }

    /**
     * Obtener detalles de un animal específico
     * - URL: GET /api/animales/{id}
     * - Requisitos: token opcional
     * - TODOs:
     *   - Llamar `animalService.getAnimalById(id)`
     *   - Devolver 200 con el detalle o 404 si no existe
     */
    // @GetMapping("/{id}")
    // fun getAnimal(
    //     @RequestHeader("Authorization", required = false) token: String?,
    //     @PathVariable id: String
    // ): ResponseEntity<Any> {
    //     // TODO: Implementar: recuperar por id y devolver 200/404
    //     return ResponseEntity.status(501).body("Not implemented")
    // }

    /**
     * Actualizar la información de un animal
     * - URL: PUT /api/animales/{id}
     * - Requisitos: header `Authorization` obligatorio
     * - TODOs:
     *   - Validar token y obtener usuario autenticado
     *   - Verificar que el usuario autenticado sea el dueño del animal (owner check)
     *   - Validar DTO `UpdateAnimalRequest` y aplicar cambios parciales
     *   - Llamar `animalService.updateAnimal(id, updatedDomain)`
     *   - Retornar 200 con el recurso actualizado o 403 si no es owner
     */
    // @PutMapping("/{id}")
    // fun updateAnimal(
    //     @RequestHeader("Authorization", required = true) token: String?,
    //     @PathVariable id: String,
    //     @Valid @RequestBody updateRequest: UpdateAnimalRequest
    // ): ResponseEntity<Any> {
    //     // TODO: Implementar: validación de token, owner check, aplicar update
    //     return ResponseEntity.status(501).body("Not implemented")
    // }

    /**
     * Endpoint para eliminar un animal del sistema
     * 
     * - URL:       /api/animales
     * - Método:    DELETE
     * - Headers:   Authorization: Bearer <token>
     * 
     * @param token Token de autenticación del usuario 
     * @param deleteAnimalRequest DTO que contiene el ID del animal a eliminar
     * @return ResponseEntity con el resultado de la operación:
     *      - 200 OK
     *      - 401 Unauthorized
     *      - 403 Forbidden
     *      - 404 Not Found
     */
    @DeleteMapping
    fun deleteAnimal(
        @RequestHeader("Authorization", required = false) token: String?,
        @Valid @RequestBody deleteAnimalRequest: DeleteAnimalRequest
    ): ResponseEntity<Any> {
        val id = deleteAnimalRequest.animalId
        logger.info("Solicitud para eliminar animal con ID: $id")
        if (token == null) {
            logger.warn("Intento de eliminar animal sin token")
            return ResponseEntity.status(401).body("Token requerido")
        }
        val cleanToken = token.replace("Bearer ", "").trim()
        val userFound = userService.findByToken(cleanToken)
        if (userFound == null) {
            logger.warn("Token inválido al eliminar animal")
            return ResponseEntity.status(401).body("Token inválido")
        }
        val animalFound = animalService.getAnimalById(id)
        if (animalFound == null) {
            logger.warn("Animal no encontrado para eliminar: $id")
            return ResponseEntity.status(404).body("Animal no encontrado")
        }
        if (animalFound.usuarioId != userFound.id) {
            logger.warn("Usuario ${userFound.id} intentó eliminar un animal que no le pertenece")
            return ResponseEntity.status(403).body("No autorizado para eliminar este animal")
        }
        val deleted = animalService.deleteAnimal(id)
        return if (deleted) {
            logger.info("Animal eliminado correctamente: $id")
            ResponseEntity.ok("Animal eliminado exitosamente")
        } else {
            ResponseEntity.status(404).body("Animal no encontrado")
        }
    }
}