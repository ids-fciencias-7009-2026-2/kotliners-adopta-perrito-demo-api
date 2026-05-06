package com.kotliners.adoptaPerrito.entities

import com.kotliners.adoptaPerrito.domain.Estatus
import com.kotliners.adoptaPerrito.domain.Sexo

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Entidad JPA que representa la tabla "animal" en la base de datos.
 */
@Entity
@Table(name = "animal")
data class AnimalEntity(

	/** Identificador único del animal (UUID generado automáticamente) */
	@Id
	@UuidGenerator
	@Column(name = "animal_id", updatable = false, nullable = false)
	val id: UUID? = null,

	/** Nombre del animal */
	@Column(name = "nombre", nullable = false, length = 50)
	var nombre: String = "",

	/** Especie del animal */
	@Column(name = "especie", nullable = false, length = 50)
	var especie: String = "",

	/** Raza del animal */
	@Column(name = "raza", length = 50)
	var raza: String? = null,

	/** Fecha de nacimiento del animal */
	@Column(name = "fecha_nacimiento", nullable = false)
	var fechaNacimiento: LocalDate = LocalDate.now(),

	/** Sexo del animal */
	@Enumerated(EnumType.STRING)
	@Column(name = "sexo", nullable = false)
	var sexo: Sexo = Sexo.MACHO,

	/** Descripción del animal */
	@Column(name = "descripcion", nullable = false)
	var descripcion: String = "",

	/** Estatus del animal */
	@Enumerated(EnumType.STRING)
	@Column(name = "estatus", nullable = false)
	var estatus: Estatus = Estatus.DISPONIBLE,

	/** UUID del usuario dueño del animal */
	@Column(name = "usuario_id", nullable = false, updatable = false)
	var usuarioId: UUID? = null,

	/** Fecha de registro del animal */
	@Column(name = "fecha_registro")
	var fechaRegistro: LocalDateTime = LocalDateTime.now(),

	/** Indica si el animal fue marcado como inapropiado */
	@Column(name = "inapropiado")
	var inapropiado: Boolean = false,

	/** Indica si el animal está esterilizado */
	@Column(name = "esterilizado")
	var esterilizado: Boolean = false,

	/** Fecha de última actualización */
	@Column(name = "updated_at")
	var updatedAt: LocalDateTime = LocalDateTime.now()
)
