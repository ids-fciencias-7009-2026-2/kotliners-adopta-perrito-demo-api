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

/**
 * Entidad JPA que representa la tabla "Usuario" en la base de datos.
 */
@Entity
@Table(name = "usuario")
data class UsuarioEntity(

    /**
     * Identificador único del usuario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    val id: Int? = null,

    /**
     * Clave Única de Registro de Población (CURP).
     */
    @Column(name = "curp", unique = true, nullable = false, length = 18)
    var curp: String = "",

    /**
     * Nombre de usuario (username).
     */
    @Column(name = "username", unique = true, nullable = false)
    var username: String = "",

    /**
     * Rol del usuario dentro del sistema.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    var rol: Rol = Rol.ADOPTANTE,

    /**
     * URL de la foto de perfil del usuario.
     */
    @Column(name = "foto_perfil")
    var fotoPerfil: String? = null,

    /**
     * Nombre(s) del usuario.
     */
    @Column(name = "nombres", nullable = false)
    var nombres: String = "",

    /**
     * Apellido paterno del usuario.
     */
    @Column(name = "apellido_paterno", nullable = false)
    var apellidoPaterno: String = "",

    /**
     * Apellido materno del usuario.
     */
    @Column(name = "apellido_materno", nullable = false)
    var apellidoMaterno: String = "",

    /**
     * Correo electrónico del usuario.
     */
    @Column(name = "email", unique = true, nullable = false)
    var email: String = "",

    /**
     * Código postal asociado al usuario.
     */
    @Column(name = "codigo_postal", nullable = false)
    var codigoPostal: String = "",

    /**
     * Contraseña del usuario hasheada con SHA-256.
     */
    @Column(name = "password", nullable = false)
    var password: String = "",

    /**
     * Token de sesión del usuario.
     */
    @Column(name = "token")
    var token: String? = null
)
