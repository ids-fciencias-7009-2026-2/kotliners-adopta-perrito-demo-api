package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.adapters.MailAdapter
import com.kotliners.adoptaPerrito.domain.Rol
import com.kotliners.adoptaPerrito.entities.UsuarioEntity
import com.kotliners.adoptaPerrito.repositories.CodigoPostalRepository
import com.kotliners.adoptaPerrito.repositories.UsuarioRepository
import com.kotliners.adoptaPerrito.utils.PasswordUtil
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

class NotificacionServiceTest {
    private lateinit var service: UsuarioService
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var codigoPostalRepository: CodigoPostalRepository
    private lateinit var mailAdapter: MailAdapter

    private val TEST_UUID = UUID.fromString("00000000-0000-0000-0000-000000001234")

    private fun entityBase(fechaEliminado: LocalDateTime? = null) = UsuarioEntity(
        id = TEST_UUID,
        curp = "ABCD123456HDFXXX01",
        username = "testuser",
        rol = Rol.ADOPTANTE,
        nombres = "Ana",
        apellidoPaterno = "Garcia",
        apellidoMaterno = "Lopez",
        email = "ana@test.com",
        codigoPostal = "06600",
        password = PasswordUtil.hash("password123"),
        token = "token-valido",
        fechaEliminado = fechaEliminado
    )

    @BeforeEach
    fun setUp() {
        usuarioRepository = mock(UsuarioRepository::class.java)
        codigoPostalRepository = mock(CodigoPostalRepository::class.java)
        mailAdapter = mock(MailAdapter::class.java)
        service = UsuarioService()
        service.usuarioRepository = usuarioRepository
        service.codigoPostalRepository = codigoPostalRepository
        service.mailAdapter = mailAdapter
        `when`(codigoPostalRepository.existsById(anyString())).thenReturn(true)
    }

    //Notificacion de ingreso
    @Test
    fun `login envia correo de notificacion cuando credenciales son correctas`() {
        val entity = entityBase()
        `when`(usuarioRepository.findByEmail("ana@test.com")).thenReturn(entity)
        `when`(usuarioRepository.save(any(UsuarioEntity::class.java))).thenAnswer { it.arguments[0] }
        `when`(mailAdapter.sendHtmlEmail(anyString(), anyString(), anyString(), isNull()))
            .thenReturn(Result.success(Unit))
        val result = service.login("ana@test.com", "password123")
        assertNotNull(result)
        verify(mailAdapter).sendHtmlEmail(
            eq("ana@test.com"),
            eq("Nuevo inicio de sesion en Colitas Felices"),
            anyString(),
            isNull()
        )
    }
    @Test
    fun `login no falla si el correo de notificacion falla`() {
        val entity = entityBase()
        `when`(usuarioRepository.findByEmail("ana@test.com")).thenReturn(entity)
        `when`(usuarioRepository.save(any(UsuarioEntity::class.java))).thenAnswer { it.arguments[0] }
        `when`(mailAdapter.sendHtmlEmail(anyString(), anyString(), anyString(), isNull()))
            .thenReturn(Result.failure(RuntimeException("SMTP error")))

        val result = service.login("ana@test.com", "password123")
        assertNotNull(result)
    }

    @Test
    fun `login retorna null para cuenta con fecha de eliminacion`() {
        val entity = entityBase(fechaEliminado = LocalDateTime.now().minusDays(1))
        `when`(usuarioRepository.findByEmail("ana@test.com")).thenReturn(entity)

        val result = service.login("ana@test.com", "password123")
        assertNull(result)
    }

    // --- Eliminacion de cuenta ---

    @Test
    fun `eliminarCuenta llama softDeleteById y envia correo`() {
        val entity = entityBase()
        `when`(usuarioRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity))
        doNothing().`when`(usuarioRepository).softDeleteById(eq(TEST_UUID), any(LocalDateTime::class.java))
        `when`(mailAdapter.sendHtmlEmail(anyString(), anyString(), anyString(), isNull()))
            .thenReturn(Result.success(Unit))

        service.eliminarCuenta(TEST_UUID.toString())

        verify(usuarioRepository).softDeleteById(eq(TEST_UUID), any(LocalDateTime::class.java))
        verify(mailAdapter).sendHtmlEmail(
            eq("ana@test.com"),
            eq("Tu cuenta en Colitas Felices ha sido eliminada"),
            anyString(),
            isNull()
        )
    }

    @Test
    fun `eliminarCuenta lanza excepcion si usuario no existe`() {
        `when`(usuarioRepository.findById(TEST_UUID)).thenReturn(Optional.empty())
        assertThrows<IllegalArgumentException> { service.eliminarCuenta(TEST_UUID.toString()) }
    }

    @Test
    fun `eliminarCuenta lanza excepcion si la cuenta ya fue eliminada`() {
        val entity = entityBase(fechaEliminado = LocalDateTime.now().minusDays(3))
        `when`(usuarioRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity))
        assertThrows<IllegalArgumentException> { service.eliminarCuenta(TEST_UUID.toString()) }
    }

    @Test
    fun `eliminarCuenta no falla si el correo de confirmacion falla`() {
        val entity = entityBase()
        `when`(usuarioRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity))
        doNothing().`when`(usuarioRepository).softDeleteById(eq(TEST_UUID), any(LocalDateTime::class.java))
        `when`(mailAdapter.sendHtmlEmail(anyString(), anyString(), anyString(), isNull()))
            .thenReturn(Result.failure(RuntimeException("SMTP error")))

        assertDoesNotThrow { service.eliminarCuenta(TEST_UUID.toString()) }
        verify(usuarioRepository).softDeleteById(eq(TEST_UUID), any(LocalDateTime::class.java))
    }
}