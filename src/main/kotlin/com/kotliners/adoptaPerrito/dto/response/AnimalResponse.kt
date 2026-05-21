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
    val razaId: String?,
    val fechaNacimiento: LocalDate,
    val sexo: String,
    val descripcion: String,
    val estatus: String,
    val esterilizado: Boolean,
    val usuarioId: String,
    val fechaRegistro: LocalDateTime,
    val fotoPortada: String? = null,
    val numInteresados: Int = 0
)

/** DTO de respuesta detallado — incluye fotos, vacunas y padecimientos. */
class AnimalDetalleResponse(
    val id: String,
    val nombre: String,
    val especie: String,
    val raza: String?,
    val razaId: String?,
    val fechaNacimiento: LocalDate,
    val sexo: String,
    val descripcion: String,
    val estatus: String,
    val esterilizado: Boolean,
    val usuarioId: String,
    val fechaRegistro: LocalDateTime,
    val fotos: List<String>,
    val vacunas: List<String>,
    val padecimientos: List<String>,
    val numInteresados: Int = 0
)

/**
 * Convierte un Animal de dominio a AnimalResponse (listado).
 * Acepta opcionalmente la primera foto para mostrar en tarjetas.
 */
fun Animal.toAnimalResponse(fotoPortada: String? = null, numInteresados: Int = 0) = AnimalResponse(
    id = this.id ?: "",
    nombre = this.nombre,
    especie = this.especie,
    raza = this.raza,
    razaId = this.razaId,
    fechaNacimiento = this.fechaNacimiento,
    sexo = this.sexo.name,
    descripcion = this.descripcion,
    estatus = this.estatus.name,
    esterilizado = this.esterilizado,
    usuarioId = this.usuarioId,
    fechaRegistro = this.fechaRegistro,
    fotoPortada = fotoPortada,
    numInteresados = numInteresados
)

fun Animal.toAnimalDetalleResponse(
    fotos: List<String>,
    vacunas: List<String>,
    padecimientos: List<String>,
    numInteresados: Int = 0
) = AnimalDetalleResponse(
    id = this.id ?: "",
    nombre = this.nombre,
    especie = this.especie,
    raza = this.raza,
    razaId = this.razaId,
    fechaNacimiento = this.fechaNacimiento,
    sexo = this.sexo.name,
    descripcion = this.descripcion,
    estatus = this.estatus.name,
    esterilizado = this.esterilizado,
    usuarioId = this.usuarioId,
    fechaRegistro = this.fechaRegistro,
    fotos = fotos,
    vacunas = vacunas,
    padecimientos = padecimientos,
    numInteresados = numInteresados
)
