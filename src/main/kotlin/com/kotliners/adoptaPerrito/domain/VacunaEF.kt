package com.kotliners.adoptaPerrito.domain

import com.kotliners.adoptaPerrito.entities.VacunaEntity
import com.kotliners.adoptaPerrito.dto.request.CreateVacunaRequest

/**
 * Función de extensión para convertir un CreateVacunaRequest a un objeto de dominio Vacuna.
 */
fun CreateVacunaRequest.toVacuna(): Vacuna {
    return Vacuna(
        id = null, 
        nombre = this.nombre
    )
}

/**
 * Función de extensión para convertir un VacunaEntity a un objeto de dominio Vacuna.
 */
fun VacunaEntity.toVacuna(): Vacuna {
    return Vacuna(
        id = this.id?.toString(),  
        nombre = this.nombre
    )
}