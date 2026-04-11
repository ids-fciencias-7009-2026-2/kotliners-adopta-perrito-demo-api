package com.kotliners.adoptaPerrito.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

/**
 * Entidad JPA que representa la tabla "codigo_postal" en la base de datos.
 */
@Entity
@Table(name = "codigo_postal")
data class CodigoPostalEntity(

    /** Código postal de 5 dígitos (PK) */
    @Id
    @Column(name = "codigo_postal", length = 5)
    val codigoPostal: String,

    /** Latitud geográfica (mock si no se conoce) */
    @Column(name = "latitud", nullable = false, precision = 10, scale = 6)
    val latitud: BigDecimal = BigDecimal("19.432608"),

    /** Longitud geográfica (mock si no se conoce) */
    @Column(name = "longitud", nullable = false, precision = 10, scale = 6)
    val longitud: BigDecimal = BigDecimal("-99.133209")
)
