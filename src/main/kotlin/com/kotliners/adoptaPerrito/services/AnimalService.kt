package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.domain.Animal
import com.kotliners.adoptaPerrito.domain.toAnimal
import com.kotliners.adoptaPerrito.domain.Estatus
import com.kotliners.adoptaPerrito.domain.Rol

import com.kotliners.adoptaPerrito.dto.request.UpdateAnimalRequest
import com.kotliners.adoptaPerrito.dto.response.AnimalDetalleResponse
import com.kotliners.adoptaPerrito.dto.response.toAnimalDetalleResponse

import com.kotliners.adoptaPerrito.entities.AnimalEntity

import com.kotliners.adoptaPerrito.repositories.AnimalRepository
import com.kotliners.adoptaPerrito.repositories.FotoAnimalRepository
import com.kotliners.adoptaPerrito.repositories.VacunaRepository
import com.kotliners.adoptaPerrito.repositories.PadecimientoRepository
import com.kotliners.adoptaPerrito.repositories.AnimalVacunaRepository
import com.kotliners.adoptaPerrito.repositories.AnimalPadecimientoRepository
import com.kotliners.adoptaPerrito.repositories.toAnimalEntity

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.UUID
import java.time.LocalDateTime

/**
 * Servicio de dominio para gestionar operaciones relacionadas con animales.
 */
@Service
class AnimalService {

    /* Logger para registrar información relevante durante la ejecución de las operaciones del servicio */
    private val logger: Logger = LoggerFactory.getLogger(AnimalService::class.java)

    /** Repositorio para acceder a los datos de los animales */
    @Autowired
    lateinit var animalRepository: AnimalRepository

    @Autowired
    lateinit var fotoAnimalRepository: FotoAnimalRepository

    @Autowired
    lateinit var vacunaRepository: VacunaRepository

    @Autowired
    lateinit var padecimientoRepository: PadecimientoRepository

    @Autowired
    lateinit var animalVacunaRepository: AnimalVacunaRepository

    @Autowired
    lateinit var animalPadecimientoRepository: AnimalPadecimientoRepository

    /** 
     * Crea un nuevo animal y lo persiste en la base de datos
     * 
     * @param animal Animal de dominio a guardar
     * @return El animal guardado con su ID asignado
     */
    fun addNewAnimal(animal: Animal, requesterRole: Rol): Animal {
        logger.info("Creando nuevo animal: ${animal.nombre}")
        if (requesterRole != Rol.CUIDADOR) {
            logger.warn("Intento de crear animal por usuario sin rol CUIDADOR: $requesterRole")
            throw IllegalArgumentException("Solo usuarios con rol CUIDADOR pueden crear animales")
        }
        val entity = animal.toAnimalEntity()
        val saved = animalRepository.save(entity)
        logger.info("Animal creado con ID: ${saved.id}")
        return saved.toAnimal()
    }

    /** 
     * Lista todos los animales registrados en la base de datos y los devuelve como objetos de dominio.
     * 
     * @param requesterRole Rol del usuario que hace la solicitud 
     * @return Lista de animales de dominio
     * 
     * @throws IllegalArgumentException si el requester no tiene el rol ADOPTANTE
     */
    fun searchAllAnimals(requesterRole: Rol): List<Animal> {
        logger.info("Listando todos los animales para rol: $requesterRole")
        if (requesterRole != Rol.ADOPTANTE) {
            logger.warn("Intento de listar animales por usuario sin rol ADOPTANTE: $requesterRole")
            throw IllegalArgumentException("Solo usuarios con rol ADOPTANTE pueden listar todos los animales")
        }
        return animalRepository.findAll().map { it.toAnimal() }
    }

    /** 
     * Busca un animal por su ID y lo devuelve como un objeto de dominio. 
     * 
     * @param id Identificador del animal a buscar
     * @return El animal encontrado o null si no existe
     */
    fun getAnimalById(id: String): Animal? {
        logger.info("Buscando animal por ID: $id")
        val uuid = try { UUID.fromString(id) } catch (e: IllegalArgumentException) {
            logger.warn("ID de animal no es un UUID valido: $id")
            return null
        }
        val entity = animalRepository.findById(uuid).orElse(null)
        if (entity == null) {
            logger.warn("No se encontro el animal con ID: $id")
            return null
        }
        return entity.toAnimal()
    }

    /**
     * Devuelve un animal respetando permisos del requester.
     * - Si el requester es CUIDADOR solo puede ver animales propios
     * - Si el requester es ADOPTANTE puede ver cualquier animal
     * 
     * @param id ID del animal a obtener
     * @param requesterId ID del usuario que hace la solicitud (para validar permisos)
     * @param requesterRole Rol del usuario que hace la solicitud (para validar permisos)
     * @return El animal si se encuentra y el requester tiene permisos, o null si no se encuentra o no tiene permisos
     */
    fun getAnimalForRequester(id: String, requesterId: String, requesterRole: Rol): Animal? {
        val animal = getAnimalById(id) ?: return null
        if (requesterRole == Rol.CUIDADOR) {
            if (animal.usuarioId != requesterId) {
                logger.warn("Cuidador $requesterId intentó acceder a animal que no le pertenece: $id")
                throw IllegalArgumentException("No autorizado para ver este animal")
            }
        }
        return animal
    }

