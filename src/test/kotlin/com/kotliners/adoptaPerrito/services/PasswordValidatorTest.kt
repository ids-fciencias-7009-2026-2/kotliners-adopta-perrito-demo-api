package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.utils.PasswordValidator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PasswordValidatorTest {

    @Test
    fun `contrasena valida no tiene errores`() {
        val errors = PasswordValidator.validate("Admin123!")
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `contrasena corta falla`() {
        val errors = PasswordValidator.validate("Ab1!")
        assertTrue(errors.any { "8" in it })
    }

    @Test
    fun `sin mayuscula falla`() {
        val errors = PasswordValidator.validate("admin123!")
        assertTrue(errors.any { "mayúscula" in it })
    }

    @Test
    fun `sin minuscula falla`() {
        val errors = PasswordValidator.validate("ADMIN123!")
        assertTrue(errors.any { "minúscula" in it })
    }

    @Test
    fun `sin numero falla`() {
        val errors = PasswordValidator.validate("AdminPass!")
        assertTrue(errors.any { "número" in it })
    }

    @Test
    fun `sin caracter especial falla`() {
        val errors = PasswordValidator.validate("Admin1234")
        assertTrue(errors.any { "especial" in it })
    }

    @Test
    fun `contrasena vacia falla con todos los errores`() {
        val errors = PasswordValidator.validate("")
        assertEquals(5, errors.size)
    }
}
