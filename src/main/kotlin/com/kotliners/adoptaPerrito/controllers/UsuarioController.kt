package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.domain.toUsuario
import com.kotliners.adoptaPerrito.dto.request.CreateUsuarioRequest
import com.kotliners.adoptaPerrito.dto.request.LoginRequest
import com.kotliners.adoptaPerrito.dto.request.UpdateUsuarioRequest
import com.kotliners.adoptaPerrito.dto.response.RegisterResponse
import com.kotliners.adoptaPerrito.dto.response.LogoutResponse
import com.kotliners.adoptaPerrito.dto.response.LoginResponse
import com.kotliners.adoptaPerrito.dto.response.UsuarioResponse
import com.kotliners.adoptaPerrito.dto.response.toUsuarioResponse
import com.kotliners.adoptaPerrito.services.UsuarioService
import com.kotliners.adoptaPerrito.utils.TokenExtractor
import com.kotliners.adoptaPerrito.utils.PasswordUtil      

import jakarta.validation.Valid

import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controlador REST encargado de exponer los endpoints relacionados
 * con la gestión de usuarios en el sistema de adopción de perros.
 *
 * Este controlador maneja las operaciones principales de:
 * - Registro de nuevos usuarios
 * - Autenticación (login/logout)
 * - Gestión de sesiones mediante tokens
 * - Información del usuario actual
 *
 * Utiliza Spring Web para mapear las peticiones HTTP a métodos específicos,
 * y Spring Security concepts para la gestión de autenticación basada en tokens.
 */
@RestController
@RequestMapping("/usuarios")
class UsuarioController {

    /**
     * Logger para registrar eventos importantes del controlador.
     * Facilita el debugging y monitoreo del comportamiento de la API.
     */
    private val logger: Logger = LoggerFactory.getLogger(UsuarioController::class.java)

    /**
     * Servicio de usuarios inyectado por Spring.
     * Contiene toda la lógica de negocio para operaciones con usuarios.
     */
    @Autowired
    lateinit var userService: UsuarioService

    /**
     * Endpoint para obtener la información del usuario actualmente autenticado.
     * Este endpoint permite recuperar los datos del usuario a partir del token
     * de sesión enviado en el header Authorization.
     * URL:    http://localhost:8080/usuarios/me
     * Metodo: GET
     * Headers: Authorization: Bearer <token>
     * @param token Token de sesión enviado en el header Authorization.
     * @return ResponseEntity con los datos del usuario autenticado y código HTTP 200 (OK),
     * o código HTTP 401 (Unauthorized) si el token es inválido o no se proporciona.
     */
    @GetMapping("/me")
    fun getCurrentUser(
        @RequestHeader("Authorization", required = true) token: String?
    ): ResponseEntity<Any> {
        logger.info("Token recibido en /me: ${token?.take(8)}...")
        val userFound = TokenExtractor.resolveUser(token, userService)
            ?: return ResponseEntity.status(401).body(if (token == null) "Token requerido" else "Token inválido")

        logger.info("Usuario autenticado: ${userFound.email}")
        return ResponseEntity.ok(userFound.toUsuarioResponse())
    }

    /**
     * Hashea una contrasena usando BCrypt (incluye salt automaticamente).
     * @param password Contrasena en texto plano.
     * @return Hash BCrypt de la contrasena.
     */
    private fun hashPassword(password: String): String = PasswordUtil.hash(password)

    /**
     * Endpoint para registrar un nuevo usuario en el sistema.
     * 
     * Recibe un JSON con los datos necesarios para crear un nuevo usuario
     * y los transforma en un objeto dominio.
     * 
     * URL:    http://localhost:8080/usuarios/register
     * Metodo: POST
     * 
     * @param createUsuarioRequest DTO que contiene los datos necesarios para crear un nuevo usuario.
     * @Return ResponseEntity con la respuesta de registro y código HTTP 201 (Created).
     */
    @PostMapping("/register")
    fun agregaUsuario(
        @Valid @RequestBody createUsuarioRequest: CreateUsuarioRequest
    ): ResponseEntity<RegisterResponse> {
        logger.info("Solicitud de registro recibida para: ${createUsuarioRequest.email}")
        val usuarioCreado = createUsuarioRequest.toUsuario()
        val usuarioConPasswordHash = usuarioCreado.copy(password = PasswordUtil.hash(usuarioCreado.password))
        val usuarioGuardado = userService.addNewUsuario(usuarioConPasswordHash)
        logger.info("Usuario registrado exitosamente: ${usuarioGuardado.email}")
        return ResponseEntity.status(201).body(
            RegisterResponse(usuario = usuarioGuardado.toUsuarioResponse(), mensaje = "Usuario registrado exitosamente")
        )
    }

