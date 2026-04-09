package com.kotliners.adoptaPerrito.entities

import com.kotliners.adoptaPerrito.domain.Rol

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime

/**
 * Entidad JPA que representa la tabla "usuario" en la base de datos.
 * Los nombres de columna y variables coinciden en todas las capas.
 */
@Entity
@Table(name = "usuario")
data class UsuarioEntity(

    /** Identificador único del usuario (UUID generado automáticamente) */
    @Id
    @UuidGenerator
    @Column(name = "usuario_id", updatable = false, nullable = false)
    val id: String? = null,

    /** Clave Única de Registro de Población */
    @Column(name = "curp", unique = true, nullable = false, length = 18)
    var curp: String = "",

    /** Nombre de usuario único en el sistema */
    @Column(name = "username", unique = true, nullable = false)
    var username: String = "",

    /** Rol del usuario: ADOPTANTE o CUIDADOR */
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    var rol: Rol = Rol.ADOPTANTE,

    /** URL de la foto de perfil (opcional) */
    @Column(name = "foto_perfil")
    var fotoPerfil: String? = null,

    /** Nombre(s) del usuario */
    @Column(name = "nombres", nullable = false)
    var nombres: String = "",

    /** Apellido paterno del usuario */
    @Column(name = "apellido_paterno", nullable = false)
    var apellidoPaterno: String = "",

    /** Apellido materno del usuario */
    @Column(name = "apellido_materno", nullable = false)
    var apellidoMaterno: String = "",

    /** Correo electrónico único del usuario */
    @Column(name = "email", unique = true, nullable = false)
    var email: String = "",

    /** Código postal del usuario (FK a tabla codigo_postal) */
    @Column(name = "codigo_postal", nullable = false, length = 5)
    var codigoPostal: String = "",

    /** Contraseña hasheada con SHA-256 */
    @Column(name = "password", nullable = false)
    var password: String = "",

    /** Token de sesión activo (null si no hay sesión) */
    @Column(name = "token")
    var token: String? = null,

    /** Fecha de última actualización del perfil */
    @Column(name = "fecha_update")
    var fechaUpdate: LocalDateTime? = null,

    /** Fecha de registro del usuario */
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    val fechaRegistro: LocalDateTime = LocalDateTime.now(),

    /** Fecha de eliminación lógica (soft delete) — null si el usuario está activo */
    @Column(name = "fecha_eliminado")
    var fechaEliminado: LocalDateTime? = null
)
