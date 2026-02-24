package com.kotliners.adoptaPerrito.dto.request

/**
 * DTO utilizado para recibir las credenciales de un usuario
 * cuando intenta autenticarse en el sistema.
 */
data class LoginRequest(

    /**
     * Correo electrónico del usuario que intenta autenticarse.
     */
    val email: String,

    /**
     * Contraseña ingresada por el usuario.
     */
    val password: String
)
