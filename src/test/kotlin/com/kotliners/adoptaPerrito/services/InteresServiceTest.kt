package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.adapters.MailAdapter
import com.kotliners.adoptaPerrito.domain.Estatus
import com.kotliners.adoptaPerrito.domain.Rol
import com.kotliners.adoptaPerrito.domain.Sexo
import com.kotliners.adoptaPerrito.entities.AnimalEntity
import com.kotliners.adoptaPerrito.entities.AnimalInteresEntity
import com.kotliners.adoptaPerrito.entities.AnimalInteresId
import com.kotliners.adoptaPerrito.entities.UsuarioEntity
import com.kotliners.adoptaPerrito.repositories.AnimalInteresRepository
import com.kotliners.adoptaPerrito.repositories.AnimalRepository
import com.kotliners.adoptaPerrito.repositories.UsuarioRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

/**
 * Unit tests para InteresService.
 * Se mockean los repositorios para aislar la logica de negocio.
 */
class InteresServiceTest {

    private lateinit var service: InteresService
    private lateinit var interesRepository: AnimalInteresRepository
    private lateinit var animalRepository: AnimalRepository
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var mailAdapter: MailAdapter

    private val usuarioId = UUID.fromString("00000000-0000-0000-0000-000000001111")
    private val animalId  = UUID.fromString("00000000-0000-0000-0000-000000002222")

    private fun animalDisponible() = AnimalEntity(
        id = animalId,
        nombre = "Luna",
        especie = "Gato",
        raza = "Siames",
        fechaNacimiento = LocalDate.of(2022, 1, 1),
        sexo = Sexo.HEMBRA,
        descripcion = "Gata muy tranquila",
        estatus = Estatus.DISPONIBLE,
        usuarioId = UUID.fromString("00000000-0000-0000-0000-000000003333"),
        esterilizado = true
    )

    private fun animalAdoptado() = animalDisponible().copy(estatus = Estatus.ADOPTADO)

    private fun interesEntity() = AnimalInteresEntity(
        usuarioId = usuarioId,
        animalId = animalId,
        fecha = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        interesRepository = mock(AnimalInteresRepository::class.java)
        animalRepository  = mock(AnimalRepository::class.java)
        usuarioRepository = mock(UsuarioRepository::class.java)
        mailAdapter       = mock(MailAdapter::class.java)
        service = InteresService()
        service.interesRepository = interesRepository
        service.animalRepository  = animalRepository
        service.usuarioRepository = usuarioRepository
        service.mailAdapter       = mailAdapter
        // Por defecto los usuarios no se encuentran (el mail es opcional)
        `when`(usuarioRepository.findById(any())).thenReturn(java.util.Optional.empty())
    }

    // -------------------------------------------------------------------------
    // manifestarInteres
    // -------------------------------------------------------------------------

    @Test
    fun `manifestarInteres lanza excepcion cuando el usuario es CUIDADOR`() {
        assertThrows<IllegalArgumentException> {
            service.manifestarInteres(usuarioId.toString(), Rol.CUIDADOR, animalId.toString())
        }
    }

    @Test
    fun `manifestarInteres lanza excepcion cuando el animal no existe`() {
        `when`(animalRepository.findById(animalId)).thenReturn(Optional.empty())

        assertThrows<IllegalArgumentException> {
            service.manifestarInteres(usuarioId.toString(), Rol.ADOPTANTE, animalId.toString())
        }
    }

    @Test
    fun `manifestarInteres lanza excepcion cuando el animal esta adoptado`() {
        `when`(animalRepository.findById(animalId)).thenReturn(Optional.of(animalAdoptado()))

        assertThrows<IllegalArgumentException> {
            service.manifestarInteres(usuarioId.toString(), Rol.ADOPTANTE, animalId.toString())
        }
    }

    @Test
    fun `manifestarInteres lanza excepcion cuando el interes ya existe`() {
        `when`(animalRepository.findById(animalId)).thenReturn(Optional.of(animalDisponible()))
        `when`(interesRepository.existsByUsuarioIdAndAnimalId(usuarioId, animalId)).thenReturn(true)

        assertThrows<IllegalArgumentException> {
            service.manifestarInteres(usuarioId.toString(), Rol.ADOPTANTE, animalId.toString())
        }
    }

    @Test
    fun `manifestarInteres guarda y retorna el interes cuando es valido`() {
        val entity = interesEntity()
        `when`(animalRepository.findById(animalId)).thenReturn(Optional.of(animalDisponible()))
        `when`(interesRepository.existsByUsuarioIdAndAnimalId(usuarioId, animalId)).thenReturn(false)
        `when`(interesRepository.save(any(AnimalInteresEntity::class.java))).thenReturn(entity)

        val result = service.manifestarInteres(usuarioId.toString(), Rol.ADOPTANTE, animalId.toString())

        assertNotNull(result)
        assertEquals(usuarioId.toString(), result.usuarioId)
        assertEquals(animalId.toString(), result.animalId)
        verify(interesRepository).save(any(AnimalInteresEntity::class.java))
    }

    // -------------------------------------------------------------------------
    // eliminarInteres
    // -------------------------------------------------------------------------

    @Test
    fun `eliminarInteres lanza excepcion cuando el interes no existe`() {
        `when`(interesRepository.existsByUsuarioIdAndAnimalId(usuarioId, animalId)).thenReturn(false)

        assertThrows<IllegalArgumentException> {
            service.eliminarInteres(usuarioId.toString(), animalId.toString())
        }
    }

    @Test
    fun `eliminarInteres llama a deleteById cuando el interes existe`() {
        `when`(interesRepository.existsByUsuarioIdAndAnimalId(usuarioId, animalId)).thenReturn(true)

        service.eliminarInteres(usuarioId.toString(), animalId.toString())

        verify(interesRepository).deleteById(AnimalInteresId(usuarioId = usuarioId, animalId = animalId))
    }

    // -------------------------------------------------------------------------
    // listarIntereses
    // -------------------------------------------------------------------------

    @Test
    fun `listarIntereses retorna lista vacia cuando el usuario no tiene intereses`() {
        `when`(interesRepository.findByUsuarioId(usuarioId)).thenReturn(emptyList())

        val result = service.listarIntereses(usuarioId.toString())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `listarIntereses retorna los animales de interes del usuario`() {
        val entity = interesEntity()
        val animal = animalDisponible()
        `when`(interesRepository.findByUsuarioId(usuarioId)).thenReturn(listOf(entity))
        `when`(animalRepository.findById(animalId)).thenReturn(Optional.of(animal))

        val result = service.listarIntereses(usuarioId.toString())

        assertEquals(1, result.size)
        assertEquals("Luna", result[0].nombre)
        assertEquals(animalId.toString(), result[0].animalId)
    }

    @Test
    fun `listarIntereses omite animales que ya no existen en la base de datos`() {
        val entity = interesEntity()
        `when`(interesRepository.findByUsuarioId(usuarioId)).thenReturn(listOf(entity))
        `when`(animalRepository.findById(animalId)).thenReturn(Optional.empty())

        val result = service.listarIntereses(usuarioId.toString())

        assertTrue(result.isEmpty())
    }
}
