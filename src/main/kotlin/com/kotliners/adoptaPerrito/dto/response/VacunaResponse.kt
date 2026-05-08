package com.kotliners.adoptaPerrito.dto.response

/**
 * DTO de respuesta para una vacuna.
 */
data class VacunaResponse(

    /* ID de la vacuna */
    val id: String?,
    /* Nombre de la vacuna */
    val nombre: String
)