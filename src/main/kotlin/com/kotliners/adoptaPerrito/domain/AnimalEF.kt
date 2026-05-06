package com.kotliners.adoptaPerrito.domain

import com.kotliners.adoptaPerrito.entities.AnimalEntity

/**
* TODO: Función de extensión que transforma un CreateAnimalRequest en un Animal de dominio.
 */

/**
 * Función de extensión que convierte un AnimalEntity a un Animal de dominio.
 */
fun AnimalEntity.toAnimal(): Animal {
	return Animal(
		id = this.id?.toString(),
		nombre = this.nombre,
		especie = this.especie,
		raza = this.raza,
		fechaNacimiento = this.fechaNacimiento,
		sexo = this.sexo,
		descripcion = this.descripcion,
		estatus = this.estatus,
		usuarioId = this.usuarioId.toString(),
		fechaRegistro = this.fechaRegistro,
		inapropiado = this.inapropiado,
		esterilizado = this.esterilizado,
		updatedAt = this.updatedAt
	)
}
