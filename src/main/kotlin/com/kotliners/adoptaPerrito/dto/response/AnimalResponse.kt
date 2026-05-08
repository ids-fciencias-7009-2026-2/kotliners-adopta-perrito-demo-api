package com.kotliners.adoptaPerrito.dto.response

import com.kotliners.adoptaPerrito.domain.Animal
import java.time.LocalDate
import java.time.LocalDateTime

class AnimalResponse (

    val id: String,
    val nombre: String,
    val especie: String,
    val raza: String?,
    val fechaNacimiento: LocalDate,
    val sexo: String,
    val descripcion: String,
    val estatus: String,
    val esterilizado: Boolean,
    val usuarioId: String,
    val fechaRegistro: LocalDateTime
)

/**
 * Convierte un Animal de dominio a AnimalResponse.
 */
fun Animal.toAnimalResponse() = AnimalResponse(
    id = this.id ?: "",
    nombre = this.nombre,
    especie = this.especie,
    raza = this.raza,
    fechaNacimiento = this.fechaNacimiento,
    sexo = this.sexo.name,
    descripcion = this.descripcion,
    estatus = this.estatus.name,
    esterilizado = this.esterilizado,
    usuarioId = this.usuarioId,
    fechaRegistro = this.fechaRegistro
)