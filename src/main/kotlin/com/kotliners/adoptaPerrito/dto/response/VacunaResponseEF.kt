package com.kotliners.adoptaPerrito.dto.response

import com.kotliners.adoptaPerrito.domain.Vacuna

/**
 * Convierte un objeto de dominio Vacuna a un DTO de respuesta VacunaResponse.
 */
fun Vacuna.toVacunaResponse(): VacunaResponse {
    return VacunaResponse(
        id = this.id,
        nombre = this.nombre
    )
}