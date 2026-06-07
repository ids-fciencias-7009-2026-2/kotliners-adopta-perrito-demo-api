package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.adapters.MailAdapter
import com.kotliners.adoptaPerrito.domain.AnimalInteres
import com.kotliners.adoptaPerrito.domain.Estatus
import com.kotliners.adoptaPerrito.domain.Rol
import com.kotliners.adoptaPerrito.dto.response.AnimalInteresResponse
import com.kotliners.adoptaPerrito.dto.response.InteresRecibidoResponse
import com.kotliners.adoptaPerrito.dto.response.InteresResponse
import com.kotliners.adoptaPerrito.entities.AnimalInteresEntity
import com.kotliners.adoptaPerrito.entities.AnimalInteresId
import com.kotliners.adoptaPerrito.repositories.AnimalInteresRepository
import com.kotliners.adoptaPerrito.repositories.AnimalRepository
import com.kotliners.adoptaPerrito.repositories.UsuarioRepository

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

    @Autowired
    lateinit var fotoAnimalRepository: com.kotliners.adoptaPerrito.repositories.FotoAnimalRepository

    @Autowired
    lateinit var accionService: AccionService

    /**
     * Registra el interes de un usuario ADOPTANTE en un animal DISPONIBLE.
     * Valida que el usuario sea ADOPTANTE y que el animal exista y este disponible.
     *
     * @param usuarioId ID del usuario autenticado.
     * @param usuarioRol Rol del usuario autenticado.
     * @param animalId ID del animal.
     * @return InteresResponse con el interes registrado y advertencia si el correo fallo.
     * @throws IllegalArgumentException si el usuario es CUIDADOR, el animal no existe,
     *         ya fue adoptado, o el interes ya fue registrado.
     */
    @Transactional
    fun manifestarInteres(usuarioId: String, usuarioRol: Rol, animalId: String): InteresResponse {
        logger.info("Registrando interes: usuario=$usuarioId, animal=$animalId")
        val animalUuid = try { UUID.fromString(animalId) } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Animal no encontrado")
        }
        val usuarioUuid = try { UUID.fromString(usuarioId) } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Usuario no valido")
        }

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
        accionService.registrar(usuarioUuid, "MANIFESTAR_INTERES")
        logger.info("Interes registrado correctamente")

        // Enviar correo al cuidador del animal notificando el interes del adoptante
        val cuidador = usuarioRepository.findById(animal.usuarioId!!).orElse(null)
        val adoptante = usuarioRepository.findById(usuarioUuid).orElse(null)
        if (cuidador != null && adoptante != null) {
            val (asunto, cuerpo) = com.kotliners.adoptaPerrito.utils.NotificacionFactory.interesManifestado(
                cuidador.nombres, "${adoptante.nombres} ${adoptante.apellidoPaterno}", animal.nombre, adoptante.email
            )
            val resultado = mailAdapter.sendHtmlEmail(
                to = cuidador.email,
                subject = asunto,
                htmlBody = cuerpo,
                cc = adoptante.email
            )
            if (resultado.isFailure) {
                logger.error("No se pudo enviar el correo de notificacion: ${resultado.exceptionOrNull()?.message}")
                throw IllegalStateException("No se pudo notificar al cuidador por correo. Intenta de nuevo mas tarde.")
            }
        }

        return InteresResponse(
            usuarioId = saved.usuarioId.toString(),
            animalId = saved.animalId.toString(),
            fecha = saved.fecha,
            advertencia = null
        )
    }

    /**
     * Elimina el interés de un usuario en un animal.
     *
     * @param usuarioId ID del usuario autenticado
     * @param animalId ID del animal
     * @throws IllegalArgumentException si el interés no existe
     */
    @Transactional
    fun eliminarInteres(usuarioId: String, animalId: String) {
        logger.info("Eliminando interes: usuario=$usuarioId, animal=$animalId")
        val animalUuid = try { UUID.fromString(animalId) } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Animal no encontrado")
        }
        val usuarioUuid = try { UUID.fromString(usuarioId) } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Usuario no valido")
        }

        if (!interesRepository.existsByUsuarioIdAndAnimalId(usuarioUuid, animalUuid)) {
            logger.warn("No existe interes para eliminar: usuario=$usuarioId, animal=$animalId")
            throw IllegalArgumentException("No tienes interes registrado en este animal")
        }

        interesRepository.deleteById(AnimalInteresId(usuarioId = usuarioUuid, animalId = animalUuid))
        logger.info("Interes eliminado correctamente")

        // Notificar al cuidador que el adoptante ya no esta interesado
        val animal = animalRepository.findById(animalUuid).orElse(null)
        val cuidador = animal?.usuarioId?.let { usuarioRepository.findById(it).orElse(null) }
        val adoptante = usuarioRepository.findById(usuarioUuid).orElse(null)
        if (animal != null && cuidador != null && adoptante != null) {
            val (asunto, cuerpo) = com.kotliners.adoptaPerrito.utils.NotificacionFactory.interesRetirado(
                cuidador.nombres, "${adoptante.nombres} ${adoptante.apellidoPaterno}", animal.nombre
            )
            val resultado = mailAdapter.sendHtmlEmail(
                to = cuidador.email,
                subject = asunto,
                htmlBody = cuerpo,
                cc = adoptante.email
            )
            if (resultado.isFailure) {
                logger.error("No se pudo enviar correo de retiro de interes: ${resultado.exceptionOrNull()?.message}")
                throw IllegalStateException("No se pudo notificar al cuidador por correo. El retiro de interes no fue procesado.")
            }
        }
    }

    /**
     * Obtiene la lista paginada de animales en los que un usuario ha manifestado interes.
     *
     * @param usuarioId ID del usuario autenticado.
     * @param limit Numero maximo de resultados a devolver (default 20, max 100).
     * @param offset Numero de resultados a saltar para paginacion (default 0).
     * @return Lista paginada de AnimalInteresResponse.
     */
    fun listarIntereses(usuarioId: String, limit: Int = 20, offset: Int = 0): List<AnimalInteresResponse> {
        logger.info("Listando intereses del usuario: $usuarioId (limit=$limit, offset=$offset)")
        val safeLimit = limit.coerceIn(1, 100)
        val safeOffset = offset.coerceAtLeast(0)
        val usuarioUuid = UUID.fromString(usuarioId)
        val intereses = interesRepository.findByUsuarioId(usuarioUuid)

        return intereses
            .drop(safeOffset)
            .take(safeLimit)
            .mapNotNull { interes ->
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
                    fechaInteres = interes.fecha,
                    fotoPortada = fotoAnimalRepository.findByAnimalId(animal.id!!).firstOrNull()?.foto,
                    fechaRegistro = animal.fechaRegistro
                )
            }
    }

    /**
     * Obtiene la lista paginada de adoptantes interesados en los animales de un cuidador.
     *
     * @param cuidadorId ID del cuidador autenticado.
     * @param limit Numero maximo de resultados (default 20, max 100).
     * @param offset Numero de resultados a saltar (default 0).
     * @return Lista de InteresRecibidoResponse con datos del adoptante y el animal.
     */
    fun listarInteresesRecibidos(cuidadorId: String, limit: Int = 20, offset: Int = 0): List<InteresRecibidoResponse> {
        logger.info("Listando intereses recibidos por cuidador: $cuidadorId")
        val safeLimit = limit.coerceIn(1, 100)
        val safeOffset = offset.coerceAtLeast(0)
        val cuidadorUuid = UUID.fromString(cuidadorId)

        // Obtener todos los animales del cuidador
        val animalesDelCuidador = animalRepository.findAllByUsuarioId(cuidadorUuid)
        val animalIds = animalesDelCuidador.mapNotNull { it.id }.toSet()

        // Obtener todos los intereses en esos animales
        val intereses = animalIds.flatMap { animalId ->
            interesRepository.findByAnimalId(animalId)
        }

        return intereses
            .drop(safeOffset)
            .take(safeLimit)
            .mapNotNull { interes ->
                val animal = animalRepository.findById(interes.animalId).orElse(null) ?: return@mapNotNull null
                val adoptante = usuarioRepository.findById(interes.usuarioId).orElse(null) ?: return@mapNotNull null
                InteresRecibidoResponse(
                    animalId = animal.id.toString(),
                    nombreAnimal = animal.nombre,
                    adoptanteId = adoptante.id.toString(),
                    nombreAdoptante = "${adoptante.nombres} ${adoptante.apellidoPaterno}",
                    emailAdoptante = adoptante.email,
                    fotoAdoptante = adoptante.fotoPerfil,
                    fechaInteres = interes.fecha
                )
            }
    }
}
