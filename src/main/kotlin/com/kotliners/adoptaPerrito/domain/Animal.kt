package com.kotliners.adoptaPerrito.domain

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Modelo de dominio que representa a un animal disponible para adopción en el sistema.
 */
data class Animal(

	/** Identificador único del animal */
	val id: String? = null,

	/** Nombre del animal */
	var nombre: String,

	/** Especie del animal */
	var especie: String,

	/** Raza del animal */
	var raza: String? = null,

	/** Fecha de nacimiento del animal */
	var fechaNacimiento: LocalDate,

	/** Sexo del animal */
	var sexo: Sexo,

	/** Descripción del animal */
	var descripcion: String,

	/** Estatus del animal */
	var estatus: Estatus,

	/** Identificador del usuario dueño del animal */
	var usuarioId: String,

	/** Fecha de registro del animal */
	var fechaRegistro: LocalDateTime = LocalDateTime.now(),

	/** Indica si el animal fue marcado como inapropiado */
	var inapropiado: Boolean = false,

	/** Indica si el animal está esterilizado */
	var esterilizado: Boolean = false,

	/** Fecha de última actualización */
	var updatedAt: LocalDateTime = LocalDateTime.now()
)