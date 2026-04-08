package com.kotliners.adoptaPerrito.dto.request

import com.kotliners.adoptaPerrito.domain.Rol   

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

import org.hibernate.validator.constraints.URL

/**
 * DTO utilizado para recibir los datos necesarios para crear un nuevo usuario en el sistema.
 */
data class CreateUsuarioRequest(
    /**
     * Nombre del usuario enviado por el cliente.
     */
    @field:NotBlank(message = "Por favor, ingresa tu nombre.")
    val nombres: String,

    /**
     * CURP del usuario enviado por el cliente.
     */
    @field:NotBlank(message = "Por favor, ingresa tu CURP.")
    @field:Pattern(regexp = "^[A-Z0-9]{18}$", message = "Por favor, ingresa un CURP válido.")
    val curp: String,

    /**
     * Nombre de usuario enviado por el cliente.
     */
    @field:NotBlank(message = "Por favor, ingresa un nombre de usuario.")
    val username: String,

    /**
    * Rol del usuario (cuidador o adoptante) enviado por el cliente.
    */
    @field:NotNull(message = "Por favor, selecciona un rol.")
    val rol: Rol,

    /**
    * URL de la foto de perfil enviada por el cliente.
    */
    @field:URL(message = "Por favor, ingresa una URL válida para la foto de perfil.")
    val fotoPerfil: String? = null,

    /**
     * Apellido paterno del usuario enviado por el cliente.
     */
    @field:NotBlank(message = "Por favor, ingresa tu apellido paterno.")
    val apellidoPaterno: String,

    /**
     * Apellido materno del usuario enviado por el cliente.
     */
    @field:NotBlank(message = "Por favor, ingresa tu apellido materno.")
    val apellidoMaterno: String,

    /**
     * Correo electrónico enviado por el cliente.
     */
    @field:NotBlank(message = "Por favor, ingresa tu correo electrónico.")
    @field:Email(message = "Por favor, ingresa un correo electrónico válido.")
    val email: String,

    /**
     * Código postal enviado por el cliente.
     */
    @field:NotBlank(message = "Por favor, ingresa tu código postal.")
    @field:Pattern(regexp = "^\\d{5}$", message = "Por favor, ingresa un código postal válido.")
    val codigoPostal: String,     

    /**
     * Contraseña enviada por el cliente.
     */
    @field:NotBlank(message = "Por favor, ingresa una contraseña.")
    @field:Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    val password: String
)
