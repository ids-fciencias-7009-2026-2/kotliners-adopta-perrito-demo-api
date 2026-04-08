package com.kotliners.adoptaPerrito.dto.request

import com.kotliners.adoptaPerrito.domain.Rol

/**
 * DTO utilizado para actualizar los datos de usuario en el sistema.
 */
data class UpdateUsuarioRequest(

    /** Nombre(s) del usuario */
    val nombres: String,

    /** CURP del usuario */
    val curp: String,

    /** Nombre de usuario */
    val username: String,

    /** Rol del usuario */
    val rol: Rol,

    /** URL de la foto de perfil (opcional) */
    val fotoPerfil: String? = null,

    /** Apellido paterno */
    val apellidoPaterno: String,

    /** Apellido materno */
    val apellidoMaterno: String,

    /** Correo electrónico */
    val email: String,

    /** Código postal */
    val codigoPostal: String,

    /** Contraseña */
    val password: String
)
