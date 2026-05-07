package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.AnimalInteresEntity
import com.kotliners.adoptaPerrito.entities.AnimalInteresId
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Repositorio JPA para la entidad AnimalInteresEntity (tabla usuario_interes).
 */
@Repository
interface AnimalInteresRepository : CrudRepository<AnimalInteresEntity, AnimalInteresId> {

    /**
     * Obtiene todos los intereses de un usuario específico.
     * @param usuarioId ID del usuario
     */
    @Query("select i from AnimalInteresEntity i where i.usuarioId = :usuarioId")
    fun findByUsuarioId(usuarioId: UUID): List<AnimalInteresEntity>

    /**
     * Verifica si un usuario ya manifestó interés en un animal.
     * @param usuarioId ID del usuario
     * @param animalId ID del animal
     */
    @Query("select count(i) > 0 from AnimalInteresEntity i where i.usuarioId = :usuarioId and i.animalId = :animalId")
    fun existsByUsuarioIdAndAnimalId(usuarioId: UUID, animalId: UUID): Boolean
}
