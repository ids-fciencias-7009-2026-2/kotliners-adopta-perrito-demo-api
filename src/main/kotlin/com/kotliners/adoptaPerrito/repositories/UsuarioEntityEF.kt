package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.domain.Usuario
import com.kotliners.adoptaPerrito.entities.UsuarioEntity


/**
 * Función de extensión que convierte un Usuario de dominio a un UsuarioEntity para persistencia.
 */
fun Usuario.toUsuarioEntity(): UsuarioEntity {
    return UsuarioEntity(
        id = this.id,
        curp = this.curp,
        username = this.username,
        rol = this.rol,
        fotoPerfil = this.fotoPerfil,
        nombres = this.nombre,
        apellidoPaterno = this.apellidoPaterno,
        apellidoMaterno = this.apellidoMaterno,
        email = this.email,
        codigoPostal = this.cp,
        password = this.password
    )
}