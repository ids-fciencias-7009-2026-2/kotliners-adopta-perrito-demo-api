package com.kotliners.adoptaPerrito.domain

import java.time.LocalDate

/**
 * Modelo de dominio que representa a un usuario del sistema de adopción.
 */
data class Usuario (

    /**
     * Identificador único del usuario.
     */
    val id: Int? = null,

    /**
     * CURP del usuario.
     */
    var curp: String,

    /**
     * Nombre de usuario (username).
     */
    var username: String,

    /**
     * Rol del usuario (cuidador o adoptante).
     */
    var rol: String,

    /**
     * URL de la foto de perfil.
     */
    var fotoPerfil: String? = null,

    /**
     * Nombre(s) del usuario.
     */
    var nombre: String,

    /**
     * Apellido paterno.
     */
    var apellidoPaterno: String,

    /**
     * Apellido materno.
     */
    var apellidoMaterno: String,

    /**
     * Correo electrónico.
     */
    var email: String,

    /**
     * Código postal.
     */
    var cp: String,

    /**
     * Contraseña.
     */
    var password: String? = null
)