    /** 
     * Actualiza la información de un animal existente.
     * 
     * @param id Identificador del animal a actualizar
     * @param updates Objeto con los datos actualizados del animal
     * @param requesterId ID del usuario que hace la solicitud 
     * @param requesterRole Rol del usuario que hace la solicitud 
     * @return El animal actualizado o null si no se encontró el animal
     */
    fun updateAnimal(id: String, updates: UpdateAnimalRequest, requesterId: String, requesterRole: Rol): Animal? {
        logger.info("Actualizando animal por ID: $id")
        val uuid = try { UUID.fromString(id) } catch (e: IllegalArgumentException) {
            logger.warn("ID de animal no es un UUID valido: $id")
            return null
        }
        val entity = animalRepository.findById(uuid).orElse(null)
        if (entity == null) {
            logger.warn("No se encontró el animal con ID: $id")
            return null
        }
        if (requesterRole != Rol.CUIDADOR) {
            logger.warn("Usuario $requesterId sin rol CUIDADOR intentó actualizar animal: $id")
            throw IllegalArgumentException("Solo cuidadores pueden actualizar animales")
        }
        if (entity.usuarioId.toString() != requesterId) {
            logger.warn("Usuario $requesterId intentó actualizar animal que no le pertenece: $id")
            throw IllegalArgumentException("No autorizado para actualizar este animal")
        }
        entity.nombre = updates.nombre
        entity.especie = updates.especie
        entity.raza = updates.raza
        entity.fechaNacimiento = updates.fechaNacimiento
        entity.sexo = updates.sexo
        entity.descripcion = updates.descripcion
        entity.estatus = updates.estatus
        entity.inapropiado = updates.inapropiado
        entity.esterilizado = updates.esterilizado
        entity.updatedAt = LocalDateTime.now()

        val savedEntity = animalRepository.save(entity)
        logger.info("Animal actualizado con éxito: $id")
        return savedEntity.toAnimal()
    }

    /** 
     * Elimina un animal por su ID. 
     * 
     * @param id Identificador del animal a eliminar
     * @param requesterId ID del usuario que hace la solicitud
     * @param requesterRole Rol del usuario que hace la solicitud
     * @return true si el animal fue eliminado, false si no se encontró el animal
     */
    fun deleteAnimal(id: String, requesterId: String, requesterRole: Rol): Boolean {
        logger.info("Eliminando animal por ID: $id")
        val uuid = try { UUID.fromString(id) } catch (e: IllegalArgumentException) {
            logger.warn("ID de animal no es un UUID valido: $id")
            return false
        }
        val entity = animalRepository.findById(uuid).orElse(null) ?: run {
            logger.warn("No existe el animal con ID: $id")
            return false
        }
        if (requesterRole != Rol.CUIDADOR) {
            logger.warn("Usuario $requesterId sin rol CUIDADOR intentó eliminar animal: $id")
            throw IllegalArgumentException("Solo cuidadores pueden eliminar animales")
        }
        if (entity.usuarioId.toString() != requesterId) {
            logger.warn("Usuario $requesterId intentó eliminar animal que no le pertenece: $id")
            throw IllegalArgumentException("No autorizado para eliminar este animal")
        }
        animalRepository.deleteById(uuid)
        return true
    }

    /**
     * Obtiene el detalle completo de un animal incluyendo fotos, vacunas y padecimientos.
     *
     * @param id ID del animal como string.
     * @param animal Objeto Animal ya cargado (evita doble consulta).
     * @return AnimalDetalleResponse con toda la informacion del animal.
     */
    fun getAnimalDetalle(id: String, animal: Animal): AnimalDetalleResponse {
        val uuid = UUID.fromString(id)

        val fotos = fotoAnimalRepository.findByAnimalId(uuid).map { it.foto }

        val vacunaIds = animalVacunaRepository.findByAnimalId(uuid).map { it.vacunaId }
        val vacunas = vacunaIds.mapNotNull { vacunaRepository.findById(it).orElse(null)?.nombre }

        val padecimientoIds = animalPadecimientoRepository.findByAnimalId(uuid).map { it.padecimientoId }
        val padecimientos = padecimientoIds.mapNotNull { padecimientoRepository.findById(it).orElse(null)?.nombre }

        return animal.toAnimalDetalleResponse(fotos, vacunas, padecimientos)
    }

    /**
     * Lista todos los animales registrados por un cuidador especifico.
     * @param usuarioId ID del cuidador.
     * @param requesterRole Rol del usuario que hace la solicitud 
     * @return Lista de animales del cuidador.
     */
    fun listAnimalsByOwner(usuarioId: String, requesterRole: Rol): List<Animal> {
        logger.info("Listando animales del cuidador: $usuarioId")
        if (requesterRole != Rol.CUIDADOR) {
            logger.warn("Intento de listar animales por usuario sin rol CUIDADOR: $requesterRole")
            throw IllegalArgumentException("Solo cuidadores pueden listar sus animales")
        }
        val uuid = try { UUID.fromString(usuarioId) } catch (e: IllegalArgumentException) {
            logger.warn("ID de usuario no es un UUID valido: $usuarioId")
            return emptyList()
        }
        return animalRepository.findAllByUsuarioId(uuid).map { it.toAnimal() }
    }

}
