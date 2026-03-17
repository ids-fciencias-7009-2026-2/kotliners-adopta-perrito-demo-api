package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.domain.Usuario
import com.kotliners.adoptaPerrito.domain.toUsuario
import com.kotliners.adoptaPerrito.repositories.UsuarioRepository
import com.kotliners.adoptaPerrito.repositories.toUsuarioEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.UUID

/**
 * Servicio de negocio para la gestión de usuarios en el sistema de adopción de perros.
 *
 * Esta clase contiene la lógica de negocio principal para:
 * - Registro de nuevos usuarios
 * - Autenticación y login de usuarios
 * - Generación y gestión de tokens de sesión
 * - Validación de usuarios por token
 *
 * Utiliza el patrón de diseño Service para separar la lógica de negocio
 * de la capa de presentación (controladores) y la capa de datos (repositorios).
 */
@Service
class UsuarioService {

    /**
     * Logger para registrar eventos importantes del servicio.
     * Se utiliza SLF4J para el logging, permitiendo configuración flexible.
     */
    private val logger: Logger = LoggerFactory.getLogger(UsuarioService::class.java)

    /**
     * Repositorio de usuarios inyectado por Spring.
     * Proporciona acceso a las operaciones CRUD de la base de datos.
     */
    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * Este método realiza las siguientes operaciones:
     * 1. Convierte el objeto de dominio Usuario a UsuarioEntity
     * 2. Guarda la entidad en la base de datos
     * 3. Convierte la entidad guardada de vuelta a objeto de dominio
     * 4. Registra el proceso en los logs
     *
     * @param usuario El usuario a registrar con toda su información
     * @return El usuario registrado con su ID generado por la base de datos
     */
    fun addNewUsuario(usuario: Usuario): Usuario {
        logger.info("Agregando nuevo usuario: $usuario")
        val usuarioEntity = usuario.toUsuarioEntity()
        // ToDO: validar credenciales
        val savedEntity = usuarioRepository.save(usuarioEntity)
        logger.info("Usuario guardado en la base de datos con ID: ${savedEntity.id}")
        return savedEntity.toUsuario()
    }
    
    /**
     * Realiza la validación de credenciales de un usuario.
     *
     * Busca en la base de datos un usuario que coincida con el correo y contraseña proporcionados.
     * En caso de encontrar una coincidencia, se crea y persiste un nuevo token de sesión
     * asociado a dicho usuario.
     *
     * @param email Correo electrónico del usuario a autenticar.
     * @param password Contraseña encriptada para validación.
     *
     * @return Retorna el usuario autenticado con su información completa,
     * o nulo si las credenciales proporcionadas son incorrectas.
     */

    fun login(email: String, password: String): Usuario? {
        logger.info("Login attempt for email: $email")
        logger.info("Hashed password received: $password")
        
        val usuarioEntity = usuarioRepository
            .findUserByPasswordAndEmail(email, password)

        logger.info("User entity found: ${usuarioEntity != null}")
        if (usuarioEntity != null) {
            logger.info("User found - ID: ${usuarioEntity.id}, Email: ${usuarioEntity.email}")
            val token = tokenGenerator()
            logger.info("Generated token: $token")
            usuarioEntity.token = token
            val savedEntity = usuarioRepository.save(usuarioEntity)
            logger.info("User saved with token: ${savedEntity.token}")
            return savedEntity.toUsuario()
        } else {
            logger.warn("No user found with email: $email and provided password hash")
        }
        return null
    }

    /**
     * Genera un token único de sesión para el usuario autenticado.
     *
     * Utiliza UUID para crear un identificador único que será asignado al usuario
     * y almacenado en la base de datos. Este token se utiliza para mantener
     * la sesión del usuario activa sin necesidad de reautenticación constante.
     *
     * @return Una cadena que representa el token generado.
     */
    fun tokenGenerator(): String {
        val token = UUID.randomUUID().toString()
        logger.debug("Token generado: $token")
        return token
    }

    /**
     * Valida un token de sesión y retorna el usuario asociado.
     *
     * Este método es utilizado por el endpoint de logout para verificar
     * que el token proporcionado corresponde a un usuario válido en el sistema.
     *
     * @param token El token de sesión a validar
     * @return El usuario asociado al token, o null si el token es inválido
     */
    fun findByToken(token: String): Usuario? {
        logger.info("Buscando usuario por token: ${token.take(8)}...")
        val usuarioLogged = usuarioRepository.findByToken(token)
        logger.info("Usuario encontrado por token: ${usuarioLogged != null}")
        return usuarioLogged?.toUsuario()
    }

    /**
     * Invalida el token de sesión de un usuario poniéndolo como null en la base de datos.
     * 
     * @param userId ID del usuario cuya sesión se va a invalidar (puede ser null)
     */
    fun invalidateToken(userId: Int?) {
        if (userId == null) {
            logger.warn("Intento de invalidar token con ID null")
            return
        }
        usuarioRepository.updateTokenById(userId, null)
        logger.info("Token invalidado para usuario ID: $userId")
    }
}
