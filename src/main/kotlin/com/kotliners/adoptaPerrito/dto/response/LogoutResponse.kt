package com.kotliners.adoptaPerrito.dto.response

/**
 * DTO utilizado para enviar informacion al cliente cuando un
 * usuario cierra sesion en el sistema.
 */
data class LogoutResponse(

    /**
     * Identificador del usuario que cerro sesión.
     */
    val userId: String?,

    /**
     * Fecha y hora en que se realizó el logout.
     */
    val logoutDateTime: String
)
