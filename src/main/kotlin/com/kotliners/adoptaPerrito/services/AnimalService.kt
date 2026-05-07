package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.domain.Animal
import com.kotliners.adoptaPerrito.domain.toAnimal
import com.kotliners.adoptaPerrito.entities.AnimalEntity
import com.kotliners.adoptaPerrito.repositories.AnimalRepository
import com.kotliners.adoptaPerrito.repositories.toAnimalEntity

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.UUID
import java.time.LocalDateTime

/**
 * Servicio de dominio para gestionar operaciones relacionadas con animales.
 */
@Service
class AnimalService {

    /* Logger para registrar información relevante durante la ejecución de las operaciones del servicio */
    private val logger: Logger = LoggerFactory.getLogger(AnimalService::class.java)

    /** Repositorio para acceder a los datos de los animales */
    @Autowired
    lateinit var animalRepository: AnimalRepository

    /** 
     * TODO: Crea un nuevo animal y lo persiste en la base de datos
     */
    // fun addNewAnimal(animal: Animal): Animal {
    // }

    /** 
     * TODO: Lista todos los animales 
     */
    // fun searchAllAnimals(): List<Animal> {
    // }

    /** 
     * Busca un animal por su ID y lo devuelve como un objeto de dominio. 
     * 
     * @param id Identificador del animal a buscar
     * @return El animal encontrado o null si no existe
     */
    fun getAnimalById(id: String): Animal? {
        logger.info("Buscando animal por ID: $id")
        var uuid = UUID.fromString(id)
        val entity = animalRepository.findById(uuid).orElse(null)
        if (entity == null) {
            logger.warn("No se encontró el animal con ID: $id")
            return null
        }
        return entity.toAnimal()
    }

    /** 
     * TODO: Actualiza campos de un animal existente
     */
    // fun updateAnimal(id: String, updates: Animal): Animal? {
    // }

    /** 
     * Elimina un animal por su ID. 
     * 
     * @param id Identificador del animal a eliminar
     * @return true si el animal fue eliminado, false si no se encontró el animal
     */
    fun deleteAnimal(id: String): Boolean {
        logger.info("Eliminando animal por ID: $id")
        val uuid = UUID.fromString(id)
        if (!animalRepository.existsById(uuid)) {
            logger.warn("No existe el animal con ID: $id")
            return false
        }
        animalRepository.deleteById(uuid)
        return true
    }

    /** 
     * TODO: Lista animales por dueño 
     */
    // fun listByOwner(usuarioId: String): List<Animal> {
    // }

}
