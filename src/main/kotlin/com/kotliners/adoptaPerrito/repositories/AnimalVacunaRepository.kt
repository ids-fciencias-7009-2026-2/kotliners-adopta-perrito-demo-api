package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.AnimalVacunaEntity
import com.kotliners.adoptaPerrito.entities.AnimalVacunaId
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/** Repositorio JPA para la relacion animal-vacuna. */
@Repository
interface AnimalVacunaRepository : CrudRepository<AnimalVacunaEntity, AnimalVacunaId> {

    @Query("select av from AnimalVacunaEntity av where av.animalId = :animalId")
    fun findByAnimalId(animalId: UUID): List<AnimalVacunaEntity>

    fun deleteByAnimalId(animalId: UUID)
}
