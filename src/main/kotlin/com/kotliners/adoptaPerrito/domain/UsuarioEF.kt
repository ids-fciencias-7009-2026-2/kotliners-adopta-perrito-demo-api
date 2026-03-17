package com.kotliners.adoptaPerrito.domain

import com.kotliners.adoptaPerrito.dto.request.CreateUsuarioRequest
import java.time.LocalDate
import java.util.UUID

/**
 * Función de extensión que transforma un CreateUsuarioRequest
 * en una instancia de Usuario, generando un identificador único.
 */
fun CreateUsuarioRequest.toUsuario(): Usuario {

    // Creamos el objeto de dominio usando los datos del DTO y un id aleatorio
    return Usuario(
        id = "id-random-" + UUID.randomUUID().toString(),
        nombre = this.nombre,
        apellidoPaterno = this.apellidoPaterno,
        apellidoMaterno = this.apellidoMaterno,
        email = this.email,
        cp = this.cp,
        fechaNacimiento = LocalDate.parse(this.fechaNacimiento),
        password = this.password
    )
}

