package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.FotoAnimalEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/** Repositorio JPA para fotos de animales. */
@Repository
interface FotoAnimalRepository : CrudRepository<FotoAnimalEntity, UUID> {

    @Query("select f from FotoAnimalEntity f where f.animalId = :animalId order by f.fecha asc")
    fun findByAnimalId(animalId: UUID): List<FotoAnimalEntity>

    fun deleteByAnimalId(animalId: UUID)
}
