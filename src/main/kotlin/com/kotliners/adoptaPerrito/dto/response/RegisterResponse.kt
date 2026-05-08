package com.kotliners.adoptaPerrito.dto.response

/**
 * DTO de respuesta para el registro exitoso de un usuario.
 * Usa UsuarioResponse para no exponer campos sensibles.
 */
data class RegisterResponse(
    /** Datos publicos del usuario registrado. */
    val usuario: UsuarioResponse,
    /** Mensaje informativo del resultado. */
    val mensaje: String = "Usuario registrado exitosamente"
)
