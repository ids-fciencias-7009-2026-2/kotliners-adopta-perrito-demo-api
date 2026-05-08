package com.kotliners.adoptaPerrito.dto.response

import com.kotliners.adoptaPerrito.domain.Animal
import java.time.LocalDate
import java.time.LocalDateTime

/** DTO de respuesta basico para listados de animales. */
class AnimalResponse(
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

/** DTO de respuesta detallado — incluye fotos, vacunas y padecimientos. */
class AnimalDetalleResponse(
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
    val fechaRegistro: LocalDateTime,
    /** URLs de las fotos del animal. */
    val fotos: List<String>,
    /** Nombres de las vacunas aplicadas. */
    val vacunas: List<String>,
    /** Nombres de los padecimientos o condiciones medicas. */
    val padecimientos: List<String>
)

/**
 * Convierte un Animal de dominio a AnimalResponse (listado).
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

/**
 * Convierte un Animal de dominio a AnimalDetalleResponse con datos enriquecidos.
 */
fun Animal.toAnimalDetalleResponse(
    fotos: List<String>,
    vacunas: List<String>,
    padecimientos: List<String>
) = AnimalDetalleResponse(
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
    fechaRegistro = this.fechaRegistro,
    fotos = fotos,
    vacunas = vacunas,
    padecimientos = padecimientos
)
