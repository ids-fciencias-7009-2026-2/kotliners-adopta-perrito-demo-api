package com.kotliners.adoptaPerrito.dto.request

import com.kotliners.adoptaPerrito.domain.Rol   

/**
 * DTO utilizado para recibir los datos necesarios para crear un nuevo usuario en el sistema.
 */
data class CreateUsuarioRequest(
    /**
     * Nombre del usuario enviado por el cliente.
     */
    val nombre: String,

    /**
     * CURP del usuario enviado por el cliente.
     */
    val curp: String,

    /**
     * Nombre de usuario enviado por el cliente.
     */
    val username: String,

    /**
    * Rol del usuario (cuidador o adoptante) enviado por el cliente.
    */
    val rol: Rol,

    /**
    * URL de la foto de perfil enviada por el cliente.
    */
    val fotoPerfil: String? = null,

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
     * Contraseña enviada por el cliente.
     */
    val password: String
)
