package com.kotliners.adoptaPerrito.dto.response

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DTO de respuesta que representa un animal en la lista de intereses del usuario.
 * Incluye los datos del animal para mostrar en la vista "Mis favoritos".
 */
data class AnimalInteresResponse(
    /** ID del animal */
    val animalId: String,
    /** Nombre del animal */
    val nombre: String,
    /** Especie del animal */
    val especie: String,
    /** Raza del animal (opcional) */
    val raza: String?,
    /** Fecha de nacimiento del animal */
    val fechaNacimiento: LocalDate,
    /** Sexo del animal */
    val sexo: String,
    /** Descripción del animal */
    val descripcion: String,
    /** Estatus del animal */
    val estatus: String,
    /** Indica si el animal está esterilizado */
    val esterilizado: Boolean,
    /** Fecha en que el usuario manifestó interés */
    val fechaInteres: LocalDateTime
)
