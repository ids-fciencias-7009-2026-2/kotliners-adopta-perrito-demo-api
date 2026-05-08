package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.AnimalPadecimientoEntity
import com.kotliners.adoptaPerrito.entities.AnimalPadecimientoId
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/** Repositorio JPA para la relacion animal-padecimiento. */
@Repository
interface AnimalPadecimientoRepository : CrudRepository<AnimalPadecimientoEntity, AnimalPadecimientoId> {

    @Query("select ap from AnimalPadecimientoEntity ap where ap.animalId = :animalId")
    fun findByAnimalId(animalId: UUID): List<AnimalPadecimientoEntity>

    fun deleteByAnimalId(animalId: UUID)
}
