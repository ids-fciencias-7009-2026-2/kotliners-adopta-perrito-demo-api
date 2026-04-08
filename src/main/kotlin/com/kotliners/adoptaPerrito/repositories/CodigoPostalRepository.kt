package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.CodigoPostalEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

/**
 * Repositorio JPA para la entidad CodigoPostalEntity.
 * Permite buscar y guardar códigos postales.
 */
@Repository
interface CodigoPostalRepository : CrudRepository<CodigoPostalEntity, String>
