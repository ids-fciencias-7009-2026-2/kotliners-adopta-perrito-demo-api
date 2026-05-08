package com.kotliners.adoptaPerrito.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.UUID

/**
 * Entidad JPA para la tabla foto_animal.
 * Almacena las URLs de fotos asociadas a un animal.
 */
@Entity
@Table(name = "foto_animal")
data class FotoAnimalEntity(

    @Id
    @UuidGenerator
    @Column(name = "foto_id", updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(name = "animal_id", nullable = false, updatable = false)
    val animalId: UUID = UUID.randomUUID(),

    /** URL o path de la foto */
    @Column(name = "foto", nullable = false)
    val foto: String = "",

    @Column(name = "fecha", nullable = false)
    val fecha: LocalDateTime = LocalDateTime.now()
)
