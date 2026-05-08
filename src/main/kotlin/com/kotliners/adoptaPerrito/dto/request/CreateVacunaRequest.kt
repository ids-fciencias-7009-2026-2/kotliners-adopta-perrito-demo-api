package com.kotliners.adoptaPerrito.dto.request

import jakarta.validation.constraints.NotBlank

data class CreateVacunaRequest(
    @field:NotBlank(message = "El nombre de la vacuna es obligatorio.")
    val nombre: String
)