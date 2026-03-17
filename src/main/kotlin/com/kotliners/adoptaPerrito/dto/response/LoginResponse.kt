package com.kotliners.adoptaPerrito.dto.response

/**
 * DTO que representa la respuesta de un login exitoso.
 *
 * @param token Token de sesión generado para el usuario autenticado.
 */
data class LoginResponse(
    /**
     * Token de sesión creado después de una autenticación satisfactoria.
     * Este identificador representa la sesión activa del usuario y se
     * emplea comúnmente para validar permisos en peticiones siguientes.
     */
    val token: String
)
