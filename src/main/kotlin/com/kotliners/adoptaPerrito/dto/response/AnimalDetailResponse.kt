package com.kotliners.adoptaPerrito.dto.response

import com.kotliners.adoptaPerrito.domain.Animal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DTO de respuesta para la vista de detalle de un animal.
 */
data class AnimalDetailResponse(
    val id: String,
    val nombre: String,
    val especie: String,
    val raza: String?,
    val fechaNacimiento: LocalDate,
    val sexo: String,
    val descripcion: String,
    val estatus: String,
    val usuarioId: String,
    val fechaRegistro: LocalDateTime,
    val inapropiado: Boolean,
    val esterilizado: Boolean,
    val updatedAt: LocalDateTime,
    val esDueno: Boolean,
    val puedeEditar: Boolean,
    val puedeEliminar: Boolean
)

/**
 * Mapea el dominio Animal al DTO que consume el frontend de detalle.
 */
fun Animal.toAnimalDetailResponse(authenticatedUserId: String): AnimalDetailResponse {
    val isOwner = this.usuarioId == authenticatedUserId
    return AnimalDetailResponse(
        id = this.id.orEmpty(),
        nombre = this.nombre,
        especie = this.especie,
        raza = this.raza,
        fechaNacimiento = this.fechaNacimiento,
        sexo = this.sexo.name,
        descripcion = this.descripcion,
        estatus = this.estatus.name,
        usuarioId = this.usuarioId,
        fechaRegistro = this.fechaRegistro,
        inapropiado = this.inapropiado,
        esterilizado = this.esterilizado,
        updatedAt = this.updatedAt,
        esDueno = isOwner,
        puedeEditar = isOwner,
        puedeEliminar = isOwner
    )
}
