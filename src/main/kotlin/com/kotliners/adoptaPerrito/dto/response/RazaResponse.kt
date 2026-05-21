package com.kotliners.adoptaPerrito.dto.response

/** DTO de respuesta para una raza del catalogo */
data class RazaResponse(
    val id: String,
    val especie: String,
    val nombreEs: String,
    val nombreEn: String
)
