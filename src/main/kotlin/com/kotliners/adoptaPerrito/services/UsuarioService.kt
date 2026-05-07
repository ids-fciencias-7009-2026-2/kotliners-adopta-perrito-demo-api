package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.domain.Usuario
import com.kotliners.adoptaPerrito.domain.toUsuario
import com.kotliners.adoptaPerrito.entities.UsuarioEntity
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
 * Servicio de negocio para la gestion de usuarios.
 * Contiene la logica principal de registro, autenticacion, tokens y actualizacion de perfil.
 */
@Service
class UsuarioService {

    /** Logger del servicio. */
    private val logger: Logger = LoggerFactory.getLogger(UsuarioService::class.java)

    /** Repositorio de usuarios. */
    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    /** Repositorio de codigos postales. */
    @Autowired
    lateinit var codigoPostalRepository: CodigoPostalRepository

    /**
     * Valida que el codigo postal tenga exactamente 5 digitos numericos.
     * Protege la capa de servicio ante llamadas directas sin pasar por el DTO.
     *
     * @param cp Codigo postal a validar.
     * @throws IllegalArgumentException si el formato es invalido.
     */
    private fun validateCodigoPostal(cp: String) {
        if (!cp.matches(Regex("^\\d{5}$"))) {
            logger.warn("Codigo postal invalido: $cp")
            throw IllegalArgumentException("El codigo postal debe tener exactamente 5 digitos numericos.")
        }
    }

    /**
     * Retorna coordenadas geograficas mock para un codigo postal.
     * TBD: reemplazar con una API de geocodificacion real.
     *
     * @param cp Codigo postal de 5 digitos.
     * @return Par (latitud, longitud) con coordenadas del centro de CDMX.
     */
    private fun translateCpToCoords(cp: String): Pair<java.math.BigDecimal, java.math.BigDecimal> {
        return Pair(java.math.BigDecimal("19.432608"), java.math.BigDecimal("-99.133209"))
    }

    /**
     * Verifica que el codigo postal exista en la tabla codigo_postal.
     * Si no existe, lo inserta con coordenadas mock.
     *
     * @param cp Codigo postal de 5 digitos.
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
     * Valida que el email, username y CURP del usuario no esten ya registrados.
     *
     * @param usuarioEntity Entidad a validar.
     * @throws IllegalArgumentException si alguno de los datos ya existe.
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
     * Registra un nuevo usuario en el sistema.
     * Valida duplicados y formato del codigo postal antes de persistir.
     *
     * @param usuario Datos del usuario a registrar.
     * @return El usuario registrado con su ID generado. La contrasena se oculta en la respuesta.
     * @throws IllegalArgumentException si hay datos duplicados o el CP es invalido.
     */
    fun addNewUsuario(usuario: Usuario): Usuario {
        logger.info("Agregando nuevo usuario: $usuario")
        val usuarioEntity = usuario.toUsuarioEntity()
        validateUsuario(usuarioEntity)
        validateCodigoPostal(usuarioEntity.codigoPostal)
        ensureCodigoPostalExists(usuarioEntity.codigoPostal)
        val savedEntity = usuarioRepository.save(usuarioEntity)
        logger.info("Usuario guardado con ID: ${savedEntity.id}")
        val usuarioGuardado = savedEntity.toUsuario()
        usuarioGuardado.password = "****"
        return usuarioGuardado
    }

    /**
     * Recupera todos los usuarios registrados en el sistema.
     *
     * @return Lista de todos los usuarios.
     */
    fun searchAllUsuarios(): List<Usuario> {
        logger.info("Buscando todos los usuarios")
        val usuarioEntities = usuarioRepository.findAll()
        logger.info("Usuarios encontrados: ${usuarioEntities.count()}")
        return usuarioEntities.map { it.toUsuario() }
    }

    /**
     * Autentica a un usuario con email y contrasena hasheada.
     * Si las credenciales son correctas, genera y persiste un nuevo token de sesion.
     *
     * @param email Correo electronico del usuario.
     * @param password Contrasena hasheada con SHA-256.
     * @return El usuario autenticado con su token, o null si las credenciales son incorrectas.
     */
    fun login(email: String, password: String): Usuario? {
        logger.info("Intento de login para: $email")
        val usuarioEntity = usuarioRepository.findUserByPasswordAndEmail(email, password)
        logger.info("Usuario encontrado: ${usuarioEntity != null}")
        if (usuarioEntity != null) {
            val token = tokenGenerator()
            usuarioEntity.token = token
            val savedEntity = usuarioRepository.save(usuarioEntity)
            logger.info("Login exitoso para: ${savedEntity.email}")
            return savedEntity.toUsuario()
        }
        logger.warn("Login fallido para: $email")
        return null
    }

    /**
     * Genera un token UUID unico para la sesion del usuario.
     *
     * @return Token de sesion como cadena de texto.
     */
    fun tokenGenerator(): String {
        val token = UUID.randomUUID().toString()
        logger.debug("Token generado: $token")
        return token
    }

    /**
     * Busca el usuario asociado a un token de sesion.
     *
     * @param token Token de sesion a validar.
     * @return El usuario asociado al token, o null si el token es invalido.
     */
    fun findByToken(token: String): Usuario? {
        logger.info("Buscando usuario por token: ${token.take(8)}...")
        val usuarioLogged = usuarioRepository.findByToken(token)
        logger.info("Usuario encontrado por token: ${usuarioLogged != null}")
        return usuarioLogged?.toUsuario()
    }

    /**
     * Invalida el token de sesion de un usuario estableciendolo como null en la BD.
     *
     * @param userId ID del usuario cuya sesion se va a invalidar.
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
     * Actualiza la informacion editable de un usuario autenticado.
     * No permite cambiar curp, username, rol ni contrasena.
     * Valida que el nuevo email no este en uso por otro usuario.
     * Valida el formato del codigo postal.
     *
     * @param userId ID del usuario a actualizar.
     * @param request DTO con los campos a actualizar.
     * @return El usuario actualizado, o null si no existe.
     * @throws IllegalArgumentException si el email ya pertenece a otro usuario o el CP es invalido.
     */
    fun updateUsuario(userId: String, request: com.kotliners.adoptaPerrito.dto.request.UpdateUsuarioRequest): Usuario? {
        logger.info("Actualizando usuario ID: $userId")
        val uuid = UUID.fromString(userId)
        val entity = usuarioRepository.findById(uuid).orElse(null) ?: return null

        // Verificar que el email no este en uso por otro usuario
        val existingWithEmail = usuarioRepository.findByEmail(request.email)
        if (existingWithEmail != null && existingWithEmail.id != uuid) {
            logger.warn("Email ya registrado por otro usuario: ${request.email}")
            throw IllegalArgumentException("El correo electronico ya esta en uso por otro usuario.")
        }

        entity.nombres = request.nombres
        entity.apellidoPaterno = request.apellidoPaterno
        entity.apellidoMaterno = request.apellidoMaterno
        entity.email = request.email
        validateCodigoPostal(request.codigoPostal)
        ensureCodigoPostalExists(request.codigoPostal)
        entity.codigoPostal = request.codigoPostal
        if (request.fotoPerfil != null) entity.fotoPerfil = request.fotoPerfil
        entity.fechaUpdate = LocalDateTime.now()
        val saved = usuarioRepository.save(entity)
        logger.info("Usuario actualizado: ${saved.id}")
        return saved.toUsuario()
    }
}
