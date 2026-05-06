package com.kotliners.adoptaPerrito.domain

/**
 * Estatus disponibles para un animal.
 */
enum class Estatus {
    /* Estatus que indica que el animal está disponible para adopción */
    DISPONIBLE,
    /* Estatus que indica que el animal ha sido adoptado */
    ADOPTADO
}