package com.kotliners.adoptaPerrito.utils

import com.kotliners.adoptaPerrito.domain.Usuario
import com.kotliners.adoptaPerrito.services.UsuarioService

/**
 * Utilidad para extraer y validar el token de sesion del header Authorization.
 * Centraliza la logica de extraccion para evitar duplicacion en los controladores.
 */
object TokenExtractor {

    /**
     * Extrae el token del header Authorization (formato "Bearer <token>")
     * y busca el usuario asociado en el servicio.
     *
     * @param authHeader Valor del header Authorization.
     * @param usuarioService Servicio para buscar el usuario por token.
     * @return El usuario autenticado, o null si el header es nulo o el token es invalido.
     */
    fun resolveUser(authHeader: String?, usuarioService: UsuarioService): Usuario? {
        if (authHeader == null) return null
        val token = authHeader.replace("Bearer ", "").trim()
        if (token.isBlank()) return null
        return usuarioService.findByToken(token)
    }
}
