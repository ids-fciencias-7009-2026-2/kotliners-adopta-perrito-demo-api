package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.entities.AccionEntity
import com.kotliners.adoptaPerrito.repositories.AccionRepository
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Servicio para registrar acciones de auditoría.
 * No almacena PII: solo el ID interno del usuario y la descripción del evento.
 */
@Service
class AccionService(private val accionRepository: AccionRepository) {

    fun registrar(usuarioId: UUID?, accion: String) {
        accionRepository.save(AccionEntity(usuarioId = usuarioId, accion = accion))
    }
}
