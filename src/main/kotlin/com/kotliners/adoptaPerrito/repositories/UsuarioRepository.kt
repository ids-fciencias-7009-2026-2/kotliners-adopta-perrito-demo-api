package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.UsuarioEntity

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * Repositorio para la entidad UsuarioEntity.
 * Responsabilidades:
 * - Ejecutar consultas a la base de datos
 * - Guardar entidades de usuario
 * - Recuperar información de usuarios
 * 
 * Extends CrudRepository<UsuarioEntity, Int> para heredar
 * operaciones CRUD básicas.
 */
interface UsuarioRepository:CrudRepository<UsuarioEntity,Int> {

    /**
     * Busca un usuario por su token.
     * 
     * @param token El token de autenticación del usuario.
     * @return UsuarioEntity si existe, <code>null</code> en caso contrario.
     */
    @Query("select u from UsuarioEntity u where u.token = :token")
    fun findByToken(token:String):UsuarioEntity?

    /**
     * Busca un usuario por su correo electrónico.
     * 
     * @param email El correo electrónico del usuario.
     * @return UsuarioEntity si existe, <code>null</code> en caso contrario.
     */
    @Query("select u from UsuarioEntity u where u.email = :email")
    fun findByEmail(email:String):UsuarioEntity?
    
    /**
     * Busca un usuario por su correo electrónico y contraseña.
     * 
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @return UsuarioEntity si existe, <code>null</code> en caso contrario.
     */
    @Query("select u from UsuarioEntity u where u.email = :email and u.password = :password")
    fun findUserByPasswordAndEmail(email:String,password:String):UsuarioEntity?

    /**
     * Actualiza el token de un usuario dado su ID.
     * 
     * @param id El ID del usuario a actualizar.
     * @param token El nuevo token a asignar al usuario.
     * @return El número de filas afectadas por la actualización.
     */
    @Modifying
    @Transactional
    @Query("update UsuarioEntity u set u.token = :token where u.id = :id")
    fun updateTokenById(id: Int, token: String?)
}