package com.kotliners.adoptaPerrito.dto.response

import com.kotliners.adoptaPerrito.domain.Usuario

/**
 * Convierte un Usuario de dominio a UsuarioResponse (DTO de respuesta).
 * Excluye campos sensibles como password y token.
 */
fun Usuario.toUsuarioResponse(): UsuarioResponse {
    return UsuarioResponse(
        id = this.id,
        curp = this.curp,
        username = this.username,
        rol = this.rol.name,
        fotoPerfil = this.fotoPerfil,
        nombres = this.nombres,
        apellidoPaterno = this.apellidoPaterno,
        apellidoMaterno = this.apellidoMaterno,
        email = this.email,
        codigoPostal = this.codigoPostal
    )
}
