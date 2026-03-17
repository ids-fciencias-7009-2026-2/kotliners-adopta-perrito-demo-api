package com.kotliners.adoptaPerrito.domain

import java.time.LocalDate

/**
 * Modelo de dominio que representa a un usuario del sistema de adopción.
 */
data class Usuario (

    /**
     * Identificador único del usuario.
     */
    val id: String,

    /**
     * Nombre del usuario.
     */
    var nombre: String,

    /**
     * Apellido paterno del usuario.
     */
    var apellidoPaterno: String,

    /**
     * Apellido materno del usuario.
     */
    var apellidoMaterno: String,

    /**
     * Correo electrónico del usuario.
     */
    var email: String,

    /**
     * Código postal del usuario.
     */
    var cp: String,

    /**
     * Fecha de nacimiento del usuario.
     */
    var fechaNacimiento: LocalDate,

    /**
     * Contraseña del usuario.
     */
    var password: String? = null

    )
