package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.domain.Rol
import com.kotliners.adoptaPerrito.domain.Usuario
import com.kotliners.adoptaPerrito.domain.toUsuario
import com.kotliners.adoptaPerrito.entities.UsuarioEntity
import com.kotliners.adoptaPerrito.repositories.CodigoPostalRepository
import com.kotliners.adoptaPerrito.repositories.UsuarioRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.util.Optional

/**
 * Unit tests para UsuarioService.
 * Se mockean los repositorios para aislar la lógica de negocio.
 */
class UsuarioServiceTest {

    private lateinit var service: UsuarioService
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var codigoPostalRepository: CodigoPostalRepository

    private fun entityBase(token: String? = null) = UsuarioEntity(
        id = java.util.UUID.fromString("00000000-0000-0000-0000-000000001234"),
        curp = "ABCD123456HDFXXX01",
        username = "testuser",
        rol = Rol.ADOPTANTE,
        nombres = "Juan",
        apellidoPaterno = "Pérez",
        apellidoMaterno = "López",
        email = "juan@test.com",
        codigoPostal = "06600",
        password = "hashedpassword",
        token = token
    )

    @BeforeEach
    fun setUp() {
        usuarioRepository = mock(UsuarioRepository::class.java)
        codigoPostalRepository = mock(CodigoPostalRepository::class.java)
        service = UsuarioService()
        service.usuarioRepository = usuarioRepository
        service.codigoPostalRepository = codigoPostalRepository
        // CP siempre existe por defecto
        `when`(codigoPostalRepository.existsById(anyString())).thenReturn(true)
    }

    // -------------------------------------------------------------------------
    // tokenGenerator
    // -------------------------------------------------------------------------

    @Test
    fun `tokenGenerator genera tokens distintos en cada llamada`() {
        val t1 = service.tokenGenerator()
        val t2 = service.tokenGenerator()
        assertNotEquals(t1, t2)
    }

    @Test
    fun `tokenGenerator genera tokens no vacios`() {
        val token = service.tokenGenerator()
        assertTrue(token.isNotBlank())
    }

    // -------------------------------------------------------------------------
    // findByToken
    // -------------------------------------------------------------------------

    @Test
    fun `findByToken retorna null cuando token no existe`() {
        `when`(usuarioRepository.findByToken("no-existe")).thenReturn(null)
        assertNull(service.findByToken("no-existe"))
    }

    @Test
    fun `findByToken retorna usuario cuando token es valido`() {
        val entity = entityBase(token = "valid-token")
        `when`(usuarioRepository.findByToken("valid-token")).thenReturn(entity)
        val result = service.findByToken("valid-token")
        assertNotNull(result)
        assertEquals("juan@test.com", result!!.email)
    }

    // -------------------------------------------------------------------------
    // login
    // -------------------------------------------------------------------------

    @Test
    fun `login retorna null cuando credenciales son incorrectas`() {
        `when`(usuarioRepository.findUserByPasswordAndEmail(anyString(), anyString())).thenReturn(null)
        assertNull(service.login("noexiste@test.com", "wronghash"))
    }

    @Test
    fun `login retorna usuario con token cuando credenciales son correctas`() {
        val entity = entityBase()
        `when`(usuarioRepository.findUserByPasswordAndEmail("juan@test.com", "hashedpassword"))
            .thenReturn(entity)
        `when`(usuarioRepository.save(any(UsuarioEntity::class.java))).thenAnswer { it.arguments[0] as UsuarioEntity }

        val result = service.login("juan@test.com", "hashedpassword")
        assertNotNull(result)
        assertNotNull(result!!.token)
        assertTrue(result.token!!.isNotBlank())
    }

    @Test
    fun `login genera un token nuevo en cada autenticacion exitosa`() {
        val entity1 = entityBase()
        val entity2 = entityBase()
        `when`(usuarioRepository.findUserByPasswordAndEmail(anyString(), anyString()))
            .thenReturn(entity1).thenReturn(entity2)
        `when`(usuarioRepository.save(any(UsuarioEntity::class.java))).thenAnswer { it.arguments[0] as UsuarioEntity }

        val r1 = service.login("juan@test.com", "hashedpassword")
        val r2 = service.login("juan@test.com", "hashedpassword")
        assertNotEquals(r1!!.token, r2!!.token)
    }

    // -------------------------------------------------------------------------
    // invalidateToken
    // -------------------------------------------------------------------------

    @Test
    fun `invalidateToken no hace nada cuando userId es null`() {
        service.invalidateToken(null)
        verify(usuarioRepository, never()).updateTokenById(any(), any())
    }

    @Test
    fun `invalidateToken llama a updateTokenById con null`() {
        service.invalidateToken("00000000-0000-0000-0000-000000001234")
        verify(usuarioRepository).updateTokenById(java.util.UUID.fromString("00000000-0000-0000-0000-000000001234"), null)
    }

    // -------------------------------------------------------------------------
    // addNewUsuario — validación de duplicados
    // -------------------------------------------------------------------------

