package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.VacunaEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/** Repositorio JPA para el catalogo de vacunas. */
@Repository
interface VacunaRepository : CrudRepository<VacunaEntity, UUID> {
    fun findByNombre(nombre: String): VacunaEntity?
}
