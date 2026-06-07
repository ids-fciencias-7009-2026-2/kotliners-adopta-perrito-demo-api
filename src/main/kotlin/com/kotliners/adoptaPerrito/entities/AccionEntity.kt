package com.kotliners.adoptaPerrito.entities

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "accion")
data class AccionEntity(

    @Id
    @UuidGenerator
    @Column(name = "act_id", updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(name = "usuario_id")
    val usuarioId: UUID? = null,

    @Column(name = "accion", nullable = false)
    val accion: String = "",

    @Column(name = "fecha")
    val fecha: LocalDateTime = LocalDateTime.now()
)
