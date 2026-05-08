package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.adapters.MailAdapter
import com.kotliners.adoptaPerrito.domain.AnimalInteres
import com.kotliners.adoptaPerrito.domain.Estatus
import com.kotliners.adoptaPerrito.domain.Rol
import com.kotliners.adoptaPerrito.dto.response.AnimalInteresResponse
import com.kotliners.adoptaPerrito.entities.AnimalInteresEntity
import com.kotliners.adoptaPerrito.entities.AnimalInteresId
import com.kotliners.adoptaPerrito.repositories.AnimalInteresRepository
import com.kotliners.adoptaPerrito.repositories.AnimalRepository
import com.kotliners.adoptaPerrito.repositories.UsuarioRepository

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.UUID

/**
 * Servicio de negocio para gestionar el interés de usuarios en animales.
 *
 * Maneja las operaciones de:
 * - Manifestar interés en un animal
 * - Eliminar interés en un animal
 * - Listar los animales de interés de un usuario
 */
@Service
class InteresService {

    private val logger: Logger = LoggerFactory.getLogger(InteresService::class.java)

    @Autowired
    lateinit var interesRepository: AnimalInteresRepository

    @Autowired
    lateinit var animalRepository: AnimalRepository

    @Autowired
    lateinit var usuarioRepository: UsuarioRepository

    @Autowired
    lateinit var mailAdapter: MailAdapter

    /**
     * Registra el interes de un usuario ADOPTANTE en un animal DISPONIBLE.
     * Valida que el usuario sea ADOPTANTE y que el animal exista y este disponible.
     *
     * @param usuarioId ID del usuario autenticado.
     * @param usuarioRol Rol del usuario autenticado.
     * @param animalId ID del animal.
     * @return AnimalInteres creado.
     * @throws IllegalArgumentException si el usuario es CUIDADOR, el animal no existe,
     *         ya fue adoptado, o el interes ya fue registrado.
     */
    fun manifestarInteres(usuarioId: String, usuarioRol: Rol, animalId: String): AnimalInteres {
        logger.info("Registrando interes: usuario=$usuarioId, animal=$animalId")
        val animalUuid = UUID.fromString(animalId)
        val usuarioUuid = UUID.fromString(usuarioId)

        // Solo los adoptantes pueden manifestar interes
        if (usuarioRol != Rol.ADOPTANTE) {
            logger.warn("Usuario CUIDADOR intento manifestar interes: $usuarioId")
            throw IllegalArgumentException("Solo los adoptantes pueden manifestar interes en animales.")
        }

        val animal = animalRepository.findById(animalUuid).orElse(null)
            ?: run {
                logger.warn("Animal no encontrado: $animalId")
                throw IllegalArgumentException("Animal no encontrado")
            }

        // No se puede dar like a un animal ya adoptado
        if (animal.estatus == Estatus.ADOPTADO) {
            logger.warn("Intento de interes en animal adoptado: $animalId")
            throw IllegalArgumentException("Este animal ya fue adoptado y no esta disponible.")
        }

        if (interesRepository.existsByUsuarioIdAndAnimalId(usuarioUuid, animalUuid)) {
            logger.warn("El usuario $usuarioId ya tiene interes en el animal $animalId")
            throw IllegalArgumentException("Ya manifestaste interes en este animal")
        }

        val entity = AnimalInteresEntity(usuarioId = usuarioUuid, animalId = animalUuid)
        val saved = interesRepository.save(entity)
        logger.info("Interes registrado correctamente")

        // Enviar correo al cuidador del animal notificando el interes del adoptante
        val cuidador = usuarioRepository.findById(animal.usuarioId!!).orElse(null)
        val adoptante = usuarioRepository.findById(usuarioUuid).orElse(null)
        if (cuidador != null && adoptante != null) {
            val asunto = "Nuevo interes en ${animal.nombre}"
            val cuerpo = """
                <html><body>
                <p>Hola <strong>${cuidador.nombres}</strong>,</p>
                <p><strong>${adoptante.nombres} ${adoptante.apellidoPaterno}</strong> ha mostrado interes en tu animal <strong>${animal.nombre}</strong>.</p>
                <p>Su correo de contacto es: <a href="mailto:${adoptante.email}">${adoptante.email}</a></p>
                <p>Por favor contacta al adoptante para continuar el proceso de adopcion.</p>
                <br>
                <p>Saludos,<br>Colitas Felices</p>
                </body></html>
            """.trimIndent()
            mailAdapter.sendHtmlEmail(
                to = cuidador.email,
                subject = asunto,
                htmlBody = cuerpo,
                cc = adoptante.email
            )
        }

        return AnimalInteres(
            usuarioId = saved.usuarioId.toString(),
            animalId = saved.animalId.toString(),
            fecha = saved.fecha
        )
    }

    /**
     * Elimina el interés de un usuario en un animal.
     *
     * @param usuarioId ID del usuario autenticado
     * @param animalId ID del animal
     * @throws IllegalArgumentException si el interés no existe
     */
    fun eliminarInteres(usuarioId: String, animalId: String) {
        logger.info("Eliminando interés: usuario=$usuarioId, animal=$animalId")
        val animalUuid = UUID.fromString(animalId)
        val usuarioUuid = UUID.fromString(usuarioId)

        if (!interesRepository.existsByUsuarioIdAndAnimalId(usuarioUuid, animalUuid)) {
            logger.warn("No existe interés para eliminar: usuario=$usuarioId, animal=$animalId")
            throw IllegalArgumentException("No tienes interés registrado en este animal")
        }

        interesRepository.deleteById(AnimalInteresId(usuarioId = usuarioUuid, animalId = animalUuid))
        logger.info("Interés eliminado correctamente")
    }

    /**
     * Obtiene la lista de animales en los que un usuario ha manifestado interés.
     *
     * @param usuarioId ID del usuario autenticado
     * @return Lista de AnimalInteresResponse con los datos del animal y la fecha de interés
     */
    fun listarIntereses(usuarioId: String): List<AnimalInteresResponse> {
        logger.info("Listando intereses del usuario: $usuarioId")
        val usuarioUuid = UUID.fromString(usuarioId)
        val intereses = interesRepository.findByUsuarioId(usuarioUuid)

        return intereses.mapNotNull { interes ->
            val animal = animalRepository.findById(interes.animalId).orElse(null) ?: return@mapNotNull null
            AnimalInteresResponse(
                animalId = animal.id.toString(),
                nombre = animal.nombre,
                especie = animal.especie,
                raza = animal.raza,
                fechaNacimiento = animal.fechaNacimiento,
                sexo = animal.sexo.name,
                descripcion = animal.descripcion,
                estatus = animal.estatus.name,
                esterilizado = animal.esterilizado,
                fechaInteres = interes.fecha
            )
        }
    }
}
