package com.kotliners.adoptaPerrito.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.util.UUID

/** Clave compuesta para AnimalVacunaEntity. */
data class AnimalVacunaId(
    val animalId: UUID = UUID.randomUUID(),
    val vacunaId: UUID = UUID.randomUUID()
) : Serializable

/**
 * Entidad JPA para la tabla animal_vacuna (relacion muchos a muchos).
 */
@Entity
@Table(name = "animal_vacuna")
@IdClass(AnimalVacunaId::class)
data class AnimalVacunaEntity(

    @Id
    @Column(name = "animal_id", nullable = false, updatable = false)
    val animalId: UUID = UUID.randomUUID(),

    @Id
    @Column(name = "vacuna_id", nullable = false, updatable = false)
    val vacunaId: UUID = UUID.randomUUID()
)
