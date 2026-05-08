package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.domain.Rol

import com.kotliners.adoptaPerrito.dto.request.CreatePadecimientoRequest
import com.kotliners.adoptaPerrito.dto.response.PadecimientoResponse

import com.kotliners.adoptaPerrito.entities.PadecimientoEntity

import com.kotliners.adoptaPerrito.repositories.PadecimientoRepository

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired

import org.springframework.stereotype.Service

/**
 * Servicio para gestionar los padecimientos de los animales.
 */
@Service
class PadecimientoService {

    /* Logger para registrar eventos y errores */
    private val logger: Logger = LoggerFactory.getLogger(PadecimientoService::class.java)

    /* Repositorio para acceder a los datos de los padecimientos */
    @Autowired
    lateinit var padecimientoRepository: PadecimientoRepository

    /**
     * Lista todos los padecimientos disponibles en el sistema.
     * 
     * @return Lista de padecimientos ordenados alfabéticamente por nombre
     */
    fun listPadecimientos(): List<PadecimientoResponse> {
        return padecimientoRepository.findAll()
            .mapNotNull { entity ->
                val id = entity.id?.toString() ?: return@mapNotNull null
                PadecimientoResponse(id = id, nombre = entity.nombre)
            }
            .sortedBy { it.nombre.lowercase() }
    }

    /**
     * Crea un nuevo padecimiento en el sistema.
     *
     * @param request Datos para crear el padecimiento
     * @param requesterRole Rol del usuario que solicita la creación 
     * @return El padecimiento creado
     *
     * @throws IllegalStateException Si ocurre un error al generar el ID del padecimiento
     */
    fun createPadecimiento(request: CreatePadecimientoRequest, requesterRole : Rol): PadecimientoResponse {
        logger.info("Intento de creación de padecimiento con nombre: ${request.nombre} por usuario con rol: $requesterRole")
        if (requesterRole != Rol.CUIDADOR) {
            logger.warn("Usuario con rol $requesterRole no autorizado para crear padecimientos")
            throw IllegalStateException("Solo los usuarios con rol de cuidador pueden crear padecimientos")
        }
        val nombre = request.nombre.trim()
        val existing = padecimientoRepository.findByNombre(nombre)
        val saved = existing ?: padecimientoRepository.save(PadecimientoEntity(nombre = nombre))
        val id = saved.id?.toString() ?: throw IllegalStateException("No se pudo generar el ID del padecimiento")
        return PadecimientoResponse(id = id, nombre = saved.nombre)
    }
}