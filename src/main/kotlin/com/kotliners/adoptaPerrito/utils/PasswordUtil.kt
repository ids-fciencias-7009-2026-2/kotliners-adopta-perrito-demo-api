package com.kotliners.adoptaPerrito.utils

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

/**
 * Utilidad para el hashing y verificacion de contrasenas usando BCrypt.
 * BCrypt incluye salt automaticamente y es resistente a ataques de fuerza bruta.
 */
object PasswordUtil {

    private val encoder = BCryptPasswordEncoder()

    /**
     * Genera el hash BCrypt de una contrasena en texto plano.
     * @param password Contrasena en texto plano.
     * @return Hash BCrypt de la contrasena.
     */
    fun hash(password: String): String = encoder.encode(password)!!

    /**
     * Verifica si una contrasena en texto plano coincide con un hash BCrypt.
     * @param password Contrasena en texto plano a verificar.
     * @param hash Hash BCrypt almacenado.
     * @return true si la contrasena coincide, false en caso contrario.
     */
    fun matches(password: String, hash: String): Boolean = encoder.matches(password, hash)
}
