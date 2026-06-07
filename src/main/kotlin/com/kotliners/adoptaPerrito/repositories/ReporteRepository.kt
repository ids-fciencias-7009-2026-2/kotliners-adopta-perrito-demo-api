package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.ReporteEntity
import com.kotliners.adoptaPerrito.entities.ReporteEstado
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ReporteRepository : JpaRepository<ReporteEntity, UUID> {

    fun findAllByEstadoOrderByFechaDesc(estado: ReporteEstado): List<ReporteEntity>

    fun findAllByAnimalId(animalId: UUID): List<ReporteEntity>

    fun existsByUsuarioIdAndAnimalId(usuarioId: UUID, animalId: UUID): Boolean

    fun existsByUsuarioIdAndAnimalIdAndEstado(usuarioId: UUID, animalId: UUID, estado: ReporteEstado): Boolean

    fun deleteByUsuarioIdAndAnimalId(usuarioId: UUID, animalId: UUID)
}
