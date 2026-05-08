package com.kotliners.adoptaPerrito.domain

/**
 * Clase de dominio que representa una vacuna que un animal ha recibido o necesita recibir.
 */
data class Vacuna(

    /* ID de la vacuna */
    val id: String? = null,
    /* Nombre de la vacuna */
    val nombre: String
)