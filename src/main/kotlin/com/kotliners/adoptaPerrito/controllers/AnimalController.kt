package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.domain.Animal
import com.kotliners.adoptaPerrito.domain.Estatus
import com.kotliners.adoptaPerrito.domain.Sexo

import com.kotliners.adoptaPerrito.dto.request.CreateAnimalRequest
import com.kotliners.adoptaPerrito.dto.request.DeleteAnimalRequest
import com.kotliners.adoptaPerrito.dto.request.UpdateAnimalRequest
import com.kotliners.adoptaPerrito.dto.response.toAnimalResponse

import com.kotliners.adoptaPerrito.services.AnimalService
import com.kotliners.adoptaPerrito.services.UsuarioService

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import jakarta.validation.Valid

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.web.bind.annotation.*

import java.time.LocalDateTime

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
     * - Header: Authorization: Bearer <token>
     * - Requisitos: header `Authorization: Bearer <token>` obligatorio
     * - TODOs:
     *   - Validar y limpiar token (quitar "Bearer ") y buscar usuario con `UsuarioService`
     *   - Validar DTO `CreateAnimalRequest` (usar `@Valid`)
     *   - Mapear DTO -> `Animal` (domain) y llamar `animalService.createAnimal`
     *   - Retornar `201 Created` con el recurso creado o error 400/401 según corresponda
     */
    @PostMapping
    fun createAnimal(
         @RequestHeader("Authorization", required = true) token: String?,
         @Valid @RequestBody createRequest: CreateAnimalRequest
     ): ResponseEntity<Any> {
        logger.info("Solicitud para crear animal")
        if (token == null) {
            logger.warn("Intento de crear animal sin token")
            return ResponseEntity.status(401).body("Token requerido")
        }

        val cleanToken = token.replace("Bearer ", "").trim()
        val userFound = userService.findByToken(cleanToken)
        if (userFound == null) {
            logger.warn("Token inválido al crear animal")
            return ResponseEntity.status(401).body("Token inválido")
        }

        val animal = Animal(
            nombre = createRequest.nombre,
            especie = createRequest.especie,
            raza = createRequest.raza,
            fechaNacimiento = createRequest.fechaNacimiento,
            sexo = createRequest.sexo,
            descripcion = createRequest.descripcion,
            estatus = Estatus.DISPONIBLE,
            usuarioId = userFound.id!!,
            esterilizado = createRequest.esterilizado,
            fechaRegistro = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val created = animalService.addNewAnimal(animal)
        logger.info("Animal creado correctamente con ID: ${created.id}")
        return ResponseEntity.status(201).body(created.toAnimalResponse())
     }

    /**
     * Obtener los animales del cuidador autenticado.
     * URL:    GET /api/animales/me
     * Header: Authorization: Bearer <token>
     */
    @GetMapping("/me")
    fun listMyAnimals(
        @RequestHeader("Authorization", required = false) token: String?
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        val cleanToken = token.replace("Bearer ", "").trim()
        val userFound = userService.findByToken(cleanToken)
            ?: return ResponseEntity.status(401).body("Token invalido")
        logger.info("Listando animales del cuidador: ${userFound.id}")
        val animals = animalService.listAnimalsByOwner(userFound.id!!)
        return ResponseEntity.ok(animals.map { it.toAnimalResponse() })
    }

    /**
     * - URL: GET /api/animales
     * - Requisitos: token opcional (listar público), soportar filtros en query params
     * - TODOs:
     *   - Implementar parámetros de paginación/filtros (opcional)
     *   - Llamar `animalService.listAllAnimals()` y mapear a DTOs de respuesta
     */
    @GetMapping
    fun listAnimals(
         @RequestHeader("Authorization", required = false) token: String?
     ): ResponseEntity<Any> {
        logger.info("Solicitud para listar animales")
        val animals = animalService.searchAllAnimals()
        val response = animals.map { it.toAnimalResponse() }
        return ResponseEntity.ok(response)
     }

    /**
     * Obtener detalles de un animal específico
     * - URL: GET /api/animales/{id}
     * - Requisitos: token opcional
     * - TODOs:
     *   - Llamar `animalService.getAnimalById(id)`
     *   - Devolver 200 con el detalle o 404 si no existe
     */
    @GetMapping("/{id}")
    fun getAnimal(
         @RequestHeader("Authorization", required = false) token: String?,
         @PathVariable id: String
     ): ResponseEntity<Any> {
        logger.info("Solicitud para obtener animal con ID: $id")
        val animal = animalService.getAnimalById(id)
        if (animal == null) {
            logger.warn("Animal no encontrado: $id")
            return ResponseEntity.status(404).body("Animal no encontrado")
        }
        return ResponseEntity.ok(animal.toAnimalResponse())
     }

    /**
     * Actualizar la información de un animal
     * 
     * - URL:  /api/animales/{id}
     * - Método: PUT
     * - Headers: Authorization: Bearer <token>
     * 
     * @param token Token de autenticación del usuario 
     * @param id Identificador del animal a actualizar
     * @param updateRequest DTO con los campos a actualizar
     * @return ResponseEntity con el resultado de la operación:
     *      - 200 OK con el animal actualizado
     *      - 400 Bad Request si el DTO no es válido
     *      - 401 Unauthorized si el token es inválido o no se proporciona
     *      - 403 Forbidden si el usuario no es dueño del animal
     *      - 404 Not Found si no existe el animal
     */
    @PutMapping("/{id}")
    fun updateAnimal(
        @RequestHeader("Authorization", required = false) token: String?,
        @PathVariable id: String,
        @Valid @RequestBody updateRequest: UpdateAnimalRequest
    ): ResponseEntity<Any> {
        logger.info("Solicitud para actualizar animal con ID: $id")
        if (token == null) {
            logger.warn("Intento de actualizar animal sin token")
            return ResponseEntity.status(401).body("Token requerido")
        }

        val cleanToken = token.replace("Bearer ", "").trim()
        val userFound = userService.findByToken(cleanToken)
        if (userFound == null) {
            logger.warn("Token inválido al actualizar animal")
            return ResponseEntity.status(401).body("Token inválido")
        }

        val animalFound = animalService.getAnimalById(id)
        if (animalFound == null) {
            logger.warn("Animal no encontrado para actualizar: $id")
            return ResponseEntity.status(404).body("Animal no encontrado")
        }

        if (animalFound.usuarioId != userFound.id) {
            logger.warn("Usuario ${userFound.id} intentó actualizar un animal que no le pertenece")
            return ResponseEntity.status(403).body("No autorizado para actualizar este animal")
        }

        val updated = animalService.updateAnimal(id, updateRequest)
        return if (updated != null) {
            logger.info("Animal actualizado correctamente: $id")
            ResponseEntity.ok(updated)
        } else {
            ResponseEntity.status(404).body("Animal no encontrado")
        }
    }

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