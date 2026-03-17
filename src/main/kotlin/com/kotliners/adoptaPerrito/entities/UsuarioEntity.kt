package com.kotliners.adoptaPerrito.entities

import com.kotliners.adoptaPerrito.domain.Rol

import jakarta.persistence.Column
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * Entidad JPA que representa la tabla "Usuario" en la base de datos.
 *
 * Esta clase mapea directamente a la tabla de usuarios en PostgreSQL y utiliza
 * las anotaciones de JPA/Hibernate para definir:
 * - La estructura de la tabla y sus columnas
 * - Las restricciones de integridad (unicidad, no nulos)
 * - Las relaciones con otras entidades (si las hubiera)
 * - La estrategia de generación de IDs
 *
 * Cada instancia de UsuarioEntity corresponde a exactamente un registro
 * en la tabla "Usuario" de la base de datos.
 *
 * NOTA IMPORTANTE: Esta entidad requiere un constructor sin parámetros
 * para que Hibernate pueda instanciarla durante las operaciones de carga
 * desde la base de datos. El constructor secundario al final de la clase
 * cumple esta función.
 */
@Entity
@Table(name = "Usuario")
data class UsuarioEntity(

    /**
     * Identificador único del usuario.
     * Se genera automáticamente mediante la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDUsuario")
    val id: Int? = null,

    /**
     * Clave Única de Registro de Población (CURP) del usuario.
     * Debe ser única y no nula.
     */
    @Column(name = "curp")
    var curp: String = "",

    /**
     * Nombre de usuario (username).
     * Debe ser único dentro del sistema.
     */
    @Column(name = "username")
    var username: String = "",

    /**
     * Rol del usuario dentro del sistema.
     * Puede ser adoptante o cuidador.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    var rol: Rol = Rol.ADOPTANTE,

    /**
     * URL de la foto de perfil del usuario.
     * Campo opcional.
     */
    @Column(name = "foto_perfil")
    var fotoPerfil: String? = null,

    /**
     * Nombre(s) del usuario.
     */
    @Column(name = "nombres")
    var nombres: String = "",

    /**
     * Apellido paterno del usuario.
     */
    @Column(name = "apellido_paterno")
    var apellidoPaterno: String = "",

    /**
     * Apellido materno del usuario.
     */
    @Column(name = "apellido_materno")
    var apellidoMaterno: String = "",

    /**
     * Correo electrónico del usuario.
     * Debe ser único y se utiliza para autenticación.
     */
    @Column(name = "email")
    var email: String = "",

    /**
     * Código postal asociado al usuario.
     */
    @Column(name = "codigo_postal")
    var codigoPostal: String = "",

    /**
     * Contraseña del usuario.
     * Se almacena de forma cifrada (hash), nunca en texto plano.
     */
    @Column(name = "password")
    var password: String = "",

    /**
     * Token de sesión del usuario.
     * Se utiliza para identificar sesiones activas.
     */
    @Column(name = "token")
    var token: String? = null
) 
