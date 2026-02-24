package com.kotliners.adoptaPerrito.dto.request

/**
 * DTO utilizado para actualizar los datos de usuario en el sistema.
 */
data class UpdateUsuarioRequest(
    var cp: String,
    var email: String,
    var password: String?
)
