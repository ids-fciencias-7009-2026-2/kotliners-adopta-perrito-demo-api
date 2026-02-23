package com.kotliners.adoptaPerrito.dto.request

data class CreateUsuarioRequest(
    val nombre: String,
    val email: String,
    val postalCode: String,
)
