package com.kotliners.adoptaPerrito.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "Usuario")
data class UsuarioEntity(
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "IDUsuario")
    val id: Int?=null,

    @Column(name = "nombres")
    val nombres: String,

    @Column(name = "apellido_paterno")
    var apellidoPaterno: String,

    @Column(name = "apellido_materno")
    var apellidoMaterno: String,

    @Column(name = "email")
    var email: String,

    @Column(name = "codigo_postal")
    var codigoPostal: String,

    @Column(name = "password")
    var password: String,

    @Column(name = "fecha_nacimiento")
    var fechaNacimiento: LocalDate,

    @Column(name = "token")
    var token: String? = null

)