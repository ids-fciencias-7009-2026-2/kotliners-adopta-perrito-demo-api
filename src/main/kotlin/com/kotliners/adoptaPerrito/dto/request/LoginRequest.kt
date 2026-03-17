package com.kotliners.adoptaPerrito.dto.request

/**
 * DTO (Data Transfer Object) utilizado para recibir las credenciales de autenticación
 * de un usuario cuando intenta iniciar sesión en el sistema.
 *
 * Este objeto se utiliza específicamente en el endpoint POST /usuarios/login
 * y contiene únicamente los campos necesarios para la autenticación:
 * email y contraseña en texto plano (que será hasheada antes de la validación).
 *
 * La contraseña se recibe en texto plano desde el cliente pero nunca se almacena
 * así en la base de datos - siempre se hashea usando SHA-256.
 */
data class LoginRequest(

    /**
     * Correo electrónico del usuario que intenta autenticarse.
     * Debe existir en la base de datos y ser único por usuario.
     * Se utiliza como identificador principal para la búsqueda del usuario.
     */
    val email: String,

    /**
     * Contraseña en texto plano proporcionada por el usuario.
     * Esta contraseña será hasheada con SHA-256 antes de compararse
     * con el hash almacenado en la base de datos.
     */
    val password: String
)
