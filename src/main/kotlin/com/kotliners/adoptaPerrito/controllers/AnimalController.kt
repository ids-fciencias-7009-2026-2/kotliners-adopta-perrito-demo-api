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
import com.kotliners.adoptaPerrito.dto.response.VacunaResponse
import com.kotliners.adoptaPerrito.dto.response.PadecimientoResponse

import com.kotliners.adoptaPerrito.adapters.CloudinaryAdapter

import com.kotliners.adoptaPerrito.services.AnimalService
import com.kotliners.adoptaPerrito.services.ReporteService
import com.kotliners.adoptaPerrito.services.UsuarioService
import com.kotliners.adoptaPerrito.utils.TokenExtractor


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import jakarta.validation.Valid

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

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

    @Autowired
    lateinit var userService: UsuarioService

    @Autowired
    lateinit var cloudinaryAdapter: CloudinaryAdapter

    @Autowired
    lateinit var reporteService: ReporteService

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
            razaId = createRequest.razaId,
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
            return ResponseEntity.ok(animals.map { animal ->
                val foto = animalService.getPrimeraFoto(animal.id ?: "")
                val interesados = animalService.getNumInteresados(animal.id ?: "")
                animal.toAnimalResponse(foto, interesados)
            })
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(403).body(e.message ?: "No autorizado")
        }
    }

    /**
     * - URL: GET /api/animales
     * - Header: Authorization: Bearer <token>
     * - Query params opcionales (solo para ADOPTANTE):
     *   especie, sexo, esterilizado, codigoPostal, vacuna, sinPadecimientos, ordenar
      * 
      * @return ResponseEntity con el resultado de la operacion
     */
    @GetMapping
    fun listAnimals(
            @RequestHeader("Authorization", required = true) token: String?,
            @RequestParam(required = false) especie: String?,
            @RequestParam(required = false) sexo: String?,
            @RequestParam(required = false) esterilizado: Boolean?,
            @RequestParam(required = false) codigoPostal: String?,
            @RequestParam(required = false) vacuna: String?,
            @RequestParam(required = false) razaId: String?,
            @RequestParam(defaultValue = "false") sinPadecimientos: Boolean,
            @RequestParam(defaultValue = "false") soloVacunados: Boolean,
            @RequestParam(required = false) edadMinAnios: Int?,
            @RequestParam(required = false) edadMaxAnios: Int?,
            @RequestParam(required = false) distanciaKm: Double?,
            @RequestParam(required = false) ordenar: String?,
            @RequestParam(defaultValue = "true") ordenDesc: Boolean
    ): ResponseEntity<Any> {
        logger.info("Solicitud para listar animales")
        if (token == null) {
            logger.warn("Intento de listar animales sin token")
            return ResponseEntity.status(401).body("Token requerido")
        }
        val cleanToken = token.replace("Bearer ", "").trim()
        val userFound = userService.findByToken(cleanToken) ?: return ResponseEntity.status(401).body("Token inválido")

        val rol = userFound.rol

        try {
            if (rol == Rol.CUIDADOR) {
                logger.info("Usuario con rol CUIDADOR listando sus animales")
                val animals = animalService.listAnimalsByOwner(userFound.id!!, rol)
                return ResponseEntity.ok(animals.map { animal ->
                    val foto = animalService.getPrimeraFoto(animal.id ?: "")
                    val interesados = animalService.getNumInteresados(animal.id ?: "")
                    val coords = animalService.getCoordsForAnimal(animal)
                    animal.toAnimalResponse(foto, interesados, coords?.first, coords?.second)
                })
            } else {
                // ADOPTANTE: aplica filtros si se proporcionaron
                val hayFiltros = especie != null || sexo != null || esterilizado != null ||
                    codigoPostal != null || vacuna != null || razaId != null || sinPadecimientos ||
                    soloVacunados || edadMinAnios != null || edadMaxAnios != null || distanciaKm != null || ordenar != null

                val animals = if (hayFiltros) {
                    logger.info("ADOPTANTE usando filtros avanzados")
                    animalService.buscarAnimalesConFiltros(
                        requesterRole = rol,
                        requesterId = userFound.id!!,
                        especie = especie,
                        sexo = sexo,
                        esterilizado = esterilizado,
                        codigoPostal = codigoPostal,
                        vacuna = vacuna,
                        razaId = razaId,
                        sinPadecimientos = sinPadecimientos,
                        soloVacunados = soloVacunados,
                        edadMinAnios = edadMinAnios,
                        edadMaxAnios = edadMaxAnios,
                        distanciaKm = distanciaKm,
                        ordenar = ordenar,
                        ordenDesc = ordenDesc
                    )
                } else {
                    logger.info("ADOPTANTE listando todos los animales disponibles")
                    animalService.searchAllAnimals(rol)
                }

                return ResponseEntity.ok(animals.map { animal ->
                    val foto = animalService.getPrimeraFoto(animal.id ?: "")
                    val coords = animalService.getCoordsForAnimal(animal)
                    animal.toAnimalResponse(foto, 0, coords?.first, coords?.second)
                })
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
    fun deleteAnimal(        @RequestHeader("Authorization", required = true) token: String?,
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

    /**
     * Actualiza las vacunas de un animal (reemplaza la lista completa).
     * URL: PUT /api/animales/{id}/vacunas
     * Body: ["Rabia", "Moquillo"]
     */
    @PutMapping("/{id}/vacunas")
    fun updateVacunas(
        @RequestHeader("Authorization", required = false) token: String?,
        @PathVariable id: String,
        @RequestBody nombres: List<String>
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        val cleanToken = token.replace("Bearer ", "").trim()
        val userFound = userService.findByToken(cleanToken) ?: return ResponseEntity.status(401).body("Token invalido")
        return try {
            animalService.updateVacunas(id, nombres, userFound.id!!, userFound.rol)
            ResponseEntity.ok("Vacunas actualizadas")
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(403).body(e.message ?: "No autorizado")
        }
    }

    /**
     * Actualiza los padecimientos de un animal (reemplaza la lista completa).
     * URL: PUT /api/animales/{id}/padecimientos
     * Body: ["Diabetes", "Artritis"]
     */
    @PutMapping("/{id}/padecimientos")
    fun updatePadecimientos(
        @RequestHeader("Authorization", required = false) token: String?,
        @PathVariable id: String,
        @RequestBody nombres: List<String>
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        val cleanToken = token.replace("Bearer ", "").trim()
        val userFound = userService.findByToken(cleanToken) ?: return ResponseEntity.status(401).body("Token invalido")
        return try {
            animalService.updatePadecimientos(id, nombres, userFound.id!!, userFound.rol)
            ResponseEntity.ok("Padecimientos actualizados")
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(403).body(e.message ?: "No autorizado")
        }
    }
    /**
     * Retorna el historial de animales adoptados del cuidador autenticado.
     *
     * URL:    GET http://localhost:8080/api/animales/historial-adoptados
     * Headers: Authorization: Bearer <token>
     */
    @GetMapping("/historial-adoptados")
    fun historialAdoptados(
        @RequestHeader("Authorization", required = true) token: String?
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        val cleanToken = token.replace("Bearer ", "").trim()
        val userFound = userService.findByToken(cleanToken)
            ?: return ResponseEntity.status(401).body("Token invalido")

        return try {
            val historial = animalService.historialAdoptados(
                cuidadorId = userFound.id ?: return ResponseEntity.status(401).body("Token invalido"),
                rol = userFound.rol
            )
            ResponseEntity.ok(historial)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(403).body(e.message ?: "No autorizado")
        }
    }

    /**
     * Reporta un animal como inapropiado, creando un reporte con motivo.
     * URL: PATCH /api/animales/{id}/inapropiado
     * Header: Authorization: Bearer <token>
     * Body: { "motivo": "texto" }
     */
    @PatchMapping("/{id}/inapropiado")
    fun marcarAnimalInapropiado(
        @RequestHeader("Authorization", required = true) token: String?,
        @PathVariable id: String,
        @RequestBody(required = false) body: Map<String, String>?
    ): ResponseEntity<Any> {
        val userFound = TokenExtractor.resolveUser(token, userService)
            ?: return ResponseEntity.status(401).body("Token inválido")
        val motivo = body?.get("motivo") ?: "Contenido inapropiado"
        return try {
            val reporte = reporteService.crearReporte(
                java.util.UUID.fromString(userFound.id!!),
                java.util.UUID.fromString(id),
                motivo
            )
            ResponseEntity.status(201).body(reporte)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(e.message ?: "Error al reportar")
        }
    }
}
