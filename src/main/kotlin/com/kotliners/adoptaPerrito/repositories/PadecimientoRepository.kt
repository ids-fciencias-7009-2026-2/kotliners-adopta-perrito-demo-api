package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.PadecimientoEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/** Repositorio JPA para el catalogo de padecimientos. */
@Repository
interface PadecimientoRepository : CrudRepository<PadecimientoEntity, UUID> {
    fun findByNombre(nombre: String): PadecimientoEntity?
}
