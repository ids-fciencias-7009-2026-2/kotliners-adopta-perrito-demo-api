package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.adapters.MailAdapter
import com.kotliners.adoptaPerrito.entities.ReporteEntity
import com.kotliners.adoptaPerrito.entities.ReporteEstado
import com.kotliners.adoptaPerrito.repositories.AnimalRepository
import com.kotliners.adoptaPerrito.repositories.ReporteRepository
import com.kotliners.adoptaPerrito.repositories.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class ReporteService(
    private val reporteRepository: ReporteRepository,
    private val animalRepository: AnimalRepository,
    private val usuarioRepository: UsuarioRepository,
    private val mailAdapter: MailAdapter
) {

    private val logger = LoggerFactory.getLogger(ReporteService::class.java)

    /**
     * Verifica si un usuario ya reportó un animal (reporte pendiente).
     */
    fun usuarioYaReporto(usuarioId: UUID, animalId: UUID): Boolean {
        return reporteRepository.existsByUsuarioIdAndAnimalIdAndEstado(usuarioId, animalId, ReporteEstado.PENDIENTE)
    }

    /**
     * Retira el reporte de un usuario sobre un animal.
     */
    @Transactional
    fun retirarReporte(usuarioId: UUID, animalId: UUID) {
        if (!reporteRepository.existsByUsuarioIdAndAnimalId(usuarioId, animalId)) {
            throw IllegalArgumentException("No tienes un reporte activo para este animal")
        }
        reporteRepository.deleteByUsuarioIdAndAnimalId(usuarioId, animalId)
        logger.info("Reporte retirado por usuario $usuarioId para animal $animalId")
    }

    /**
     * Crea un nuevo reporte. Valida que el animal exista y que no se duplique.
     */
    fun crearReporte(usuarioId: UUID, animalId: UUID, motivo: String): ReporteEntity {
        if (motivo.isBlank()) throw IllegalArgumentException("El motivo es obligatorio")

        val animalUuid = animalId
        animalRepository.findById(animalUuid).orElseThrow {
            IllegalArgumentException("El animal no existe")
        }

        if (reporteRepository.existsByUsuarioIdAndAnimalIdAndEstado(usuarioId, animalUuid, ReporteEstado.PENDIENTE)) {
            throw IllegalArgumentException("Ya reportaste esta publicacion")
        }

        val reporte = ReporteEntity(
            usuarioId = usuarioId,
            animalId = animalUuid,
            motivo = motivo
        )
        logger.info("Reporte creado por usuario $usuarioId para animal $animalId")
        return reporteRepository.save(reporte)
    }

    /**
     * Lista todos los reportes pendientes de revisión.
     */
    fun listarPendientes(): List<ReporteEntity> {
        return reporteRepository.findAllByEstadoOrderByFechaDesc(ReporteEstado.PENDIENTE)
    }

    fun getNombreAnimal(animalId: UUID): String {
        return animalRepository.findById(animalId).orElse(null)?.nombre ?: "Eliminado"
    }

    /**
     * Resuelve un reporte: elimina la publicación y envía correo al cuidador.
     */
    @Transactional
    fun resolver(reporteId: UUID): ReporteEntity {
        val reporte = reporteRepository.findById(reporteId).orElseThrow {
            IllegalArgumentException("Reporte no encontrado")
        }
        if (reporte.estado != ReporteEstado.PENDIENTE) {
            throw IllegalArgumentException("Este reporte ya fue procesado")
        }

        // 1. Marcar TODOS los reportes del animal como RESUELTOS primero
        val todosReportes = reporteRepository.findAllByAnimalId(reporte.animalId)
            .filter { it.estado == ReporteEstado.PENDIENTE }
        val motivos = todosReportes.map { it.motivo }.distinct()

        todosReportes.forEach {
            it.estado = ReporteEstado.RESUELTO
            it.fechaResolucion = LocalDateTime.now()
        }
        reporteRepository.saveAll(todosReportes)

        // 2. Eliminar reportes de la BD antes de borrar el animal (evita conflicto con CASCADE)
        reporteRepository.deleteAll(todosReportes)

        // 3. Ahora eliminar el animal
        val animal = animalRepository.findById(reporte.animalId).orElse(null)
        if (animal != null) {
            val cuidador = usuarioRepository.findById(animal.usuarioId!!).orElse(null)
            if (cuidador != null) {
                val motivosHtml = motivos.joinToString("") { "<li>$it</li>" }
                enviarCorreoEliminacion(cuidador.email, animal.nombre, "<ul>$motivosHtml</ul>")
            }
            animalRepository.deleteById(animal.id!!)
            logger.info("Animal ${animal.id} eliminado por resolucion de reporte $reporteId")
        }

        // Devolver una copia del reporte resuelto (ya no existe en BD)
        reporte.estado = ReporteEstado.RESUELTO
        reporte.fechaResolucion = LocalDateTime.now()
        return reporte
    }

    /**
     * Desestima un reporte: lo marca como DESESTIMADO sin eliminar la publicación.
     */
    fun desestimar(reporteId: UUID): ReporteEntity {
        val reporte = reporteRepository.findById(reporteId).orElseThrow {
            IllegalArgumentException("Reporte no encontrado")
        }
        if (reporte.estado != ReporteEstado.PENDIENTE) {
            throw IllegalArgumentException("Este reporte ya fue procesado")
        }

        reporte.estado = ReporteEstado.DESESTIMADO
        reporte.fechaResolucion = LocalDateTime.now()
        logger.info("Reporte $reporteId desestimado")
        return reporteRepository.save(reporte)
    }

    private fun enviarCorreoEliminacion(email: String, nombreAnimal: String, motivo: String) {
        val (subject, body) = com.kotliners.adoptaPerrito.utils.NotificacionFactory.publicacionEliminada(
            nombre = email.substringBefore("@"),
            animalNombre = nombreAnimal,
            motivo = motivo
        )
        mailAdapter.sendHtmlEmail(email, subject, body)
    }
}
