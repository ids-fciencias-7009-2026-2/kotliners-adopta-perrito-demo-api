package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.RazaEntity
import org.springframework.data.repository.CrudRepository
import java.util.UUID

/** Repositorio JPA para la entidad RazaEntity. */
interface RazaRepository : CrudRepository<RazaEntity, UUID> {

    /** Lista todas las razas de una especie ordenadas por nombre en espanol */
    fun findAllByEspecieOrderByNombreEs(especie: String): List<RazaEntity>

    /** Busca una raza por su nombre en ingles y especie */
    fun findByEspecieAndNombreEn(especie: String, nombreEn: String): RazaEntity?
}
