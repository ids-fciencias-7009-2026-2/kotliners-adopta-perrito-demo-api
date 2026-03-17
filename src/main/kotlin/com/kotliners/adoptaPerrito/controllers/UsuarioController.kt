package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.domain.Usuario 
import com.kotliners.adoptaPerrito.domain.toUsuario
import com.kotliners.adoptaPerrito.dto.request.CreateUsuarioRequest
import com.kotliners.adoptaPerrito.dto.request.LoginRequest
import com.kotliners.adoptaPerrito.dto.request.UpdateUsuarioRequest
import com.kotliners.adoptaPerrito.dto.response.LogoutResponse
import com.kotliners.adoptaPerrito.services.UsuarioService

import java.security.MessageDigest

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * Controlador encargado de exponer los endpoints REST relacionados 
 * con la gestión de usuarios.
 */
@RestController
@RequestMapping("/usuarios") 
class UsuarioController {

    /* Logger para registrar eventos importantes del flujo de ejecución. */
    private val logger: Logger = LoggerFactory.getLogger(UsuarioController::class.java)

    /* Servicio de usuarios. */
    @Autowired
    lateinit var userService: UsuarioService

    /* Conjunto de tokens activos. */
    val activeTokens = mutableSetOf<String>()

    /**
     * Endpoint que devuelve la información del usuario autenticado.
     * En esta versión de prueba se retorna un usuario "fake" con datos simulados.
     * 
     * URL:    http://localhost:8080/usuarios/me
     * Metodo: GET
     * 
     * @Return ResponseEntity con un objeto Usuario y código HTTP 200 (OK).
     */
    /* 
    @GetMapping("/me")
    fun retrieveUsuario(): ResponseEntity<Usuario> {
        val usuarioFake = Usuario(
            id = "1234",
            nombre = "Aureliano Buendía",
            email = "aureliano.buendia@gmail.com",
            cp = "12345"
        )
        logger.info("Usuario recuperado: $usuarioFake")
        return ResponseEntity.ok(usuarioFake)
    }
    */

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
     * @Return ResponseEntity con el usuario creado y código HTTP 201 (Created).
     */
    @PostMapping("/register")
    fun agregaUsuario(
        @RequestBody createUsuarioRequest: CreateUsuarioRequest
    ): ResponseEntity<Usuario> {
        logger.info("Solicitud de registro recibida: $createUsuarioRequest")
        val usuarioCreado = createUsuarioRequest.toUsuario()
        val hashedPassword = hashPassword(usuarioCreado.password)
        val usuarioConPasswordHash = usuarioCreado.copy(password = hashedPassword)
        val usuarioGuardado = userService.addNewUsuario(usuarioConPasswordHash)
        logger.info("Usuario registrado exitosamente: $usuarioGuardado")
        return ResponseEntity.status(201).body(usuarioGuardado)
    }

    /**
     * Endpoint para simula el proceso de autenticación de un usuario.
     * Recibe un JSON con las credenciales (email y password) y verifica si coinciden
     * con un usuario "fake" predefinido. 
     * 
     * URL:    http://localhost:8080/usuarios/login
     * Metodo: POST
     * 
     * @param loginRequest DTO que contiene las credenciales de login (email y password).
     * @Return ResponseEntity con un mensaje de éxito o error y código HTTP: 200 (OK) o 401 (Unauthorized).
     */
    /*
    @PostMapping("/login")
    fun login(
        @RequestBody loginRequest: LoginRequest
    ): ResponseEntity<String> {
        logger.info("Solicitud de login recibida: $loginRequest")
        val usuarioFake = Usuario(
            id = "1234",
            nombre = "Aureliano Buendía",
            email = "aureliano.buendia@gmail.com",
            cp = "12345",
            password = "macondo123"
        )
        return if (
            loginRequest.email == usuarioFake.email &&
            loginRequest.password == usuarioFake.password
        ) {
            logger.info("Login exitoso")
            ResponseEntity.ok("Login exitoso")
        } else {
            logger.warn("Login fallido")
            ResponseEntity.status(401).body("Credenciales inválidas")
        }
    }
    */

    /**
     * Endpoint para simular el proceso de logout de un usuario.
     * 
     * Genera una respuesta de éxito con un mensaje que incluye 
     * el ID del usuario y la fecha/hora del logout.
     * 
     * URL:    http://localhost:8080/usuarios/logout
     * Metodo: POST
     * 
     * @Return ResponseEntity con un mensaje de éxito y código HTTP 200 (OK).
     */
    /*
    @PostMapping("/logout")
    fun logout(): ResponseEntity<LogoutResponse> {
        logger.info("Solicitud de logout recibida")
        val usuarioFake = Usuario(
            id = "1234",
            nombre = "Aureliano Buendía",
            email = "aureliano.buendia@gmail.com",
            cp = "12345"
        )
        val logoutResponse = LogoutResponse(
            usuarioFake.id,
            LocalDateTime.now().toString()
        )
        logger.info("Logout exitoso para usuario: ${usuarioFake.id}")
        return ResponseEntity.ok(logoutResponse)
    }
    */

    /**
     * Endpoint para actualizar la información de un usuario existente.
     * 
     * Permite modificar campos como el email o la contraseña. Recibe un JSON con los datos a actualizar
     * y retorna el usuario actualizado.
     * 
     * URL:    http://localhost:8080/usuarios
     * Metodo: PUT
     * 
     * @param updateUsuarioRequest DTO que contiene los datos a actualizar (email, password).
     * @Return ResponseEntity con el usuario actualizado y código HTTP 200 (OK).
     */
    /*
    @PutMapping
    fun updateUsuario(
        @RequestBody updateUsuarioRequest: UpdateUsuarioRequest
    ): ResponseEntity<Usuario> {
        logger.info("Solicitud de actualización recibida: $updateUsuarioRequest")
        val usuarioFake = Usuario(
            id = "1234",
            nombre = "Aureliano Buendía",
            email = "aureliano.buendia@gmail.com",
            cp = "12345",
            password = "macondo123"
        )
        logger.info("Usuario encontrado: $usuarioFake")
        val usuarioActualizado = usuarioFake.copy(
            cp = updateUsuarioRequest.cp, 
            email = updateUsuarioRequest.email,
            password = updateUsuarioRequest.password
        )
        logger.info("Usuario actualizado: $usuarioActualizado")
        return ResponseEntity.ok(usuarioActualizado)
    }
    */
}
