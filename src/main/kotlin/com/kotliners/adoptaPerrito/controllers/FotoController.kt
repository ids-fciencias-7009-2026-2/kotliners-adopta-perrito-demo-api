package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.services.UsuarioService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

/**
 * Controlador para la subida de imagenes de perfil de usuario.
 * Guarda los archivos en src/main/resources/static/uploads/ y
 * devuelve la URL publica para acceder a la imagen.
 *
 * URL base: /uploads
 */
@RestController
@RequestMapping("/uploads")
class FotoController {

    private val logger: Logger = LoggerFactory.getLogger(FotoController::class.java)

    @Autowired
    lateinit var usuarioService: UsuarioService

    /** Directorio donde se guardan las imagenes subidas. */
    private val uploadDir = Paths.get("src/main/resources/static/uploads")

    /**
     * Sube una imagen de perfil para el usuario autenticado.
     * Valida el token, el tipo de archivo y el tamano maximo (5MB).
     * Guarda el archivo con un nombre unico y devuelve la URL publica.
     *
     * URL:    POST /uploads/foto-perfil
     * Header: Authorization: Bearer <token>
     * Body:   multipart/form-data con campo "file"
     *
     * @param token Token de sesion del usuario autenticado.
     * @param file  Archivo de imagen a subir (jpg, jpeg, png, webp).
     * @return URL publica de la imagen subida.
     */
    @PostMapping("/foto-perfil")
    fun subirFotoPerfil(
        @RequestHeader("Authorization", required = false) token: String?,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")

        val cleanToken = token.replace("Bearer ", "").trim()
        val usuario = usuarioService.findByToken(cleanToken)
            ?: return ResponseEntity.status(401).body("Token invalido")

        // Validar tipo de archivo
        val contentType = file.contentType ?: ""
        val tiposPermitidos = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
        if (contentType !in tiposPermitidos) {
            return ResponseEntity.badRequest().body("Solo se permiten imagenes JPG, PNG o WebP.")
        }

        // Validar tamano maximo (5MB)
        val maxBytes = 5 * 1024 * 1024L
        if (file.size > maxBytes) {
            return ResponseEntity.badRequest().body("La imagen no puede superar 5MB.")
        }

        // Crear directorio si no existe
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir)
            logger.info("Directorio de uploads creado: $uploadDir")
        }

        // Generar nombre unico para el archivo
        val extension = contentType.substringAfter("/")
        val nombreArchivo = "${UUID.randomUUID()}.$extension"
        val destino = uploadDir.resolve(nombreArchivo)

        // Guardar archivo
        file.transferTo(destino.toFile())
        logger.info("Imagen guardada: $nombreArchivo para usuario ${usuario.id}")

        val url = "http://localhost:8080/uploads/$nombreArchivo"
        return ResponseEntity.ok(mapOf("url" to url))
    }
}
