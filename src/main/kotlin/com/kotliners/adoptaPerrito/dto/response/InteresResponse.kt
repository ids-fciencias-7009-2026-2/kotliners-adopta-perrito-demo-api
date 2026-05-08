package com.kotliners.adoptaPerrito.dto.response

import java.time.LocalDateTime

/**
 * DTO de respuesta al manifestar interes en un animal.
 * Incluye advertencia si el correo de notificacion no pudo enviarse.
 */
data class InteresResponse(
    /** ID del usuario que manifesto interes. */
    val usuarioId: String,
    /** ID del animal en el que se manifesto interes. */
    val animalId: String,
    /** Fecha en que se registro el interes. */
    val fecha: LocalDateTime,
    /** Advertencia si el correo de notificacion fallo. Null si se envio correctamente. */
    val advertencia: String? = null
)
