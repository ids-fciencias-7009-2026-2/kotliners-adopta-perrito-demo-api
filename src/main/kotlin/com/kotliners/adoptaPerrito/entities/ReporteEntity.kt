package com.kotliners.adoptaPerrito.entities

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.UUID

enum class ReporteEstado {
    PENDIENTE, RESUELTO, DESESTIMADO
}

@Entity
@Table(name = "reporte")
data class ReporteEntity(

    @Id
    @UuidGenerator
    @Column(name = "reporte_id", updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(name = "usuario_id", nullable = false)
    val usuarioId: UUID = UUID.randomUUID(),

    @Column(name = "animal_id", nullable = false)
    val animalId: UUID = UUID.randomUUID(),

    @Column(name = "motivo", nullable = false)
    val motivo: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    var estado: ReporteEstado = ReporteEstado.PENDIENTE,

    @Column(name = "fecha")
    val fecha: LocalDateTime = LocalDateTime.now(),

    @Column(name = "fecha_resolucion")
    var fechaResolucion: LocalDateTime? = null
)
