package com.kotliners.adoptaPerrito.dto.request

import jakarta.validation.constraints.NotBlank

data class CreatePadecimientoRequest(
    @field:NotBlank(message = "El nombre del padecimiento es obligatorio.")
    val nombre: String
)