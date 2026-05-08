package com.kotliners.adoptaPerrito.dto.response

import java.time.LocalDateTime

/**
 * DTO de respuesta para el cuidador que lista los adoptantes interesados en sus animales.
 */
data class InteresRecibidoResponse(
    /** ID del animal en el que se manifesto interes. */
    val animalId: String,
    /** Nombre del animal. */
    val nombreAnimal: String,
    /** ID del adoptante que manifesto interes. */
    val adoptanteId: String,
    /** Nombre completo del adoptante. */
    val nombreAdoptante: String,
    /** Correo electronico del adoptante para contacto. */
    val emailAdoptante: String,
    /** Fecha en que se registro el interes. */
    val fechaInteres: LocalDateTime
)
