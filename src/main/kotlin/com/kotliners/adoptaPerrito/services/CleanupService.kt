package com.kotliners.adoptaPerrito.services

import com.kotliners.adoptaPerrito.repositories.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * Tarea programada que elimina cuentas no verificadas después de 24 horas.
 * Evita que alguien reserve un correo ajeno indefinidamente.
 */
@Component
class CleanupService(private val usuarioRepository: UsuarioRepository) {

    private val logger = LoggerFactory.getLogger(CleanupService::class.java)

    @Scheduled(fixedRate = 3600000) // Cada hora
    fun eliminarRegistrosNoVerificados() {
        val limite = LocalDateTime.now().minusHours(24)
        val eliminados = usuarioRepository.deleteUnverifiedBefore(limite)
        if (eliminados > 0) {
            logger.info("Limpieza: $eliminados cuentas no verificadas eliminadas (registro > 24h)")
        }
    }

    @Scheduled(fixedRate = 3600000)
    fun limpiarCambiosDeCorreoExpirados() {
        val limite = LocalDateTime.now().minusHours(24)
        val limpiados = usuarioRepository.clearExpiredEmailPendiente(limite)
        if (limpiados > 0) {
            logger.info("Limpieza: $limpiados cambios de correo expirados descartados")
        }
    }
}
