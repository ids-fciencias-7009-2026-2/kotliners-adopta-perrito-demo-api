package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.domain.Usuario
import com.kotliners.adoptaPerrito.entities.UsuarioEntity
import java.util.UUID

/**
 * Función de extensión que convierte un Usuario de dominio a un UsuarioEntity para persistencia.
 */
fun Usuario.toUsuarioEntity(): UsuarioEntity {
    return UsuarioEntity(
        id = this.id?.let { UUID.fromString(it) },  // String → UUID
        curp = this.curp,
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
