package com.kotliners.adoptaPerrito.domain

import com.kotliners.adoptaPerrito.entities.AnimalEntity

/**
 * Convierte un AnimalEntity a un Animal de dominio.
 */
fun AnimalEntity.toAnimal(): Animal {
	return Animal(
		id = this.id?.toString(),
		nombre = this.nombre,
		especie = this.especie,
		raza = this.raza,
		razaId = this.razaId?.toString(),
		fechaNacimiento = this.fechaNacimiento,
		sexo = this.sexo,
		descripcion = this.descripcion,
		estatus = this.estatus,
		usuarioId = this.usuarioId?.toString() ?: "",
		fechaRegistro = this.fechaRegistro,
		esterilizado = this.esterilizado,
		updatedAt = this.updatedAt
	)
}
