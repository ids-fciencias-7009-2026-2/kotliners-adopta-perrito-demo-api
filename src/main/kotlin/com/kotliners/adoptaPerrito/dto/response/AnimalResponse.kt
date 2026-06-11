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
    val numInteresados: Int = 0,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val cuidadorUsername: String? = null,
    val cuidadorFoto: String? = null,
    val codigoPostal: String? = null
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
    val numInteresados: Int = 0,
    val cuidadorUsername: String? = null,
    val cuidadorFoto: String? = null
)

/**
 * Convierte un Animal de dominio a AnimalResponse (listado).
 * Acepta opcionalmente la primera foto para mostrar en tarjetas.
 */
fun Animal.toAnimalResponse(fotoPortada: String? = null, numInteresados: Int = 0, latitud: Double? = null, longitud: Double? = null, cuidadorUsername: String? = null, cuidadorFoto: String? = null, codigoPostal: String? = null) = AnimalResponse(
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
    numInteresados = numInteresados,
    latitud = latitud?.let { Math.round(it * 100.0) / 100.0 },
    longitud = longitud?.let { Math.round(it * 100.0) / 100.0 },
    cuidadorUsername = cuidadorUsername,
    cuidadorFoto = cuidadorFoto,
    codigoPostal = codigoPostal
)

fun Animal.toAnimalDetalleResponse(
    fotos: List<String>,
    vacunas: List<String>,
    padecimientos: List<String>,
    numInteresados: Int = 0,
    cuidadorUsername: String? = null,
    cuidadorFoto: String? = null
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
    numInteresados = numInteresados,
    cuidadorUsername = cuidadorUsername,
    cuidadorFoto = cuidadorFoto
)
