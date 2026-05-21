package com.kotliners.adoptaPerrito.dto.response

/**
 * DTO de respuesta con la informacion de una raza obtenida de API externa,
 * ya traducida al espanol.
 */
data class RazaInfoResponse(
    /** Nombre de la raza en ingles (nombre oficial de la API) */
    val nombre: String,
    /** URL de imagen representativa de la raza, o null si no hay */
    val imagenUrl: String?,
    /** Lista de campos con etiqueta y valor, todos en espanol */
    val campos: List<RazaCampoResponse>
)

data class RazaCampoResponse(
    val etiqueta: String,
    val valor: String
)
