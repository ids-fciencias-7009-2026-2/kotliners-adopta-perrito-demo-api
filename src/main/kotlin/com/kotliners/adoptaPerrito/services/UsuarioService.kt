package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.domain.Usuario
import com.kotliners.adoptaPerrito.domain.toUsuario
import com.kotliners.adoptaPerrito.adapters.MailAdapter
import com.kotliners.adoptaPerrito.entities.UsuarioEntity
import com.kotliners.adoptaPerrito.repositories.UsuarioRepository
import com.kotliners.adoptaPerrito.repositories.CodigoPostalRepository
import com.kotliners.adoptaPerrito.repositories.toUsuarioEntity

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.security.MessageDigest
import java.security.SecureRandom
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

    @Autowired
    lateinit var mailAdapter: MailAdapter

    private val secureRandom = SecureRandom()
    private val maxIntentosFallidos = 3
    private val minutosBloqueo = 15L
    private val minutosCodigo2FA = 10L
    private val minutosResetPassword = 30L
    private val horasVerificacionEmail = 24L

    sealed class LoginResult {
        data class Success(val usuario: Usuario) : LoginResult()
        object InvalidCredentials : LoginResult()
        data class Locked(val lockedUntil: LocalDateTime) : LoginResult()
        object EmailNotVerified : LoginResult()
        object TwoFactorRequired : LoginResult()
    }
    /** Repositorio de los correos */
    @Autowired
    lateinit var mailAdapter: com.kotliners.adoptaPerrito.adapters.MailAdapter

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

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun generateSixDigitCode(): String {
        return secureRandom.nextInt(1_000_000).toString().padStart(6, '0')
    }

    private fun sendEmail(to: String, subject: String, html: String) {
        if (::mailAdapter.isInitialized) {
            mailAdapter.sendHtmlEmail(to = to, subject = subject, htmlBody = html)
        } else {
            logger.warn("MailAdapter no inicializado; correo omitido para $to")
        }
    }

    private fun sendVerificationEmail(usuario: UsuarioEntity) {
        val token = usuario.emailVerificacionToken ?: return
        sendEmail(
            usuario.email,
            "Verifica tu correo - Colitas Felices",
            """
                <h2>Verifica tu correo</h2>
                <p>Hola ${usuario.nombres}, usa este token para activar tu cuenta:</p>
                <p><strong>$token</strong></p>
                <p>Expira en $horasVerificacionEmail horas.</p>
            """.trimIndent()
        )
    }

    private fun sendPasswordResetEmail(usuario: UsuarioEntity) {
        val token = usuario.passwordResetToken ?: return
        sendEmail(
            usuario.email,
            "Recuperacion de contrasena - Colitas Felices",
            """
                <h2>Recuperacion de contrasena</h2>
                <p>Usa este token para definir una nueva contrasena:</p>
                <p><strong>$token</strong></p>
                <p>Expira en $minutosResetPassword minutos.</p>
            """.trimIndent()
        )
    }

    private fun sendTwoFactorEmail(usuario: UsuarioEntity) {
        val code = usuario.twoFactorCode ?: return
        sendEmail(
            usuario.email,
            "Codigo de seguridad 2FA - Colitas Felices",
            """
                <h2>Codigo de seguridad</h2>
                <p>Tu codigo de acceso es:</p>
                <p><strong>$code</strong></p>
                <p>Expira en $minutosCodigo2FA minutos.</p>
            """.trimIndent()
        )
    }

    private fun sendAccountLockedEmail(usuario: UsuarioEntity) {
        sendEmail(
            usuario.email,
            "Cuenta bloqueada temporalmente - Colitas Felices",
            """
                <h2>Cuenta bloqueada temporalmente</h2>
                <p>Detectamos varios intentos fallidos de inicio de sesion.</p>
                <p>Tu cuenta estara bloqueada hasta ${usuario.bloqueadoHasta}.</p>
            """.trimIndent()
        )
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
        usuarioEntity.emailVerificado = false
        usuarioEntity.emailVerificacionToken = tokenGenerator()
        usuarioEntity.emailVerificacionExpira = LocalDateTime.now().plusHours(horasVerificacionEmail)
        val savedEntity = usuarioRepository.save(usuarioEntity)
        sendVerificationEmail(savedEntity)
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
        return when (val result = authenticate(email, password)) {
            is LoginResult.Success -> result.usuario
            else -> null
        }
    }

    fun authenticate(email: String, password: String): LoginResult {
        logger.info("Intento de login para: $email")
        val usuarioEntity = usuarioRepository.findByEmail(email) ?: return LoginResult.InvalidCredentials
        val now = LocalDateTime.now()
        val bloqueadoHasta = usuarioEntity.bloqueadoHasta
        if (bloqueadoHasta != null && bloqueadoHasta.isAfter(now)) {
            return LoginResult.Locked(bloqueadoHasta)
        }
        if (bloqueadoHasta != null && !bloqueadoHasta.isAfter(now)) {
            usuarioEntity.intentosFallidos = 0
            usuarioEntity.bloqueadoHasta = null
        }

        if (usuarioEntity.password != password) {
            usuarioEntity.intentosFallidos += 1
            if (usuarioEntity.intentosFallidos >= maxIntentosFallidos) {
                usuarioEntity.bloqueadoHasta = now.plusMinutes(minutosBloqueo)
                usuarioEntity.token = null
                val saved = usuarioRepository.save(usuarioEntity)
                sendAccountLockedEmail(saved)
                return LoginResult.Locked(saved.bloqueadoHasta!!)
            }
            usuarioRepository.save(usuarioEntity)
            return LoginResult.InvalidCredentials
        //Bloqueador de cuentas eliminadas
        if (usuarioEntity.fechaEliminado != null) {
            logger.warn("Login fallido: cuenta eliminada para: $email")
            return null
        }
        if (!PasswordUtil.matches(password, usuarioEntity.password)) {
            logger.warn("Login fallido: contrasena incorrecta para: $email")
            return null
        }

        usuarioEntity.intentosFallidos = 0
        usuarioEntity.bloqueadoHasta = null
        if (!usuarioEntity.emailVerificado) {
            usuarioEntity.emailVerificacionToken = tokenGenerator()
            usuarioEntity.emailVerificacionExpira = now.plusHours(horasVerificacionEmail)
            val saved = usuarioRepository.save(usuarioEntity)
            sendVerificationEmail(saved)
            return LoginResult.EmailNotVerified
        }

        if (usuarioEntity.twoFactorEnabled) {
            usuarioEntity.twoFactorCode = generateSixDigitCode()
            usuarioEntity.twoFactorExpira = now.plusMinutes(minutosCodigo2FA)
            usuarioEntity.token = null
            val saved = usuarioRepository.save(usuarioEntity)
            sendTwoFactorEmail(saved)
            return LoginResult.TwoFactorRequired
        }

        usuarioEntity.token = tokenGenerator()
        val savedEntity = usuarioRepository.save(usuarioEntity)
        logger.info("Login exitoso para: ${savedEntity.email}")
        return LoginResult.Success(savedEntity.toUsuario())
        // Notificacion del ingreso exitoso
        val cuerpoIngreso = """
        <html><body>
        <p>Hola <strong>${savedEntity.nombres}</strong>,</p>
        <p>Se ha detectado un nuevo inicio de sesion en tu cuenta de <strong>Colitas Felices</strong>.</p>
        <p>Si fuiste tu, puedes ignorar este mensaje.</p>
        <p>Si no reconoces esta actividad, por favor contacta a soporte de inmediato.</p>
        <br><p>Saludos,<br>Colitas Felices</p>
        </body></html>
    """.trimIndent()
        val resultadoCorreo = mailAdapter.sendHtmlEmail(
            to = savedEntity.email,
            subject = "Nuevo inicio de sesion en Colitas Felices",
            htmlBody = cuerpoIngreso
        )
        if (resultadoCorreo.isFailure) {
            logger.warn("No se pudo enviar correo de notificacion de ingreso a: ${savedEntity.email}")
        }
        return savedEntity.toUsuario()
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

    fun verifyEmail(token: String): Boolean {
        val usuario = usuarioRepository.findByEmailVerificacionToken(token) ?: return false
        val expira = usuario.emailVerificacionExpira
        if (expira == null || expira.isBefore(LocalDateTime.now())) return false
        usuario.emailVerificado = true
        usuario.emailVerificacionToken = null
        usuario.emailVerificacionExpira = null
        usuarioRepository.save(usuario)
        return true
    }

    fun requestPasswordReset(email: String) {
        val usuario = usuarioRepository.findByEmail(email) ?: return
        usuario.passwordResetToken = tokenGenerator()
        usuario.passwordResetExpira = LocalDateTime.now().plusMinutes(minutosResetPassword)
        val saved = usuarioRepository.save(usuario)
        sendPasswordResetEmail(saved)
    }

    fun resetPassword(token: String, newPassword: String): Boolean {
        val usuario = usuarioRepository.findByPasswordResetToken(token) ?: return false
        val expira = usuario.passwordResetExpira
        if (expira == null || expira.isBefore(LocalDateTime.now())) return false
        usuario.password = hashPassword(newPassword)
        usuario.passwordResetToken = null
        usuario.passwordResetExpira = null
        usuario.intentosFallidos = 0
        usuario.bloqueadoHasta = null
        usuario.token = null
        usuario.fechaUpdate = LocalDateTime.now()
        usuarioRepository.save(usuario)
        return true
    }

    fun enableTwoFactor(token: String, enabled: Boolean): Boolean {
        val usuario = usuarioRepository.findByToken(token) ?: return false
        usuario.twoFactorEnabled = enabled
        usuario.twoFactorCode = null
        usuario.twoFactorExpira = null
        usuario.fechaUpdate = LocalDateTime.now()
        usuarioRepository.save(usuario)
        return true
    }

    fun verifyTwoFactor(email: String, code: String): Usuario? {
        val usuario = usuarioRepository.findByEmail(email) ?: return null
        val expira = usuario.twoFactorExpira
        if (usuario.twoFactorCode != code || expira == null || expira.isBefore(LocalDateTime.now())) {
            return null
        }
        usuario.twoFactorCode = null
        usuario.twoFactorExpira = null
        usuario.intentosFallidos = 0
        usuario.bloqueadoHasta = null
        usuario.token = tokenGenerator()
        val saved = usuarioRepository.save(usuario)
        return saved.toUsuario()
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
