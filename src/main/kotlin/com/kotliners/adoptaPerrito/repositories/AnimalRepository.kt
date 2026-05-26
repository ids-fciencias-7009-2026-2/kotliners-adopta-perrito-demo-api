package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.AnimalEntity
import org.springframework.data.repository.CrudRepository
import com.kotliners.adoptaPerrito.domain.Estatus
import java.util.UUID

/**
 * Repositorio JPA para la entidad AnimalEntity.
 */
interface AnimalRepository: CrudRepository<AnimalEntity, UUID> {

    /** Encuentra todos los animales por el ID del dueño */
    fun findAllByUsuarioId(usuarioId: UUID): Iterable<AnimalEntity>

    /**
     * Retorna animales de un cuidador filtrados por estatus.
     */
        fun findAllByUsuarioIdAndEstatus(usuarioId: UUID, estatus: Estatus): List<AnimalEntity>
}
