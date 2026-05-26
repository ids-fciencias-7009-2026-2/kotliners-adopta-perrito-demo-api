package com.kotliners.adoptaPerrito.entities

import com.kotliners.adoptaPerrito.domain.Rol

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.LocalDateTime
import java.util.UUID

/**
 * Entidad JPA que representa la tabla "usuario" en la base de datos.
 */
@Entity
@Table(name = "usuario")
data class UsuarioEntity(

    /** Identificador único del usuario (UUID generado automáticamente) */
    @Id
    @UuidGenerator
    @Column(name = "usuario_id", updatable = false, nullable = false)
    val id: UUID? = null,

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

    /** Código postal del usuario */
    @Column(name = "codigo_postal", nullable = false, length = 5)
    var codigoPostal: String = "",

    /** Contraseña hasheada con SHA-256 */
    @Column(name = "password", nullable = false)
    var password: String = "",

    /** Token de sesión activo (null si no hay sesión) */
    @Column(name = "token")
    var token: String? = null,

    /** Indica si el correo del usuario ya fue verificado */
    @Column(name = "email_verificado", nullable = false)
    var emailVerificado: Boolean = false,

    /** Token temporal para verificacion de correo */
    @Column(name = "email_verificacion_token")
    var emailVerificacionToken: String? = null,

    /** Fecha de expiracion del token de verificacion de correo */
    @Column(name = "email_verificacion_expira")
    var emailVerificacionExpira: LocalDateTime? = null,

    /** Token temporal para recuperar contrasena */
    @Column(name = "password_reset_token")
    var passwordResetToken: String? = null,

    /** Fecha de expiracion del token de recuperacion de contrasena */
    @Column(name = "password_reset_expira")
    var passwordResetExpira: LocalDateTime? = null,

    /** Indica si el usuario tiene segundo factor habilitado */
    @Column(name = "two_factor_enabled", nullable = false)
    var twoFactorEnabled: Boolean = false,

    /** Codigo temporal de segundo factor */
    @Column(name = "two_factor_code")
    var twoFactorCode: String? = null,

    /** Fecha de expiracion del codigo de segundo factor */
    @Column(name = "two_factor_expira")
    var twoFactorExpira: LocalDateTime? = null,

    /** Numero consecutivo de intentos fallidos de login */
    @Column(name = "intentos_fallidos", nullable = false)
    var intentosFallidos: Int = 0,

    /** Fecha hasta la que el usuario esta bloqueado por intentos fallidos */
    @Column(name = "bloqueado_hasta")
    var bloqueadoHasta: LocalDateTime? = null,

    /** Fecha de última actualización del perfil */
    @Column(name = "fecha_update")
    var fechaUpdate: LocalDateTime? = null,

    /** Fecha de registro del usuario */
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    val fechaRegistro: LocalDateTime = LocalDateTime.now(),

    /** Fecha de eliminación lógica (soft delete) */
    @Column(name = "fecha_eliminado")
    var fechaEliminado: LocalDateTime? = null
)
