package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.domain.Animal
import com.kotliners.adoptaPerrito.entities.AnimalEntity
import java.util.UUID

/**
 * Función de extensión que convierte un Animal de dominio a AnimalEntity para persistencia.
 */
fun Animal.toAnimalEntity(): AnimalEntity {
    return AnimalEntity(
        id = this.id?.let { UUID.fromString(it) },
        nombre = this.nombre,
        especie = this.especie,
        raza = this.raza,
        fechaNacimiento = this.fechaNacimiento,
        sexo = this.sexo,
        descripcion = this.descripcion,
        estatus = this.estatus,
        usuarioId = UUID.fromString(this.usuarioId),
        fechaRegistro = this.fechaRegistro,
        inapropiado = this.inapropiado,
        esterilizado = this.esterilizado,
        updatedAt = this.updatedAt
    )
}
