package com.kotliners.adoptaPerrito.domain

/**
 * Clase de dominio que representa un padecimiento que puede tener un animal.
 */
data class Padecimiento(

    /* ID del padecimiento */
    val id: String? = null,
    /* Nombre del padecimiento */
    val nombre: String
)