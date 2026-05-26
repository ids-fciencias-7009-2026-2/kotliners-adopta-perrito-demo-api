package com.kotliners.adoptaPerrito.dto.response

data class MessageResponse(
    val mensaje: String
)

data class TwoFactorRequiredResponse(
    val requiere2FA: Boolean = true,
    val mensaje: String = "Codigo 2FA enviado al correo registrado"
)
