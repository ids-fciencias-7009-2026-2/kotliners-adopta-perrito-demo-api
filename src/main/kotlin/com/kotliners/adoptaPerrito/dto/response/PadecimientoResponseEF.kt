package com.kotliners.adoptaPerrito.dto.response

import com.kotliners.adoptaPerrito.domain.Padecimiento

/**
 * Convierte un objeto de dominio Padecimiento a un DTO de respuesta PadecimientoResponse.
 */
fun Padecimiento.toPadecimientoResponse(): PadecimientoResponse {
    return PadecimientoResponse(
        id = this.id,
        nombre = this.nombre
    )
}