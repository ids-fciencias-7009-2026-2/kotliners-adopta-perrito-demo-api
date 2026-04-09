package com.kotliners.adoptaPerrito.repositories

import com.kotliners.adoptaPerrito.entities.UsuarioEntity

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import jakarta.transaction.Transactional
import java.util.UUID

/**
 * Repositorio JPA para la entidad UsuarioEntity.
 *
 * Esta interfaz extiende CrudRepository para heredar operaciones CRUD básicas
 * (save, findById, findAll, delete, etc.) y define consultas personalizadas
 * específicas para la lógica de negocio de usuarios.
 *
 * Las consultas personalizadas utilizan JPQL (Java Persistence Query Language)
 * que es independiente del dialecto SQL específico de la base de datos.
 *
 * Responsabilidades principales:
 * - Ejecutar consultas de búsqueda por token, email y credenciales
 * - Actualizar tokens de sesión de usuarios
 * - Proporcionar acceso tipado a operaciones de base de datos
 */
interface UsuarioRepository:CrudRepository<UsuarioEntity,UUID> {

    /**
     * Busca un usuario por su token de sesión.
     *
     * Esta consulta se utiliza principalmente durante el proceso de logout
     * para validar que el token proporcionado corresponde a un usuario válido.
     *
     * @param token El token de autenticación único del usuario
     * @return UsuarioEntity si existe un usuario con ese token, null en caso contrario
     */
    @Query("select u from UsuarioEntity u where u.token = :token")
    fun findByToken(token:String):UsuarioEntity?

    /**
     * Busca un usuario por su correo electrónico.
     *
     * Útil para validaciones de unicidad durante el registro de nuevos usuarios.
     *
     * @param email El correo electrónico del usuario (debe ser único)
     * @return UsuarioEntity si existe un usuario con ese email, null en caso contrario
     */
    @Query("select u from UsuarioEntity u where u.email = :email")
    fun findByEmail(email:String):UsuarioEntity?

    /**
     * Busca un usuario por su nombre de usuario (username).
     * 
     * @param username El nombre de usuario del usuario.
     * @return UsuarioEntity si existe, <code>null</code> en caso contrario.
     */
    @Query("select u from UsuarioEntity u where u.username = :username")
    fun findByUsername(username:String):UsuarioEntity?

    /**
     * Busca un usuario por su CURP.
     * 
     * @param curp La CURP del usuario.
     * @return UsuarioEntity si existe, <code>null</code> en caso contrario.
     */
    @Query("select u from UsuarioEntity u where u.curp = :curp")
    fun findByCurp(curp:String):UsuarioEntity?

    /**
     * Busca un usuario por su correo electrónico y contraseña hasheada.
     *
     * Esta es la consulta principal utilizada durante el proceso de login.
     * Combina email (identificador único) con contraseña hasheada para
     * autenticación segura.
     *
     * @param email El correo electrónico del usuario
     * @param password La contraseña hasheada con SHA-256
     * @return UsuarioEntity si las credenciales coinciden, <code>null</code> en caso contrario
     */
    @Query("select u from UsuarioEntity u where u.email = :email and u.password = :password")
    fun findUserByPasswordAndEmail(email:String,password:String):UsuarioEntity?   
    
    /**
     * Actualiza el token de sesión de un usuario específico.
     *
     * Esta operación se utiliza durante el logout para invalidar la sesión
     * del usuario estableciendo su token como null. También podría usarse
     * para renovar tokens de sesión.
     *
     * La anotación @Modifying indica que esta consulta modifica datos
     * (UPDATE en lugar de SELECT).
     *
     * La anotación @Transactional asegura que la operación se ejecute
     * dentro de una transacción de base de datos.
     *
     * @param id El ID único del usuario cuya sesión se va a modificar
     * @param token El nuevo token (puede ser null para invalidar la sesión)
     */
    @Modifying
    @Transactional
    @Query("update UsuarioEntity u set u.token = :token where u.id = :id")
    fun updateTokenById(id: UUID?, token: String?)
}