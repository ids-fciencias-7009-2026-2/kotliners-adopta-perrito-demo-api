package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.domain.Rol

import com.kotliners.adoptaPerrito.dto.request.CreateVacunaRequest
import com.kotliners.adoptaPerrito.dto.response.VacunaResponse

import com.kotliners.adoptaPerrito.entities.VacunaEntity

import com.kotliners.adoptaPerrito.repositories.VacunaRepository

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired

import org.springframework.stereotype.Service

/**
 * Servicio para gestionar las vacunas de los animales.
 */
@Service
class VacunaService {

    /* Logger para registrar eventos y errores */
    private val logger: Logger = LoggerFactory.getLogger(VacunaService::class.java)

    /* Repositorio para acceder a los datos de las vacunas */
    @Autowired
    lateinit var vacunaRepository: VacunaRepository

    /**
     * Lista todas las vacunas disponibles en el sistema.
     * 
     * @return Lista de vacunas ordenados alfabéticamente por nombre
     */
    fun listVacunas(): List<VacunaResponse> {
        return vacunaRepository.findAll()
            .mapNotNull { entity ->
                val id = entity.id?.toString() ?: return@mapNotNull null
                VacunaResponse(id = id, nombre = entity.nombre)
            }
            .sortedBy { it.nombre.lowercase() }
    }

    /**
     * Crea una nueva vacuna en el sistema.
     *
     * @param request Datos para crear la vacuna
     * @param requesterRole Rol del usuario que solicita la creación
     * @return La vacuna creada
     * 
     * @throws IllegalStateException Si el usuario no tiene permisos para crear vacunas o si ocurre un error al generar el ID de la vacuna
     */
    fun createVacuna(request: CreateVacunaRequest, requesterRole : Rol): VacunaResponse {
        logger.info("Intento de creación de vacuna con nombre: ${request.nombre} por usuario con rol: $requesterRole")
        if (requesterRole != Rol.CUIDADOR) {
            logger.warn("Usuario con rol $requesterRole no autorizado para crear vacunas")
            throw IllegalStateException("Solo los usuarios con rol de cuidador pueden crear vacunas")
        }
        val nombre = request.nombre.trim()
        val existing = vacunaRepository.findByNombre(nombre)
        val saved = existing ?: vacunaRepository.save(VacunaEntity(nombre = nombre))
        val id = saved.id?.toString() ?: throw IllegalStateException("No se pudo generar el ID de la vacuna")
        return VacunaResponse(id = id, nombre = saved.nombre)
    }
}