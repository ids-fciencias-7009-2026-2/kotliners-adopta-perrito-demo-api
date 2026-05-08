package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.domain.AnimalInteres
import com.kotliners.adoptaPerrito.domain.Rol
import com.kotliners.adoptaPerrito.domain.Usuario
import com.kotliners.adoptaPerrito.dto.response.AnimalInteresResponse
import com.kotliners.adoptaPerrito.services.InteresService
import com.kotliners.adoptaPerrito.services.UsuarioService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit tests para InteresController.
 * Se mockean InteresService y UsuarioService para aislar la logica del controlador.
 */
class InteresControllerTest {

    private lateinit var controller: InteresController
    private lateinit var interesService: InteresService
    private lateinit var usuarioService: UsuarioService

    private val usuarioAdoptante = Usuario(
        id = "00000000-0000-0000-0000-000000001111",
        curp = "ABCD123456HDFXXX01",
        username = "testuser",
        rol = Rol.ADOPTANTE,
        nombres = "Juan",
        apellidoPaterno = "Perez",
        apellidoMaterno = "Lopez",
        email = "juan@test.com",
        codigoPostal = "06600",
        password = "hashedpassword",
        token = "valid-token-123"
    )

    private val animalId = "00000000-0000-0000-0000-000000002222"

    private fun interesBase() = AnimalInteres(
        usuarioId = usuarioAdoptante.id!!,
        animalId = animalId,
        fecha = LocalDateTime.now()
    )

    private fun interesResponseBase() = AnimalInteresResponse(
        animalId = animalId,
        nombre = "Luna",
        especie = "Gato",
        raza = null,
        fechaNacimiento = LocalDate.of(2022, 1, 1),
        sexo = "HEMBRA",
        descripcion = "Gata tranquila",
        estatus = "DISPONIBLE",
        esterilizado = true,
        fechaInteres = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        interesService  = mock(InteresService::class.java)
        usuarioService  = mock(UsuarioService::class.java)
        controller = InteresController()
        controller.interesService = interesService
        controller.usuarioService = usuarioService
    }

    // -------------------------------------------------------------------------
    // POST /api/animales/{id}/interes
    // -------------------------------------------------------------------------

    @Test
    fun `manifestarInteres retorna 401 cuando token es null`() {
        val response = controller.manifestarInteres(null, animalId)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `manifestarInteres retorna 401 cuando token es invalido`() {
        `when`(usuarioService.findByToken("bad-token")).thenReturn(null)
        val response = controller.manifestarInteres("Bearer bad-token", animalId)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `manifestarInteres retorna 201 cuando token es valido y usuario es ADOPTANTE`() {
        `when`(usuarioService.findByToken("valid-token-123")).thenReturn(usuarioAdoptante)
        `when`(interesService.manifestarInteres(usuarioAdoptante.id!!, Rol.ADOPTANTE, animalId))
            .thenReturn(interesBase())

        val response = controller.manifestarInteres("Bearer valid-token-123", animalId)
        assertEquals(HttpStatus.CREATED, response.statusCode)
    }

    // -------------------------------------------------------------------------
    // DELETE /api/animales/{id}/interes
    // -------------------------------------------------------------------------

    @Test
    fun `eliminarInteres retorna 401 cuando token es null`() {
        val response = controller.eliminarInteres(null, animalId)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `eliminarInteres retorna 401 cuando token es invalido`() {
        `when`(usuarioService.findByToken("bad-token")).thenReturn(null)
        val response = controller.eliminarInteres("Bearer bad-token", animalId)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `eliminarInteres retorna 200 cuando token es valido`() {
        `when`(usuarioService.findByToken("valid-token-123")).thenReturn(usuarioAdoptante)
        doNothing().`when`(interesService).eliminarInteres(usuarioAdoptante.id!!, animalId)

        val response = controller.eliminarInteres("Bearer valid-token-123", animalId)
        assertEquals(HttpStatus.OK, response.statusCode)
    }

    // -------------------------------------------------------------------------
    // GET /api/usuarios/me/intereses
    // -------------------------------------------------------------------------

    @Test
    fun `listarIntereses retorna 401 cuando token es null`() {
        val response = controller.listarIntereses(null)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `listarIntereses retorna 401 cuando token es invalido`() {
        `when`(usuarioService.findByToken("bad-token")).thenReturn(null)
        val response = controller.listarIntereses("Bearer bad-token")
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun `listarIntereses retorna 200 con lista cuando token es valido`() {
        `when`(usuarioService.findByToken("valid-token-123")).thenReturn(usuarioAdoptante)
        `when`(interesService.listarIntereses(usuarioAdoptante.id!!)).thenReturn(listOf(interesResponseBase()))

        val response = controller.listarIntereses("Bearer valid-token-123")
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body as List<*>
        assertEquals(1, body.size)
    }

    @Test
    fun `listarIntereses retorna lista vacia cuando usuario no tiene intereses`() {
        `when`(usuarioService.findByToken("valid-token-123")).thenReturn(usuarioAdoptante)
        `when`(interesService.listarIntereses(usuarioAdoptante.id!!)).thenReturn(emptyList())

        val response = controller.listarIntereses("Bearer valid-token-123")
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body as List<*>
        assertTrue(body.isEmpty())
    }
}
