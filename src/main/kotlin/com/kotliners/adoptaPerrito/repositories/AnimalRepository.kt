package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.AnimalEntity
import org.springframework.data.repository.CrudRepository
import java.util.UUID

/**
 * Repositorio JPA para la entidad AnimalEntity.
 */
interface AnimalRepository: CrudRepository<AnimalEntity, UUID> {

    /** Encuentra todos los animales por el ID del dueño */
    fun findAllByUsuarioId(usuarioId: UUID): Iterable<AnimalEntity>

}
