package com.kotliners.adoptaPerrito.dto.request

data class UpdateUsuarioRequest(
    var email: String,
    var password: String?
)
