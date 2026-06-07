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

    /** Código 2FA enviado por correo */
    @Column(name = "codigo_2fa", length = 6)
    var codigo2fa: String? = null,

    /** Fecha de expiración del código 2FA */
    @Column(name = "codigo_2fa_expira")
    var codigo2faExpira: LocalDateTime? = null,

    /** Intentos fallidos consecutivos de login */
    @Column(name = "intentos_fallidos")
    var intentosFallidos: Int = 0,

    /** Fecha hasta la cual la cuenta está bloqueada */
    @Column(name = "bloqueado_hasta")
    var bloqueadoHasta: LocalDateTime? = null,

    /** Si el correo fue verificado */
    @Column(name = "verificado")
    var verificado: Boolean = false,

    /** Token para verificar correo */
    @Column(name = "token_verificacion")
    var tokenVerificacion: String? = null,

    /** Token para recuperar contraseña */
    @Column(name = "token_recuperacion")
    var tokenRecuperacion: String? = null,

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
