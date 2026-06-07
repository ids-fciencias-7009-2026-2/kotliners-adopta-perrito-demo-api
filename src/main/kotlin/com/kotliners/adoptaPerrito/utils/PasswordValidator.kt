package com.kotliners.adoptaPerrito.utils

/**
 * Patron Strategy para validacion de contrasenas.
 * Cada regla es una estrategia independiente que se puede agregar o quitar.
 */
interface PasswordRule {
    fun validate(password: String): String?
}

class MinLengthRule(private val min: Int = 8) : PasswordRule {
    override fun validate(password: String) =
        if (password.length < min) "mínimo $min caracteres" else null
}

class UppercaseRule : PasswordRule {
    override fun validate(password: String) =
        if (!password.any { it.isUpperCase() }) "al menos una mayúscula" else null
}

class LowercaseRule : PasswordRule {
    override fun validate(password: String) =
        if (!password.any { it.isLowerCase() }) "al menos una minúscula" else null
}

class DigitRule : PasswordRule {
    override fun validate(password: String) =
        if (!password.any { it.isDigit() }) "al menos un número" else null
}

class SpecialCharRule : PasswordRule {
    override fun validate(password: String) =
        if (!password.any { !it.isLetterOrDigit() }) "al menos un carácter especial" else null
}

/**
 * Validador que aplica todas las estrategias configuradas.
 */
object PasswordValidator {
    private val rules: List<PasswordRule> = listOf(
        MinLengthRule(),
        UppercaseRule(),
        LowercaseRule(),
        DigitRule(),
        SpecialCharRule()
    )

    /**
     * Valida la contrasena contra todas las reglas.
     * @return lista de errores (vacia si la contrasena es valida)
     */
    fun validate(password: String): List<String> {
        return rules.mapNotNull { it.validate(password) }
    }
}
