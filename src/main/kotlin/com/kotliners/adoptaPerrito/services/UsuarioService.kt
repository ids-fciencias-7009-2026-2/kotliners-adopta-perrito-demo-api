package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.domain.Usuario
import com.kotliners.adoptaPerrito.domain.toUsuario
import com.kotliners.adoptaPerrito.entities.UsuarioEntity
import com.kotliners.adoptaPerrito.repositories.UsuarioRepository
import com.kotliners.adoptaPerrito.repositories.CodigoPostalRepository
import com.kotliners.adoptaPerrito.repositories.toUsuarioEntity
import com.kotliners.adoptaPerrito.utils.PasswordUtil

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

    /** Repositorio de los correos */
    @Autowired
    lateinit var mailAdapter: com.kotliners.adoptaPerrito.adapters.MailAdapter

    @Autowired
    lateinit var accionService: AccionService

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
     * Obtiene coordenadas geograficas para un codigo postal usando la API de Nominatim (OpenStreetMap).
     * Si la API no responde o no encuentra el CP, devuelve coordenadas del centro de CDMX como fallback.
     *
     * @param cp Codigo postal de 5 digitos.
     * @return Par (latitud, longitud).
     */
    private fun translateCpToCoords(cp: String): Pair<java.math.BigDecimal, java.math.BigDecimal> {
        val fallback = Pair(java.math.BigDecimal("19.432608"), java.math.BigDecimal("-99.133209"))
        return try {
            val url = "https://nominatim.openstreetmap.org/search?postalcode=${cp}&country=Mexico&format=json&limit=1"
            val restTemplate = org.springframework.web.client.RestTemplate().apply {
                val factory = org.springframework.http.client.SimpleClientHttpRequestFactory()
                factory.setConnectTimeout(4000)
                factory.setReadTimeout(6000)
                requestFactory = factory
            }
            val headers = org.springframework.http.HttpHeaders().apply {
                set("User-Agent", "ColitasFelices/1.0 (contacto@colitas.mx)")
            }
            val entity = org.springframework.http.HttpEntity<Void>(headers)
            @Suppress("UNCHECKED_CAST")
            val response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, List::class.java)
            val results = response.body as? List<Map<String, Any?>>
            if (results.isNullOrEmpty()) {
                logger.warn("Nominatim no encontro coordenadas para CP $cp, usando fallback")
                return fallback
            }
            val lat = results[0]["lat"]?.toString()?.toBigDecimalOrNull()
            val lon = results[0]["lon"]?.toString()?.toBigDecimalOrNull()
            if (lat != null && lon != null) {
                logger.info("Nominatim: CP $cp -> lat=$lat, lon=$lon")
                Pair(lat, lon)
            } else {
                logger.warn("Nominatim devolvio datos invalidos para CP $cp, usando fallback")
                fallback
            }
        } catch (e: Exception) {
            logger.warn("Error consultando Nominatim para CP $cp: ${e.message}, usando fallback")
            fallback
        }
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
            throw IllegalArgumentException("Algunos datos ya estan registrados. Verifica tu correo, usuario y CURP.")
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
     * Verifica el correo del usuario con el token enviado por correo.
     */
    fun verificarCorreo(token: String): Boolean {
        val entity = usuarioRepository.findByTokenVerificacion(token)
            ?: throw IllegalArgumentException("Token de verificación inválido o expirado")
        entity.verificado = true
        entity.tokenVerificacion = null
        usuarioRepository.save(entity)
        logger.info("Correo verificado para usuario: ${entity.id}")
        return true
    }

    /**
     * Autentica a un usuario con email y contrasena en texto plano.
     * Busca por email y verifica la contrasena con BCrypt.
     * Si las credenciales son correctas, genera y persiste un nuevo token de sesion.
     *
     * @param email Correo electronico del usuario.
     * @param password Contrasena en texto plano (se verifica contra el hash BCrypt almacenado).
     * @return El usuario autenticado con su token, o null si las credenciales son incorrectas.
     */
    fun login(email: String, password: String): Usuario? {
        logger.info("Intento de login para: $email")
        val usuarioEntity = usuarioRepository.findByEmail(email)
        if (usuarioEntity == null) {
            logger.warn("Login fallido: email no encontrado: $email")
            return null
        }
        if (usuarioEntity.fechaEliminado != null) {
            logger.warn("Login fallido: cuenta eliminada para: $email")
            return null
        }
        if (!usuarioEntity.verificado) {
            throw IllegalStateException("CORREO_NO_VERIFICADO")
        }
        if (usuarioEntity.bloqueadoHasta != null && usuarioEntity.bloqueadoHasta!!.isAfter(LocalDateTime.now())) {
            throw IllegalStateException("CUENTA_BLOQUEADA")
        }
        if (!PasswordUtil.matches(password, usuarioEntity.password)) {
            logger.warn("Login fallido: contrasena incorrecta para: $email")
            usuarioEntity.intentosFallidos += 1
            if (usuarioEntity.intentosFallidos >= 5) {
                usuarioEntity.bloqueadoHasta = LocalDateTime.now().plusMinutes(15)
                usuarioEntity.intentosFallidos = 0
            }
            usuarioRepository.save(usuarioEntity)
            return null
        }
        // Credenciales correctas: resetear intentos y enviar código 2FA
        usuarioEntity.intentosFallidos = 0
        usuarioEntity.bloqueadoHasta = null
        val codigo = (100000..999999).random().toString()
        usuarioEntity.codigo2fa = codigo
        usuarioEntity.codigo2faExpira = LocalDateTime.now().plusMinutes(10)
        usuarioRepository.save(usuarioEntity)

        val html = """
            <html><body>
            <p>Hola <strong>${usuarioEntity.nombres}</strong>,</p>
            <p>Tu código de verificación es:</p>
            <h2 style="letter-spacing:8px;text-align:center">${codigo}</h2>
            <p>Este código expira en 10 minutos.</p>
            </body></html>
        """.trimIndent()
        mailAdapter.sendHtmlEmail(usuarioEntity.email, "Código de verificación – Colitas Felices", html)
        logger.info("Codigo 2FA enviado a: $email")

        val usuario = usuarioEntity.toUsuario()
        usuario.password = "****"
        return usuario
    }

    /**
     * Valida el código 2FA y otorga el token de sesión.
     */
    fun verificar2fa(email: String, codigo: String): Usuario? {
        val entity = usuarioRepository.findByEmail(email) ?: return null
        if (entity.codigo2fa == null || entity.codigo2faExpira == null) {
            throw IllegalStateException("No hay código pendiente")
        }
        if (entity.codigo2faExpira!!.isBefore(LocalDateTime.now())) {
            entity.codigo2fa = null
            entity.codigo2faExpira = null
            usuarioRepository.save(entity)
            throw IllegalStateException("CODIGO_EXPIRADO")
        }
        if (entity.codigo2fa != codigo) {
            throw IllegalStateException("CODIGO_INCORRECTO")
        }
        entity.codigo2fa = null
        entity.codigo2faExpira = null
        entity.token = tokenGenerator()
        val saved = usuarioRepository.save(entity)

        // Notificación de ingreso exitoso
        val html = """
            <html><body>
            <p>Hola <strong>${saved.nombres}</strong>,</p>
            <p>Se ha detectado un nuevo inicio de sesión en tu cuenta de <strong>Colitas Felices</strong>.</p>
            <p>Si no reconoces esta actividad, contacta a soporte de inmediato.</p>
            </body></html>
        """.trimIndent()
        mailAdapter.sendHtmlEmail(saved.email, "Nuevo inicio de sesión en Colitas Felices", html)
        accionService.registrar(saved.id, "LOGIN")
        return saved.toUsuario()
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
    /**
     * Elimina logicamente la cuenta de un usuario autenticado (soft delete).
     * Establece fecha_eliminado y envia correo de confirmacion.
     *
     * @param userId ID del usuario a eliminar.
     * @throws IllegalArgumentException si el usuario no existe o ya fue eliminado.
     */
    fun eliminarCuenta(userId: String) {
        logger.info("Solicitud de eliminacion de cuenta para usuario ID: $userId")
        val uuid = java.util.UUID.fromString(userId)
        val entity = usuarioRepository.findById(uuid).orElse(null)
            ?: throw IllegalArgumentException("Usuario no encontrado.")
        if (entity.fechaEliminado != null) {
            logger.warn("Cuenta ya eliminada: $userId")
            throw IllegalArgumentException("La cuenta ya fue eliminada.")
        }
        usuarioRepository.softDeleteById(uuid, java.time.LocalDateTime.now())
        accionService.registrar(uuid, "ELIMINACION_CUENTA")
        logger.info("Cuenta eliminada logicamente para usuario ID: $userId")
        val cuerpoEliminacion = """
        <html><body>
        <p>Hola <strong>${entity.nombres}</strong>,</p>
        <p>Tu cuenta en <strong>Colitas Felices</strong> ha sido eliminada exitosamente.</p>
        <p>Si no solicitaste esta accion, contacta a soporte de inmediato.</p>
        <br><p>Saludos,<br>Colitas Felices</p>
        </body></html>
    """.trimIndent()
        val resultado = mailAdapter.sendHtmlEmail(
            to = entity.email,
            subject = "Tu cuenta en Colitas Felices ha sido eliminada",
            htmlBody = cuerpoEliminacion
        )
        if (resultado.isFailure) {
            logger.warn("No se pudo enviar correo de eliminacion a: ${entity.email}")
        }
    }
}
