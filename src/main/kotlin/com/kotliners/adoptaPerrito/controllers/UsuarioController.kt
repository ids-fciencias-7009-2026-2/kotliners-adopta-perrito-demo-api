package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.domain.Usuario 
import com.kotliners.adoptaPerrito.domain.toUsuario
import com.kotliners.adoptaPerrito.dto.request.CreateUsuarioRequest
import com.kotliners.adoptaPerrito.dto.request.LoginRequest
import com.kotliners.adoptaPerrito.dto.request.UpdateUsuarioRequest
import com.kotliners.adoptaPerrito.dto.response.RegisterResponse
import com.kotliners.adoptaPerrito.dto.response.LogoutResponse
import com.kotliners.adoptaPerrito.dto.response.LoginResponse
import com.kotliners.adoptaPerrito.services.UsuarioService      


import java.security.MessageDigest

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

import jakarta.validation.Valid

import java.time.LocalDateTime

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
     * Conjunto de tokens activos en memoria.
     * En una implementación de producción, esto debería estar en Redis o base de datos.
     * Actualmente se usa para mantener un registro simple de sesiones activas.
     */
    val activeTokens = mutableSetOf<String>()

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
        @RequestHeader("Authorization") token: String?
    ): ResponseEntity<Any> {
        logger.info("Solicitud /me recibida con token: $token")
        if (token == null) {
            return ResponseEntity.status(401).body("Token requerido")
        }
        val cleanToken = token.replace("Bearer ", "").trim()
        val userFound = userService.findByToken(cleanToken)
        return if (userFound != null) {
            logger.info("Usuario autenticado: ${userFound.email}")
            ResponseEntity.ok(
                mapOf(
                    "fotoPerfil" to userFound.fotoPerfil,
                    "id" to userFound.id,
                    "email" to userFound.email,
                    "username" to userFound.username,
                    "rol" to userFound.rol,
                    "nombres" to userFound.nombres,
                    "apellidoPaterno" to userFound.apellidoPaterno,
                    "apellidoMaterno" to userFound.apellidoMaterno,
                    "codigoPostal" to userFound.codigoPostal,
                    "curp" to userFound.curp
                )
            )
        } else {
            logger.warn("Token inválido en /me")
            ResponseEntity.status(401).body("Token inválido")
        }
    }
    /**
     * Función auxiliar para hash de contraseñas usando SHA-256. 
     * 
     * @param password Contraseña en texto plano.
     * @return Contraseña hasheada en formato hexadecimal.
     */
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

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
        logger.info("Solicitud de registro recibida: $createUsuarioRequest")
        val usuarioCreado = createUsuarioRequest.toUsuario()
        val hashedPassword = hashPassword(usuarioCreado.password)
        val usuarioConPasswordHash = usuarioCreado.copy(password = hashedPassword)
        val usuarioGuardado = userService.addNewUsuario(usuarioConPasswordHash)
        logger.info("Usuario registrado exitosamente: $usuarioGuardado")
        
        val registerResponse = RegisterResponse(
            usuario = usuarioGuardado,
            mensaje = "Usuario registrado exitosamente"
        )
        
        return ResponseEntity.status(201).body(registerResponse)
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

        // Hashear la contraseña proporcionada para comparación con la BD
        val passwordHash = hashPassword(loginRequest.password)
        logger.info("Contraseña hasheada de la petición: $passwordHash")
  
        // Intentar autenticar al usuario con email y contraseña hasheada
        val userFound = userService.login(loginRequest.email, passwordHash)

        // Logging detallado para debugging
        logger.info("Intento de login con: $loginRequest")
        logger.info("Usuario encontrado: ${userFound != null}")
        if (userFound != null) {
            logger.info("Token generado para usuario: ${userFound.token}")
        }

        // Retornar respuesta apropiada según resultado de autenticación
        return if (userFound != null) {
            logger.info("Login exitoso para usuario: ${userFound.email}")
            activeTokens.add(userFound.token.orEmpty())
            ResponseEntity.ok(LoginResponse(userFound.token.orEmpty()))
        } else {
            logger.warn("Login fallido para email: ${loginRequest.email}")
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
        @RequestHeader("Authorization") token: String?
    ): ResponseEntity<Any> {
        logger.info("Solicitud de logout recibida con token: ${token}...")
       
        // Validar que el token existe y corresponde a un usuario válido
        val userFound = userService.findByToken(token.orEmpty())
        if (token == null || userFound == null) {
            logger.warn("Token inválido o usuario no encontrado para logout")
            return ResponseEntity.status(401).body("Token inválido")
        }

        // Invalidar el token en la base de datos (ponerlo como null)
        userService.invalidateToken(userFound.id)  
        logger.info("Token invalidado para usuario: ${userFound.email}")
        activeTokens.remove(token)

        // Crear respuesta con el usuario real y timestamp actual
        val logoutResponse = LogoutResponse(
            userId = userFound.id,  
            logoutDateTime = LocalDateTime.now().toString()  
        )

        return ResponseEntity.ok(logoutResponse)
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
        @RequestHeader("Authorization") token: String?,
        @RequestBody updateUsuarioRequest: UpdateUsuarioRequest
    ): ResponseEntity<Any> {
        logger.info("Solicitud de actualización recibida")
        if (token == null) {
            return ResponseEntity.status(401).body("Token requerido")
        }
        val cleanToken = token.replace("Bearer ", "").trim()
        val userFound = userService.findByToken(cleanToken)
        if (userFound == null) {
            logger.warn("Token inválido en PUT /usuarios")
            return ResponseEntity.status(401).body("Token inválido")
        }
        val usuarioActualizado = userService.updateUsuario(userFound.id!!, updateUsuarioRequest)
        return if (usuarioActualizado != null) {
            logger.info("Usuario actualizado exitosamente: ${usuarioActualizado.id}")
            ResponseEntity.ok(usuarioActualizado)
        } else {
            ResponseEntity.status(404).body("Usuario no encontrado")
        }
    }
}
