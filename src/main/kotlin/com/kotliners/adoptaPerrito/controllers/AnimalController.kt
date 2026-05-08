package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.domain.Animal
import com.kotliners.adoptaPerrito.domain.Estatus
import com.kotliners.adoptaPerrito.domain.Rol
import com.kotliners.adoptaPerrito.domain.Sexo

import com.kotliners.adoptaPerrito.dto.request.CreateAnimalRequest
import com.kotliners.adoptaPerrito.dto.request.DeleteAnimalRequest
import com.kotliners.adoptaPerrito.dto.request.UpdateAnimalRequest
import com.kotliners.adoptaPerrito.dto.response.toAnimalResponse
import com.kotliners.adoptaPerrito.dto.response.toAnimalDetalleResponse

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
     * 
     * @return ResponseEntity con el resultado de la operación:
     *      - 201 Created con el animal creado
     *      - 400 Bad Request si el DTO no es válido
     *      - 401 Unauthorized si el token es inválido o no se proporciona
     *      - 403 Forbidden si el usuario no tiene rol CUIDADOR
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

        try {
            val created = animalService.addNewAnimal(animal, userFound.rol)
            logger.info("Animal creado correctamente con ID: ${created.id}")
            return ResponseEntity.status(201).body(created.toAnimalResponse())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(403).body(e.message ?: "No autorizado")
        }
     }

    /**
     * Obtener los animales del cuidador autenticado.
     * URL:    GET /api/animales/me
     * Header: Authorization: Bearer <token>
     * 
     * @return ResponseEntity con el resultado de la operación:
     *      - 200 OK con la lista de animales del cuidador
     *      - 401 Unauthorized si el token es inválido o no se proporciona
      *     - 403 Forbidden si el usuario no tiene rol CUIDADOR
     */
    @GetMapping("/me")
    fun listMyAnimals(
        @RequestHeader("Authorization", required = true) token: String?
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        val cleanToken = token.replace("Bearer ", "").trim()
        val userFound = userService.findByToken(cleanToken)
            ?: return ResponseEntity.status(401).body("Token invalido")
        logger.info("Listando animales del cuidador: ${userFound.id}")
        try {
            val animals = animalService.listAnimalsByOwner(userFound.id!!, userFound.rol)
            return ResponseEntity.ok(animals.map { it.toAnimalResponse() })
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(403).body(e.message ?: "No autorizado")
        }
    }

    /**
     * - URL: GET /api/animales
     * - Header: Authorization: Bearer <token>
      * 
      * @return ResponseEntity con el resultado de la operación
     */
    @GetMapping
    fun listAnimals(
            @RequestHeader("Authorization", required = true) token: String?
    ): ResponseEntity<Any> {
        logger.info("Solicitud para listar animales")
        if (token == null) {
            logger.warn("Intento de listar animales sin token")
            return ResponseEntity.status(401).body("Token requerido")
        }
        val cleanToken = token.replace("Bearer ", "").trim()
        val userFound = userService.findByToken(cleanToken) ?: return ResponseEntity.status(401).body("Token inválido")

        val rol = userFound.rol

        try{
            if (rol == Rol.CUIDADOR) {
                logger.info("Usuario con rol CUIDADOR listando sus animales")
                val animals = animalService.listAnimalsByOwner(userFound.id!!, rol)
                return ResponseEntity.ok(animals.map { it.toAnimalResponse() })
            } else {
                logger.info("Usuario con rol $rol listando todos los animales disponibles")
                val animals = animalService.searchAllAnimals(rol)
                return ResponseEntity.ok(animals.map { it.toAnimalResponse() })
            }
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(403).body(e.message ?: "No autorizado")
        }
    }

    /**
     * Obtener detalles de un animal específico
     * - URL: GET /api/animales/{id}
     * - Header: Authorization: Bearer <token>
      * 
      * @return ResponseEntity con el resultado de la operación
     */
    @GetMapping("/{id}")
    fun getAnimal(
         @RequestHeader("Authorization", required = true) token: String?,
         @PathVariable id: String
     ): ResponseEntity<Any> {
        logger.info("Solicitud para obtener animal con ID: $id")
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        val cleanToken = token.replace("Bearer ", "").trim()
        val userFound = userService.findByToken(cleanToken) ?: return ResponseEntity.status(401).body("Token invalido")
        try {
            val animal = animalService.getAnimalForRequester(id, userFound.id!!, userFound.rol)
            if (animal == null) return ResponseEntity.status(404).body("Animal no encontrado")
            val detalle = animalService.getAnimalDetalle(id, animal)
            return ResponseEntity.ok(detalle)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(403).body(e.message ?: "No autorizado")
        }
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
        @RequestHeader("Authorization", required = true) token: String?,
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

        try {
            val updated = animalService.updateAnimal(id, updateRequest, userFound.id!!, userFound.rol)
            return if (updated != null) {
                logger.info("Animal actualizado correctamente: $id")
                ResponseEntity.ok(updated)
            } else {
                ResponseEntity.status(404).body("Animal no encontrado")
            }
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(403).body(e.message ?: "No autorizado")
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
        @RequestHeader("Authorization", required = true) token: String?,
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
        try {
            val deleted = animalService.deleteAnimal(id, userFound.id!!, userFound.rol)
            return if (deleted) {
                logger.info("Animal eliminado correctamente: $id")
                ResponseEntity.ok("Animal eliminado exitosamente")
            } else {
                ResponseEntity.status(404).body("Animal no encontrado")
            }
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(403).body(e.message ?: "No autorizado")
        }
    }
}