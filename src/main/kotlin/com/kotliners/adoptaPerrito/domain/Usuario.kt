package com.kotliners.adoptaPerrito.domain

/**
 * Modelo de dominio que representa a un usuario del sistema de adopción.
 */
data class Usuario(

    /** Identificador único del usuario */
    val id: Int? = null,

    /** Clave Única de Registro de Población */
    var curp: String,

    /** Nombre de usuario único */
    var username: String,

    /** Rol del usuario: ADOPTANTE o CUIDADOR */
    var rol: Rol,

    /** URL de la foto de perfil (opcional) */
    var fotoPerfil: String? = null,

    /** Nombre(s) del usuario */
    var nombres: String,

    /** Apellido paterno */
    var apellidoPaterno: String,

    /** Apellido materno */
    var apellidoMaterno: String,

    /** Correo electrónico */
    var email: String,

    /** Código postal */
    var codigoPostal: String,

    /** Contraseña hasheada */
    var password: String,

    /** Token de sesión activo */
    var token: String? = null
)
