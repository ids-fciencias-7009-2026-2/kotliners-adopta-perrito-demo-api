package com.kotliners.adoptaPerrito.dto.response

/** DTO de respuesta con la informacion de una raza obtenida de API externa,
 * ya traducida al espanol. */
data class RazaInfoResponse(
    val nombre: String,
    val imagenUrl: String?,
    val wikipediaUrl: String?,
    val campos: List<RazaCampoResponse>
)

/**
 * Tipo de campo para renderizado en el frontend.
 * - TEXT: texto libre (descripcion, temperamento, origen)
 * - SCORE: escala 1-5 (se muestra con estrellas)
 * - BOOL: si/no (se muestra con icono like/dislike)
 */
enum class TipoCampo { TEXT, SCORE, BOOL }

data class RazaCampoResponse(
    val etiqueta: String,
    val valor: String,
    val tipo: TipoCampo
)