    /**
     * Endpoint para el proceso de autenticación de un usuario.
     *
     * Este endpoint realiza el login del usuario mediante las siguientes operaciones:
     * 1. Recibe las credenciales (email y password) en el cuerpo de la petición
     * 2. Hashea la contraseña proporcionada usando SHA-256
     * 3. Busca en la base de datos un usuario con email y contraseña hasheada coincidentes
     * 4. Si encuentra coincidencia, genera un nuevo token de sesión y lo guarda
     * 5. Retorna el token de sesión en caso de éxito, o error 401 si falla
     *
     * El token generado se utiliza para mantener la sesión activa y autorizar
     * futuras peticiones sin necesidad de reautenticación.
     *
     * URL:    http://localhost:8080/usuarios/login
     * Método: POST
     *
     * @param loginRequest DTO que contiene las credenciales de login (email y password)
     * @return ResponseEntity con LoginResponse (token) y código HTTP 200 (OK),
     *         o código HTTP 401 (Unauthorized) si las credenciales son incorrectas
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest
    ): ResponseEntity<Any> {
        logger.info("Intento de login con: ${loginRequest.email}")

        val userFound = userService.login(loginRequest.email, loginRequest.password)

        return if (userFound != null) {
            logger.info("Login exitoso para: ${userFound.email}")
            ResponseEntity.ok(LoginResponse(userFound.token.orEmpty()))
        } else {
            logger.warn("Login fallido para: ${loginRequest.email}")
            ResponseEntity.status(401).body("Credenciales incorrectas")
        }
    }

    /**
     * Endpoint para el proceso de logout de un usuario.
     *
     * Este endpoint invalida la sesión activa del usuario mediante:
     * 1. Recibe el token de autorización en el header Authorization
     * 2. Valida que el token corresponda a un usuario existente
     * 3. Limpia el token de la base de datos (lo establece como null)
     * 4. Retorna confirmación de logout exitoso
     *
     * En una implementación de producción, también se debería:
     * - Invalidar el token en un sistema de cache distribuido (Redis)
     * - Registrar el evento de logout para auditoría
     * - Limpiar cualquier estado de sesión adicional
     *
     * URL:    http://localhost:8080/usuarios/logout
     * Método: POST
     * Headers: Authorization: Bearer <token>
     *
     * @param token Token de sesión obtenido durante el login
     * @return ResponseEntity con LogoutResponse y código HTTP 200 (OK),
     *         o código HTTP 401 (Unauthorized) si el token es inválido
     */
    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization", required = true) token: String?
    ): ResponseEntity<Any> {
        logger.info("Solicitud de logout con token: ${token?.take(8)}...")
        val userFound = TokenExtractor.resolveUser(token, userService)
            ?: return ResponseEntity.status(401).body(if (token == null) "Token requerido" else "Token inválido")

        userService.invalidateToken(userFound.id)
        logger.info("Token invalidado para: ${userFound.email}")

        return ResponseEntity.ok(
            LogoutResponse(userId = userFound.id, logoutDateTime = LocalDateTime.now().toString())
        )
    }


    /**
     * Endpoint para actualizar la información del usuario autenticado.
     *
     * Recibe el token en el header Authorization para identificar al usuario,
     * y un JSON con los campos a actualizar.
     *
     * URL:    http://localhost:8080/usuarios
     * Método: PUT
     * Headers: Authorization: Bearer <token>
     *
     * @param token Token de sesión del usuario autenticado
     * @param updateUsuarioRequest DTO con los datos a actualizar
     * @return ResponseEntity con el usuario actualizado y código HTTP 200 (OK)
     */
    @PutMapping
    fun updateUsuario(
        @RequestHeader("Authorization", required = true) token: String?,
        @RequestBody  @Valid updateUsuarioRequest: UpdateUsuarioRequest
    ): ResponseEntity<Any> {
        logger.info("Solicitud de actualización recibida")
        val userFound = TokenExtractor.resolveUser(token, userService)
            ?: return ResponseEntity.status(401).body(if (token == null) "Token requerido" else "Token inválido")

        val usuarioActualizado = userService.updateUsuario(userFound.id ?: return ResponseEntity.status(401).body("Token inválido"), updateUsuarioRequest)
        return if (usuarioActualizado != null) {
            logger.info("Usuario actualizado: ${usuarioActualizado.id}")
            ResponseEntity.ok(usuarioActualizado.toUsuarioResponse())
        } else {
            ResponseEntity.status(404).body("Usuario no encontrado")
        }
    }
}
