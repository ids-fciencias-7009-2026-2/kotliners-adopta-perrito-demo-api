package com.kotliners.adoptaPerrito.dto.request

import jakarta.validation.constraints.NotBlank

/**
 * DTO utilizado para solicitar la eliminación de un animal.
 */
data class DeleteAnimalRequest(

    /** Identificador único del animal a eliminar. */
    @field:NotBlank(message = "Por favor, ingresa el id del animal a eliminar.")
    val animalId: String
)
