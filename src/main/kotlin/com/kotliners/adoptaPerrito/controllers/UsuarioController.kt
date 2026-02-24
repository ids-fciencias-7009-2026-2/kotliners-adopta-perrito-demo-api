package com.kotliners.adoptaPerrito.controllers

// ToDO: validar que los campos coincidan con la clase Usuario del dominio
// ToDO: Verificar los nombres de las clases
import com.kotliners.adoptaPerrito.domain.Usuario 
import com.kotliners.adoptaPerrito.domain.toUsuario
import com.dunder.mifflin.paper.dunderSys.dto.request.CreateUsuarioRequest
import com.dunder.mifflin.paper.dunderSys.dto.request.LoginRequest
import com.dunder.mifflin.paper.dunderSys.dto.request.UpdateUsuarioRequest
import com.dunder.mifflin.paper.dunderSys.dto.response.LogoutResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * Controlador encargado de exponer los endpoints REST relacionados 
 * con la gestión de usuarios.
 */
@RequestMapping("/usuarios") 
class UsuarioController {

    /* Logger para registrar eventos importantes del flujo de ejecución. */
    private val logger: Logger = LoggerFactory.getLogger(UsuarioController::class.java)

    /**
     * Endpoint que devuelve la información del usuario autenticado.
     * 
     * URL:    http://localhost:8080/usuarios/me
     * Metodo: GET
     * 
     * @Return ResponseEntity con un objeto Usuario y código HTTP 200 (OK).
     */
    @GetMapping("/me")
    fun retrieveUsuario(): ResponseEntity<Usuario> {
        // ToDO: Verificar campos con clase Usuario
        val usuarioFake = Usuario(
            id = "1234",
            nombre = "Aureliano Buendía",
            email = "aureliano.buendia@gmail.com",
            cp = "12345",
        )
        logger.info("Usuario recuperado: $usuarioFake")
        return ResponseEntity.ok(usuarioFake)
    }

    /**
     * Endpoint para registrar un nuevo usuario en el sistema.
     * 
     * URL:    http://localhost:8080/usuarios/register
     * Metodo: POST
     * 
     * @param createUsuarioRequest DTO que contiene los datos necesarios para crear un nuevo usuario.
     * @Return ResponseEntity con el usuario creado y código HTTP 200 (OK).
     */
    @PostMapping("/register")
    fun agregaUsuario(
        @RequestBody createUsuarioRequest: CreateUsuarioRequest
    ): ResponseEntity<Usuario> {
        logger.info("Solicitud de registro recibida: $createUsuarioRequest")
        val usuarioCreado = createUsuarioRequest.toUsuario()
        logger.info("Usuario creado: $usuarioCreado")
        return ResponseEntity.ok(usuarioCreado)
    }

    /**
     * Endpoint para simula el proceso de autenticación de un usuario.
     * 
     * URL:    http://localhost:8080/usuarios/login
     * Metodo: POST
     * 
     * @param loginRequest DTO que contiene las credenciales de login (email y password).
     * @Return ResponseEntity con un mensaje de éxito o error y código HTTP: 200 (OK) o 401 (Unauthorized).
     */
    @PostMapping("/login")
    fun login(
        @RequestBody loginRequest: LoginRequest
    ): ResponseEntity<String> {
        logger.info("Solicitud de login recibida: $loginRequest")
        // ToDO: Verificar campos con clase Usuario
        val usuarioFake = Usuario(
            id = "1234",
            nombre = "Aureliano Buendía",
            email = "aureliano.buendia@gmail.com",
            cp = "12345",
            password = "macondo123"
        )
        logger.info("Intentando login para usuario: ${loginRequest.id}")
        return if (
            loginRequest.email == usuarioFake.email &&
            loginRequest.password == usuarioFake.password
        ) {
            logger.info("Login exitoso para usuario: ${loginRequest.id}")
            ResponseEntity.ok("Login exitoso")
        } else {
            logger.warn("Login fallido para usuario: ${loginRequest.id}")
            ResponseEntity.status(401).body("Credenciales inválidas")
        }
    }


}