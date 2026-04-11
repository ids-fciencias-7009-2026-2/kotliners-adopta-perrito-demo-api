package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.domain.Usuario
import com.kotliners.adoptaPerrito.domain.toUsuario
import com.kotliners.adoptaPerrito.entities.UsuarioEntity
import com.kotliners.adoptaPerrito.entities.CodigoPostalEntity
import com.kotliners.adoptaPerrito.repositories.UsuarioRepository
import com.kotliners.adoptaPerrito.repositories.CodigoPostalRepository
import com.kotliners.adoptaPerrito.repositories.toUsuarioEntity

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.UUID
import java.time.LocalDateTime
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
     */
    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    /**
     * Repositorio de códigos postales inyectado por Spring.
     */
    @Autowired
    lateinit var codigoPostalRepository: CodigoPostalRepository

    /**
     * Traduce un código postal a coordenadas geográficas.
     * TBD: traducir CP a coordenadas reales usando una API de geocodificación.
     * Por ahora retorna coordenadas mock del centro de CDMX.
     *
     * @param cp Código postal de 5 dígitos
     * @return Par (latitud, longitud)
     */
    private fun translateCpToCoords(cp: String): Pair<java.math.BigDecimal, java.math.BigDecimal> {
        // TBD: traducir CP a coordenadas usando API externa (como Google Maps Geocoding API)
        return Pair(java.math.BigDecimal("19.432608"), java.math.BigDecimal("-99.133209"))
    }

    /**
     * Verifica que el código postal exista en la tabla codigo_postal.
     * Si no existe, lo crea usando [translateCpToCoords] para obtener las coordenadas.
     *
     * @param cp Código postal de 5 dígitos
     */
    private fun ensureCodigoPostalExists(cp: String) {
        if (!codigoPostalRepository.existsById(cp)) {
            logger.info("CP $cp no encontrado, creando con coordenadas mock")
            val (latitud, longitud) = translateCpToCoords(cp)
            codigoPostalRepository.save(
                com.kotliners.adoptaPerrito.entities.CodigoPostalEntity(
                    codigoPostal = cp,
                    latitud = latitud,
                    longitud = longitud
                )
            )
        }
    }

    /**
     * Valida que el usuario no tenga datos duplicados en la base de datos, como email, username o CURP.
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
        validateUsuario(usuarioEntity)
        ensureCodigoPostalExists(usuarioEntity.codigoPostal)
        val savedEntity = usuarioRepository.save(usuarioEntity)
        logger.info("Usuario guardado en la base de datos con ID: ${savedEntity.id}")
        val usuarioGuardado = savedEntity.toUsuario()
        usuarioGuardado.password = "****"  // Ocultar contraseña antes de retornar
        return usuarioGuardado
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
    fun invalidateToken(userId: String?) {
        if (userId == null) {
            logger.warn("Intento de invalidar token con ID null")
            return
        }
        usuarioRepository.updateTokenById(UUID.fromString(userId), null)
        logger.info("Token invalidado para usuario ID: $userId")
    }

    /**
     * Actualiza la información de un usuario autenticado.
     *
     * Solo actualiza los campos que vienen en el request (parcial).
     * No permite cambiar curp, username ni token.
     *
     * @param userId ID del usuario a actualizar
     * @param request DTO con los campos a actualizar
     * @return El usuario actualizado, o null si no existe
     */
    fun updateUsuario(userId: String, request: com.kotliners.adoptaPerrito.dto.request.UpdateUsuarioRequest): Usuario? {
        logger.info("Actualizando usuario ID: $userId")
        val entity = usuarioRepository.findById(UUID.fromString(userId)).orElse(null) ?: return null
        entity.nombres = request.nombres
        entity.apellidoPaterno = request.apellidoPaterno
        entity.apellidoMaterno = request.apellidoMaterno
        entity.email = request.email
        ensureCodigoPostalExists(request.codigoPostal)
        entity.codigoPostal = request.codigoPostal
        if (request.fotoPerfil != null) entity.fotoPerfil = request.fotoPerfil
        entity.fechaUpdate = LocalDateTime.now()
        val saved = usuarioRepository.save(entity)
        logger.info("Usuario actualizado: ${saved.id}")
        return saved.toUsuario()
    }
}