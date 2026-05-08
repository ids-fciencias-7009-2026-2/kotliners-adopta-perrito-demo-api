package com.kotliners.adoptaPerrito.dto.response

/**
 * DTO de respuesta para un padecimiento.
 */
data class PadecimientoResponse(

    /* ID del padecimiento */
    val id: String?,

    /* Nombre del padecimiento */
    val nombre: String
)