package com.kotliners.adoptaPerrito.dto.request

import com.kotliners.adoptaPerrito.domain.Sexo
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

/**
 * DTO para recibir los datos de un nuevo animal desde el frontend.
 */
class CreateAnimalRequest (

    /* Nombre del animal */
    @field:NotBlank(message = "Por favor, ingresa el nombre del animal.")
    val nombre: String,

    /* Especie del animal (perro, gato, etc.) */
    @field:NotBlank(message = "Por favor, ingresa la especie del animal.")
    val especie: String,

    /* Raza del animal (opcional) */
    val raza: String? = null,

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