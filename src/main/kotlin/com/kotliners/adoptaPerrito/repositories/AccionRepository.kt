package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.AccionEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AccionRepository : JpaRepository<AccionEntity, UUID>
