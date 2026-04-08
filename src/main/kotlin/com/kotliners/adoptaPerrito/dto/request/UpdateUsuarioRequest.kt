package com.kotliners.adoptaPerrito.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

import org.hibernate.validator.constraints.URL

/**
 * DTO utilizado para actualizar los datos editables de un usuario autenticado.
 * No permite cambiar curp, username, rol ni password desde este endpoint.
 */
data class UpdateUsuarioRequest(

    /** Nombre(s) del usuario */
    @field:NotBlank(message = "Por favor, ingresa tu nombre.")
    val nombres: String,

    /** Apellido paterno */
    @field:NotBlank(message = "Por favor, ingresa tu apellido paterno.")
    val apellidoPaterno: String,

    /** Apellido materno */
    @field:NotBlank(message = "Por favor, ingresa tu apellido materno.")
    val apellidoMaterno: String,

    /** Correo electrónico */
    @field:NotBlank(message = "Por favor, ingresa tu correo electrónico.")
    @field:Email(message = "Por favor, ingresa un correo electrónico válido.")
    val email: String,

    /** Código postal */
    @field:NotBlank(message = "Por favor, ingresa tu código postal.")
    @field:Pattern(regexp = "^\\d{5}$", message = "Por favor, ingresa un código postal válido.")
    val codigoPostal: String,

    /** URL de la foto de perfil (opcional) */
    @field:URL(message = "Por favor, ingresa una URL válida para la foto de perfil.")
    val fotoPerfil: String? = null
)
