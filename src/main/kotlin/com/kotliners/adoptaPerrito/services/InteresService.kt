package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.domain.AnimalInteres
import com.kotliners.adoptaPerrito.dto.response.AnimalInteresResponse
import com.kotliners.adoptaPerrito.entities.AnimalInteresEntity
import com.kotliners.adoptaPerrito.entities.AnimalInteresId
import com.kotliners.adoptaPerrito.repositories.AnimalInteresRepository
import com.kotliners.adoptaPerrito.repositories.AnimalRepository

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.UUID

/**
 * Servicio de negocio para gestionar el interés de usuarios en animales.
 *
 * Maneja las operaciones de:
 * - Manifestar interés en un animal
 * - Eliminar interés en un animal
 * - Listar los animales de interés de un usuario
 */
@Service
class InteresService {

    private val logger: Logger = LoggerFactory.getLogger(InteresService::class.java)

    @Autowired
    lateinit var interesRepository: AnimalInteresRepository

    @Autowired
    lateinit var animalRepository: AnimalRepository

    /**
     * Registra el interés de un usuario en un animal.
     *
     * @param usuarioId ID del usuario autenticado
     * @param animalId ID del animal
     * @return AnimalInteres creado, o null si el animal no existe o ya existe el interés
     * @throws IllegalArgumentException si el animal no existe o el interés ya fue registrado
     */
    fun manifestarInteres(usuarioId: String, animalId: String): AnimalInteres {
        logger.info("Registrando interés: usuario=$usuarioId, animal=$animalId")
        val animalUuid = UUID.fromString(animalId)
        val usuarioUuid = UUID.fromString(usuarioId)

        if (!animalRepository.existsById(animalUuid)) {
            logger.warn("Animal no encontrado: $animalId")
            throw IllegalArgumentException("Animal no encontrado")
        }

        if (interesRepository.existsByUsuarioIdAndAnimalId(usuarioUuid, animalUuid)) {
            logger.warn("El usuario $usuarioId ya tiene interés en el animal $animalId")
            throw IllegalArgumentException("Ya manifestaste interés en este animal")
        }

        val entity = AnimalInteresEntity(
            usuarioId = usuarioUuid,
            animalId = animalUuid
        )
        val saved = interesRepository.save(entity)
        logger.info("Interés registrado correctamente")
        return AnimalInteres(
            usuarioId = saved.usuarioId.toString(),
            animalId = saved.animalId.toString(),
            fecha = saved.fecha
        )
    }

    /**
     * Elimina el interés de un usuario en un animal.
     *
     * @param usuarioId ID del usuario autenticado
     * @param animalId ID del animal
     * @throws IllegalArgumentException si el interés no existe
     */
    fun eliminarInteres(usuarioId: String, animalId: String) {
        logger.info("Eliminando interés: usuario=$usuarioId, animal=$animalId")
        val animalUuid = UUID.fromString(animalId)
        val usuarioUuid = UUID.fromString(usuarioId)

        if (!interesRepository.existsByUsuarioIdAndAnimalId(usuarioUuid, animalUuid)) {
            logger.warn("No existe interés para eliminar: usuario=$usuarioId, animal=$animalId")
            throw IllegalArgumentException("No tienes interés registrado en este animal")
        }

        interesRepository.deleteById(AnimalInteresId(usuarioId = usuarioUuid, animalId = animalUuid))
        logger.info("Interés eliminado correctamente")
    }

    /**
     * Obtiene la lista de animales en los que un usuario ha manifestado interés.
     *
     * @param usuarioId ID del usuario autenticado
     * @return Lista de AnimalInteresResponse con los datos del animal y la fecha de interés
     */
    fun listarIntereses(usuarioId: String): List<AnimalInteresResponse> {
        logger.info("Listando intereses del usuario: $usuarioId")
        val usuarioUuid = UUID.fromString(usuarioId)
        val intereses = interesRepository.findByUsuarioId(usuarioUuid)

        return intereses.mapNotNull { interes ->
            val animal = animalRepository.findById(interes.animalId).orElse(null) ?: return@mapNotNull null
            AnimalInteresResponse(
                animalId = animal.id.toString(),
                nombre = animal.nombre,
                especie = animal.especie,
                raza = animal.raza,
                fechaNacimiento = animal.fechaNacimiento,
                sexo = animal.sexo.name,
                descripcion = animal.descripcion,
                estatus = animal.estatus.name,
                esterilizado = animal.esterilizado,
                fechaInteres = interes.fecha
            )
        }
    }
}
