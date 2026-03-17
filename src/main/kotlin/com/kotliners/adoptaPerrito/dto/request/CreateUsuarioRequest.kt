package com.kotliners.adoptaPerrito.dto.request

/**
 * DTO utilizado para recibir los datos necesarios para crear un nuevo usuario en el sistema.
 */
data class CreateUsuarioRequest(
    /**
     * Nombre del usuario enviado por el cliente.
     */
    val nombre: String,

    /**
     * Apellido paterno del usuario enviado por el cliente.
     */
    val apellidoPaterno: String,

    /**
     * Apellido materno del usuario enviado por el cliente.
     */
    val apellidoMaterno: String,

    /**
     * Correo electrónico enviado por el cliente.
     */
    val email: String,

    /**
     * Código postal enviado por el cliente.
     */
    val cp: String,     

    /**
    * Fecha de nacimiento enviada por el cliente.
    */
    val fechaNacimiento: String,

    /**
     * Contraseña enviada por el cliente.
     */
    val password: String
)
