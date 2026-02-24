package com.kotliners.adoptaPerrito.domain

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
     * Correo electrónico del usuario.
     */
    var email: String,

    /**
     * Código postal del usuario.
     */
    var cp: String,

    /**
     * Contraseña del usuario.
     */
    var password : String,

    )
