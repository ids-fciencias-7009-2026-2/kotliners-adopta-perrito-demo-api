package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.domain.Usuario
import com.kotliners.adoptaPerrito.domain.toUsuario
import com.kotliners.adoptaPerrito.entities.UsuarioEntity
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
     * Valida que el usuario no tenga datos duplicados en la base de datos.
     * 
     * @param usuarioEntity La entidad de usuario a validar.
     * @throws IllegalArgumentException si el usuario tiene datos duplicados.
     */
    private fun validateUsuario(usuarioEntity: UsuarioEntity) {
        logger.debug("Validando usuario: $usuarioEntity")
        if (usuarioRepository.findByEmail(usuarioEntity.email) != null
            || usuarioRepository.findByUsername(usuarioEntity.username) != null
            || usuarioRepository.findByCurp(usuarioEntity.curp) != null) {
            logger.warn("Intento de registro con datos duplicados")
            throw IllegalArgumentException("Datos ya registrados.")
        }
    }

    /**
     * Agrega un nuevo usuario al sistema.
     * 
     * @param usuario El objeto de dominio Usuario a agregar.
     * @return El usuario agregado con su ID generado.
     * @throws IllegalArgumentException si el usuario tiene datos duplicados.
     */
    fun addNewUsuario(usuario: Usuario): Usuario {
        logger.info("Agregando nuevo usuario: $usuario")
        val usuarioEntity = usuario.toUsuarioEntity()
        validateUsuario(usuarioEntity)
        val savedEntity = usuarioRepository.save(usuarioEntity)
        logger.info("Usuario guardado en la base de datos con ID: ${savedEntity.id}")
        return savedEntity.toUsuario()
    }

    /**
     * Recupera todos los usuarios registrados en el sistema.
     * 
     * @return Lista de usuarios encontrados.
     */
    fun searchAllUsuarios(): List<Usuario> {
        logger.info("Buscando todos los usuarios en la base de datos")
        val usuarioEntities = usuarioRepository.findAll()
        logger.info("Número de usuarios encontrados: ${usuarioEntities.count()}")
        return usuarioEntities.map { usuarioEntity ->
            logger.debug("Transformando UsuarioEntity con ID ${usuarioEntity.id} a Usuario")
            usuarioEntity.toUsuario()
        }
    }
}
