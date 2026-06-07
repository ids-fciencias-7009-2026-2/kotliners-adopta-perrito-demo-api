package com.kotliners.adoptaPerrito.dto.response

import java.time.LocalDateTime

/**
 * DTO de respuesta para el cuidador que lista los adoptantes interesados en sus animales.
 */
data class InteresRecibidoResponse(
    val animalId: String,
    val nombreAnimal: String,
    val adoptanteId: String,
    val nombreAdoptante: String,
    val emailAdoptante: String,
    val fotoAdoptante: String?,
    val fechaInteres: LocalDateTime
)
