package com.kotliners.adoptaPerrito.entities

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

/**
 * Entidad JPA que representa la tabla "raza".
 * Contiene el nombre en espanol (para mostrar al usuario) y en ingles
 * (para consultar la API externa de razas).
 */
@Entity
@Table(name = "raza", uniqueConstraints = [UniqueConstraint(columnNames = ["especie", "nombre_en"])])
data class RazaEntity(

    @Id
    @UuidGenerator
    @Column(name = "raza_id", updatable = false, nullable = false)
    val id: UUID? = null,

    /** PERRO o GATO */
    @Column(name = "especie", nullable = false, length = 10)
    val especie: String = "",

    /** Nombre en espanol para mostrar en el select */
    @Column(name = "nombre_es", nullable = false, length = 100)
    val nombreEs: String = "",

    /** Nombre en ingles para consultar la API externa */
    @Column(name = "nombre_en", nullable = false, length = 100)
    val nombreEn: String = ""
)
