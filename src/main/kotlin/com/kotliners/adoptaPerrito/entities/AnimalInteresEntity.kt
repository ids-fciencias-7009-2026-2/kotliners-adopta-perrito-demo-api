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
 * Registra el interes de un usuario en un animal especifico.
 * Los valores por defecto permiten que Hibernate instancie la entidad sin argumentos.
 */
@Entity
@Table(name = "usuario_interes")
@IdClass(AnimalInteresId::class)
data class AnimalInteresEntity(

    /** ID del usuario que manifesto interes */
    @Id
    @Column(name = "usuario_id", nullable = false, updatable = false)
    val usuarioId: UUID = UUID.randomUUID(),

    /** ID del animal en el que se manifesto interes */
    @Id
    @Column(name = "animal_id", nullable = false, updatable = false)
    val animalId: UUID = UUID.randomUUID(),

    /** Fecha en que se registro el interes */
    @Column(name = "fecha", nullable = false)
    val fecha: LocalDateTime = LocalDateTime.now()
)
