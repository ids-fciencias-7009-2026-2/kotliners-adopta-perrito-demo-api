package com.kotliners.adoptaPerrito.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.util.UUID

/** Clave compuesta para AnimalPadecimientoEntity. */
data class AnimalPadecimientoId(
    val animalId: UUID = UUID.randomUUID(),
    val padecimientoId: UUID = UUID.randomUUID()
) : Serializable

/**
 * Entidad JPA para la tabla animal_padecimiento (relacion muchos a muchos).
 */
@Entity
@Table(name = "animal_padecimiento")
@IdClass(AnimalPadecimientoId::class)
data class AnimalPadecimientoEntity(

    /* ID del animal */
    @Id
    @Column(name = "animal_id", nullable = false, updatable = false)
    val animalId: UUID = UUID.randomUUID(),

    /* ID del padecimiento */
    @Id
    @Column(name = "padecimiento_id", nullable = false, updatable = false)
    val padecimientoId: UUID = UUID.randomUUID()
)
