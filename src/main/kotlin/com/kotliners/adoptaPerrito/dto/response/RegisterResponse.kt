package com.kotliners.adoptaPerrito.dto.response

import com.kotliners.adoptaPerrito.domain.Usuario

/**
 * DTO utilizado para enviar información al cliente cuando un
 * usuario se registra exitosamente en el sistema.
 */
data class RegisterResponse(

    /**
     * Usuario que fue registrado en el sistema.
     */
    val usuario: Usuario,

    /**
     * Mensaje informativo sobre el resultado del registro.
     */
    val mensaje: String = "Usuario registrado exitosamente"
)