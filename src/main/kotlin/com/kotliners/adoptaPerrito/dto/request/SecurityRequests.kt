package com.kotliners.adoptaPerrito.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class VerifyEmailRequest(
    @field:NotBlank(message = "Token requerido.")
    val token: String
)

data class PasswordResetRequest(
    @field:NotBlank(message = "Por favor, ingresa tu correo electronico.")
    @field:Email(message = "Por favor, ingresa un correo electronico valido.")
    val email: String
)

data class PasswordResetConfirmRequest(
    @field:NotBlank(message = "Token requerido.")
    val token: String,

    @field:NotBlank(message = "Por favor, ingresa una contrasena.")
    @field:Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres.")
    val newPassword: String
)

data class TwoFactorVerifyRequest(
    @field:NotBlank(message = "Por favor, ingresa tu correo electronico.")
    @field:Email(message = "Por favor, ingresa un correo electronico valido.")
    val email: String,

    @field:NotBlank(message = "Codigo requerido.")
    @field:Pattern(regexp = "^\\d{6}$", message = "El codigo 2FA debe tener 6 digitos.")
    val code: String
)