    @Test
    fun `addNewUsuario lanza excepcion cuando email ya existe`() {
        val entity = entityBase()
        `when`(usuarioRepository.findByEmail("juan@test.com")).thenReturn(entity)
        `when`(usuarioRepository.findByUsername(anyString())).thenReturn(null)
        `when`(usuarioRepository.findByCurp(anyString())).thenReturn(null)

        val usuario = Usuario(
            curp = "ABCD123456HDFXXX01", username = "otro", rol = Rol.ADOPTANTE,
            nombres = "Juan", apellidoPaterno = "P", apellidoMaterno = "L",
            email = "juan@test.com", codigoPostal = "06600", password = "hash"
        )
        assertThrows<IllegalArgumentException> { service.addNewUsuario(usuario) }
    }

    @Test
    fun `addNewUsuario guarda usuario cuando no hay duplicados`() {
        `when`(usuarioRepository.findByEmail(anyString())).thenReturn(null)
        `when`(usuarioRepository.findByUsername(anyString())).thenReturn(null)
        `when`(usuarioRepository.findByCurp(anyString())).thenReturn(null)
        `when`(usuarioRepository.save(any(UsuarioEntity::class.java))).thenReturn(entityBase())

        val usuario = Usuario(
            curp = "ABCD123456HDFXXX01", username = "testuser", rol = Rol.ADOPTANTE,
            nombres = "Juan", apellidoPaterno = "Pérez", apellidoMaterno = "López",
            email = "juan@test.com", codigoPostal = "06600", password = "hash"
        )
        val result = service.addNewUsuario(usuario)
        assertNotNull(result)
        verify(usuarioRepository).save(any(UsuarioEntity::class.java))
    }

    // -------------------------------------------------------------------------
    // updateUsuario — validacion de email duplicado y CP
    // -------------------------------------------------------------------------

    @Test
    fun `updateUsuario lanza excepcion cuando el email ya pertenece a otro usuario`() {
        val otroUsuario = entityBase().copy(
            id = java.util.UUID.fromString("00000000-0000-0000-0000-000000009999")
        )
        `when`(usuarioRepository.findById(java.util.UUID.fromString("00000000-0000-0000-0000-000000001234")))
            .thenReturn(java.util.Optional.of(entityBase()))
        `when`(usuarioRepository.findByEmail("otro@test.com")).thenReturn(otroUsuario)

        val request = com.kotliners.adoptaPerrito.dto.request.UpdateUsuarioRequest(
            nombres = "Juan", apellidoPaterno = "Perez", apellidoMaterno = "Lopez",
            email = "otro@test.com", codigoPostal = "06600"
        )
        assertThrows<IllegalArgumentException> {
            service.updateUsuario("00000000-0000-0000-0000-000000001234", request)
        }
    }

    @Test
    fun `updateUsuario permite actualizar al mismo email del propio usuario`() {
        val entity = entityBase()
        `when`(usuarioRepository.findById(java.util.UUID.fromString("00000000-0000-0000-0000-000000001234")))
            .thenReturn(java.util.Optional.of(entity))
        `when`(usuarioRepository.findByEmail("juan@test.com")).thenReturn(entity)
        `when`(usuarioRepository.save(any(UsuarioEntity::class.java))).thenReturn(entity)

        val request = com.kotliners.adoptaPerrito.dto.request.UpdateUsuarioRequest(
            nombres = "Juan Actualizado", apellidoPaterno = "Perez", apellidoMaterno = "Lopez",
            email = "juan@test.com", codigoPostal = "06600"
        )
        val result = service.updateUsuario("00000000-0000-0000-0000-000000001234", request)
        assertNotNull(result)
    }

    @Test
    fun `updateUsuario lanza excepcion cuando el codigo postal no tiene 5 digitos`() {
        val entity = entityBase()
        `when`(usuarioRepository.findById(java.util.UUID.fromString("00000000-0000-0000-0000-000000001234")))
            .thenReturn(java.util.Optional.of(entity))
        `when`(usuarioRepository.findByEmail(anyString())).thenReturn(null)

        val request = com.kotliners.adoptaPerrito.dto.request.UpdateUsuarioRequest(
            nombres = "Juan", apellidoPaterno = "Perez", apellidoMaterno = "Lopez",
            email = "juan@test.com", codigoPostal = "abc"
        )
        assertThrows<IllegalArgumentException> {
            service.updateUsuario("00000000-0000-0000-0000-000000001234", request)
        }
    }

    @Test
    fun `addNewUsuario lanza excepcion cuando el codigo postal no tiene 5 digitos`() {
        `when`(usuarioRepository.findByEmail(anyString())).thenReturn(null)
        `when`(usuarioRepository.findByUsername(anyString())).thenReturn(null)
        `when`(usuarioRepository.findByCurp(anyString())).thenReturn(null)

        val usuario = Usuario(
            curp = "ABCD123456HDFXXX01", username = "testuser", rol = Rol.ADOPTANTE,
            nombres = "Juan", apellidoPaterno = "Perez", apellidoMaterno = "Lopez",
            email = "juan@test.com", codigoPostal = "abc", password = "hash"
        )
        assertThrows<IllegalArgumentException> { service.addNewUsuario(usuario) }
    }
}
