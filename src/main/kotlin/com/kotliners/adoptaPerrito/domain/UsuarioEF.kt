package com.kotliners.adoptaPerrito.domain

import com.kotliners.adoptaPerrito.entities.UsuarioEntity
import com.kotliners.adoptaPerrito.dto.request.CreateUsuarioRequest

/**
 * Función de extensión que transforma un CreateUsuarioRequest en un Usuario de dominio.
 */
fun CreateUsuarioRequest.toUsuario(): Usuario {
    return Usuario(
        id = null,
        curp = this.curp.uppercase(),
        username = this.username,
        rol = this.rol,
        fotoPerfil = this.fotoPerfil,
        nombres = this.nombres,
        apellidoPaterno = this.apellidoPaterno,
        apellidoMaterno = this.apellidoMaterno,
        email = this.email,
        codigoPostal = this.codigoPostal,
        password = this.password
    )
}

/**
 * Función de extensión que convierte un UsuarioEntity a un Usuario de dominio.
 */
fun UsuarioEntity.toUsuario(): Usuario {
    return Usuario(
        id = this.id?.toString(),  
        curp = this.curp,
        username = this.username,
        rol = this.rol,
        fotoPerfil = this.fotoPerfil,
        nombres = this.nombres,
        apellidoPaterno = this.apellidoPaterno,
        apellidoMaterno = this.apellidoMaterno,
        email = this.email,
        codigoPostal = this.codigoPostal,
        password = this.password,
        token = this.token
    )
}
