package com.kotliners.adoptaPerrito.dto.response

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DTO de respuesta que representa un animal en la lista de intereses del usuario.
 * Incluye los datos del animal para mostrar en la vista "Mis favoritos".
 */
data class AnimalInteresResponse(
    val animalId: String,
    val nombre: String,
    val especie: String,
    val raza: String?,
    val fechaNacimiento: LocalDate,
    val sexo: String,
    val descripcion: String,
    val estatus: String,
    val esterilizado: Boolean,
    val fechaInteres: LocalDateTime,
    /** Primera foto del animal para mostrar en tarjetas. */
    val fotoPortada: String? = null,
    /** Fecha de publicacion del animal. */
    val fechaRegistro: LocalDateTime? = null
)
