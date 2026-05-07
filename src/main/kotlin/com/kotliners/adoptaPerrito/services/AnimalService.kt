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
     * TODO: Obtiene un animal por su ID */
    // fun getAnimalById(id: String): Animal? {
    // }

    /** 
     * TODO: Actualiza campos de un animal existente
     */
    // fun updateAnimal(id: String, updates: Animal): Animal? {
    // }

    /** 
     * TODO: Elimina un animal por su ID 
     */
    // fun deleteAnimal(id: String): Boolean {
    // }

    /** 
     * TODO: Lista animales por dueño 
     */
    // fun listByOwner(usuarioId: String): List<Animal> {
    // }

}
