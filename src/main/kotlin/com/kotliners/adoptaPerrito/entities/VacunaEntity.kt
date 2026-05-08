package com.kotliners.adoptaPerrito.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

/**
 * Entidad JPA para la tabla vacuna.
 * Catalogo de vacunas disponibles.
 */
@Entity
@Table(name = "vacuna")
data class VacunaEntity(

    /* ID de la vacuna */
    @Id
    @UuidGenerator
    @Column(name = "vacuna_id", updatable = false, nullable = false)
    val id: UUID? = null,

    /* Nombre de la vacuna */
    @Column(name = "nombre", nullable = false, unique = true)
    val nombre: String = ""
)
