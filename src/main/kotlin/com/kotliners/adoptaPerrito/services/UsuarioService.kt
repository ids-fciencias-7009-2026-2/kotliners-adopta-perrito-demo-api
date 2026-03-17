package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.domain.Usuario
import com.kotliners.adoptaPerrito.domain.toUsuario
import com.kotliners.adoptaPerrito.repositories.UsuarioRepository
import com.kotliners.adoptaPerrito.repositories.toUsuarioEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UsuarioService {

    /* Logger para registrar eventos importantes del flujo de ejecución. */
    private val logger: Logger = LoggerFactory.getLogger(UsuarioService::class.java)

    /**
     * Repositorio de usuarios.
     */
    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    /**
     * Agrega un nuevo usuario al sistema.
     */
    fun addNewUsuario(usuario: Usuario): Usuario {
        logger.info("Agregando nuevo usuario: $usuario")
        val usuarioEntity = usuario.toUsuarioEntity()
        val savedEntity = usuarioRepository.save(usuarioEntity)
        logger.info("Usuario guardado en la base de datos con ID: ${savedEntity.id}")
        return savedEntity.toUsuario()
    }
}
