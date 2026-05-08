package com.kotliners.adoptaPerrito.dto.response

/**
 * DTO de respuesta con los datos publicos del usuario.
 * No expone campos sensibles como password o token.
 */
data class UsuarioResponse(
    val id: String?,
    val curp: String,
    val username: String,
    val rol: String,
    val fotoPerfil: String?,
    val nombres: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val email: String,
    val codigoPostal: String
)
