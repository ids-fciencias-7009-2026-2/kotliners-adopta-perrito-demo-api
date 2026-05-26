package com.kotliners.adoptaPerrito.dto.request

import com.kotliners.adoptaPerrito.domain.Sexo
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

/**
 * DTO para recibir los datos de un nuevo animal desde el frontend.
 */
class CreateAnimalRequest (

    @field:NotBlank(message = "Por favor, ingresa el nombre del animal.")
    val nombre: String,

    /** Solo se aceptan "Perro" o "Gato" */
    @field:NotBlank(message = "Por favor, ingresa la especie del animal.")
    @field:Pattern(regexp = "(?i)perro|gato", message = "La especie debe ser Perro o Gato.")
    val especie: String,

    /* Raza del animal (texto libre, opcional — se mantiene para compatibilidad) */
    val raza: String? = null,

    /* ID de la raza en el catalogo (FK opcional) */
    val razaId: String? = null,

    /* Fecha de nacimiento del animal */
    @field:NotNull(message = "Por favor, ingresa la fecha de nacimiento.")
    val fechaNacimiento: LocalDate,

    /* Sexo del animal */
    @field:NotNull(message = "Por favor, indica el sexo del animal.")
    val sexo: Sexo,

    /* Descripción del animal */
    @field:NotBlank(message = "Por favor, ingresa una descripción.")
    val descripcion: String,

    /* Indica si el animal está esterilizado */
    @field:NotNull(message = "Por favor, indica si está esterilizado.")
    val esterilizado: Boolean
)