package com.kotliners.adoptaPerrito.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

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
    @Column(name = "curp")
    var curp: String,


    /**
     * Nombre de usuario (username), debe ser único y no nulo.
     */
    @Column(name = "username")
    var username: String,

    /**
     * Rol del usuario (cuidador o adoptante), no nulo.
     */
    @Column(name = "rol")
    var rol: String,

    /**
    * URL de la foto de perfil, puede ser nulo.
    */
    @Column(name = "foto_perfil")
    var fotoPerfil: String? = null,

    /**
     * Nombre(s) del usuario, no nulo.
     */
    @Column(name = "nombres")
    var nombres: String,

    /**
     * Apellido paterno del usuario, no nulo.
     */
    @Column(name = "apellido_paterno")
    var apellidoPaterno: String,

    /**
     * Apellido materno del usuario, no nulo.
     */
    @Column(name = "apellido_materno")
    var apellidoMaterno: String,
    
    /**
    * Correo electrónico del usuario, debe ser único y no nulo.
    */
    @Column(name = "email")
    var email: String,

    /**
     * Código postal del usuario, no nulo.
     */
    @Column(name = "codigo_postal")
    var codigoPostal: String,

    /**
    * Contraseña del usuario, no nulo.
    */
    @Column(name = "password")
    var password: String,

    /**
    * Fecha de nacimiento del usuario, no nulo.
    */
    @Column(name = "token")
    var token: String? = null
)