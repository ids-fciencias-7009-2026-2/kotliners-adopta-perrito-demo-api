package com.kotliners.adoptaPerrito.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

/**
 * Entidad JPA para la tabla padecimiento.
 * Catalogo de padecimientos o condiciones medicas.
 */
@Entity
@Table(name = "padecimiento")
data class PadecimientoEntity(

    @Id
    @UuidGenerator
    @Column(name = "padecimiento_id", updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(name = "nombre", nullable = false, unique = true)
    val nombre: String = ""
)
