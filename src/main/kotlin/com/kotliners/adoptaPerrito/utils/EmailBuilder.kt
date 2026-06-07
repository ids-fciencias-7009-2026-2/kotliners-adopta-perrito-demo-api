package com.kotliners.adoptaPerrito.utils

/**
 * Builder para construir correos HTML de forma legible.
 * Patron Builder: facilita la construccion de objetos complejos paso a paso.
 */
class EmailBuilder {
    private var greeting: String = ""
    private val lines = mutableListOf<String>()
    private var buttonText: String? = null
    private var buttonUrl: String? = null

    fun greeting(nombre: String) = apply {
        greeting = "<p>Hola <strong>$nombre</strong>,</p>"
    }

    fun line(text: String) = apply {
        lines.add("<p>$text</p>")
    }

    fun button(text: String, url: String) = apply {
        buttonText = text
        buttonUrl = url
    }

    fun build(): String {
        val sb = StringBuilder("<html><body>")
        sb.append(greeting)
        lines.forEach { sb.append(it) }
        if (buttonText != null && buttonUrl != null) {
            sb.append("<p><a href=\"$buttonUrl\" style=\"background:#65c3c8;color:white;padding:10px 20px;border-radius:8px;text-decoration:none\">$buttonText</a></p>")
        }
        sb.append("<br><p>Saludos,<br>Colitas Felices</p>")
        sb.append("</body></html>")
        return sb.toString()
    }
}
