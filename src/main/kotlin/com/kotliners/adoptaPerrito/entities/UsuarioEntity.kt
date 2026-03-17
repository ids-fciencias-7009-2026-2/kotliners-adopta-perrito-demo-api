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
 * Entidad que representa la tabla "Usuario" en la base de datos.
 * 
 * Cada instancia de UsuarioEntity corresponde a un registro en la tabla "Usuario".
 * 
 * Responsabilidades:
 * - Mapear los campos de la tabla "Usuario" a propiedades de la clase.
 * - Proporcionar una estructura para almacenar información de los usuarios del sistema.
 */
@Entity
@Table(name = "Usuario")
data class UsuarioEntity(

    /**
     * Identificador único del usuario, generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDUsuario")
    val id: Int? = null,

    /**
     * CURP del usuario, debe ser único y no nulo.
     */
    @Column(name = "curp", unique = true, nullable = false, length = 18)
    var curp: String,


    /**
     * Nombre de usuario (username), debe ser único y no nulo.
     */
    @Column(name = "username", unique = true, nullable = false)
    var username: String,

    /**
     * Rol del usuario (cuidador o adoptante), no nulo.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    var rol: Rol,

    /**
    * URL de la foto de perfil, puede ser nulo.
    */
    @Column(name = "foto_perfil")
    var fotoPerfil: String? = null,

    /**
     * Nombre(s) del usuario, no nulo.
     */
    @Column(name = "nombres", nullable = false)
    var nombres: String,

    /**
     * Apellido paterno del usuario, no nulo.
     */
    @Column(name = "apellido_paterno", nullable = false)
    var apellidoPaterno: String,

    /**
     * Apellido materno del usuario, no nulo.
     */
    @Column(name = "apellido_materno", nullable = false)
    var apellidoMaterno: String,
    
    /**
    * Correo electrónico del usuario, debe ser único y no nulo.
    */
    @Column(name = "email", unique = true, nullable = false)
    var email: String,

    /**
     * Código postal del usuario, no nulo.
     */
    @Column(name = "codigo_postal", nullable = false)
    var codigoPostal: String,

    /**
    * Contraseña del usuario, no nulo.
    */
    @Column(name = "password", nullable = false)
    var password: String,

    /**
    * Fecha de nacimiento del usuario, no nulo.
    */
    @Column(name = "token")
    var token: String? = null
)