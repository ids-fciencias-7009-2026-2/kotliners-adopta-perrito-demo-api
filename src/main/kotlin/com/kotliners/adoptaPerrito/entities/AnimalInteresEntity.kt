package com.kotliners.adoptaPerrito.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

/**
 * Clave compuesta para la entidad AnimalInteresEntity.
 * La PK es (usuarioId, animalId).
 */
data class AnimalInteresId(
    val usuarioId: UUID = UUID.randomUUID(),
    val animalId: UUID = UUID.randomUUID()
) : Serializable

/**
 * Entidad JPA que representa la tabla "usuario_interes".
 * Registra el interés de un usuario en un animal específico.
 */
@Entity
@Table(name = "usuario_interes")
@IdClass(AnimalInteresId::class)
data class AnimalInteresEntity(

    /** ID del usuario que manifestó interés */
    @Id
    @Column(name = "usuario_id", nullable = false)
    val usuarioId: UUID,

    /** ID del animal en el que se manifestó interés */
    @Id
    @Column(name = "animal_id", nullable = false)
    val animalId: UUID,

    /** Fecha en que se registró el interés */
    @Column(name = "fecha", nullable = false)
    val fecha: LocalDateTime = LocalDateTime.now()
)
