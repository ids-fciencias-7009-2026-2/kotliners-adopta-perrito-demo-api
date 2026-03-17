package com.kotliners.adoptaPerrito.domain

import com.kotliners.adoptaPerrito.entities.UsuarioEntity

import com.kotliners.adoptaPerrito.dto.request.CreateUsuarioRequest
import java.util.UUID

/**
 * Función de extensión que transforma un CreateUsuarioRequest
 * en una instancia de Usuario, generando un identificador único.
 */
fun CreateUsuarioRequest.toUsuario(): Usuario {

    // Creamos el objeto de dominio usando los datos del DTO y un id aleatorio
    return Usuario(
        id = null, // "id-random-" + UUID.randomUUID().toString(), 
        curp = this.curp.uppercase(),
        username = this.username,
        rol = this.rol,
        fotoPerfil = this.fotoPerfil,
        nombre = this.nombre,
        apellidoPaterno = this.apellidoPaterno,
        apellidoMaterno = this.apellidoMaterno,
        email = this.email,
        cp = this.cp,
        password = this.password
    )
}

/**
 * Función de extensión que convierte un UsuarioEntity a un Usuario de dominio.
 */
fun UsuarioEntity.toUsuario(): Usuario {
    return Usuario(
        id = this.id,
        curp = this.curp,
        username = this.username,
        rol = this.rol,
        fotoPerfil = this.fotoPerfil,
        nombre = this.nombres,
        apellidoPaterno = this.apellidoPaterno,
        apellidoMaterno = this.apellidoMaterno,
        email = this.email,
        cp = this.codigoPostal,
        password = this.password
    )
}