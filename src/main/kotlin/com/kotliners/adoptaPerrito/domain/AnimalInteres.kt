package com.kotliners.adoptaPerrito.domain

import java.time.LocalDateTime

/**
 * Modelo de dominio que representa el interés de un usuario en un animal.
 */
data class AnimalInteres(
    /** ID del usuario que manifestó interés */
    val usuarioId: String,
    /** ID del animal en el que se manifestó interés */
    val animalId: String,
    /** Fecha en que se registró el interés */
    val fecha: LocalDateTime = LocalDateTime.now()
)
