package com.kotliners.adoptaPerrito.domain

import com.kotliners.adoptaPerrito.entities.PadecimientoEntity
import com.kotliners.adoptaPerrito.dto.request.CreatePadecimientoRequest

/**
 * Función de extensión para convertir un CreatePadecimientoRequest a un objeto de dominio Padecimiento.
 */
fun CreatePadecimientoRequest.toPadecimiento(): Padecimiento {
    return Padecimiento(
        id = null,
        nombre = this.nombre
    )
}

/**
 * Función de extensión para convertir un PadecimientoEntity a un objeto de dominio Padecimiento.
 */
fun PadecimientoEntity.toPadecimiento(): Padecimiento {
    return Padecimiento(
        id = this.id?.toString(),  
        nombre = this.nombre
    )
}