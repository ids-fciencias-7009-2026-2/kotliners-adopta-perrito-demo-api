package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.adapters.CloudinaryAdapter
import com.kotliners.adoptaPerrito.services.UsuarioService
import com.kotliners.adoptaPerrito.utils.TokenExtractor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * Controlador para la subida de imagenes de perfil.
 * Usa Cloudinary como almacenamiento — no guarda archivos localmente.
 *
 * URL: POST /uploads/foto-perfil
 */
@RestController
@RequestMapping("/uploads")
class FotoController {

    private val logger: Logger = LoggerFactory.getLogger(FotoController::class.java)

    @Autowired
    lateinit var usuarioService: UsuarioService

    @Autowired
    lateinit var cloudinaryAdapter: CloudinaryAdapter

    private val tiposPermitidos = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
    private val maxBytes = 5 * 1024 * 1024L

    /**
     * Sube una imagen de perfil a Cloudinary y devuelve la URL publica.
     *
     * URL:    POST /uploads/foto-perfil
     * Header: Authorization: Bearer <token>
     * Body:   multipart/form-data con campo "file"
     *
     * @param token Token de sesion del usuario autenticado.
     * @param file  Archivo de imagen a subir.
     * @return URL publica de Cloudinary.
     */
    @PostMapping("/foto-perfil")
    fun subirFotoPerfil(
        @RequestHeader("Authorization", required = false) token: String?,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")

        val usuario = TokenExtractor.resolveUser(token, usuarioService)
            ?: return ResponseEntity.status(401).body("Token invalido")

        val contentType = file.contentType ?: ""
        if (contentType !in tiposPermitidos) {
            return ResponseEntity.badRequest().body("Solo se permiten imagenes JPG, PNG o WebP.")
        }

        if (file.size > maxBytes) {
            return ResponseEntity.badRequest().body("La imagen no puede superar 5MB.")
        }

        val resultado = cloudinaryAdapter.subirImagen(file, folder = "colitas/perfiles")
        if (resultado.isFailure) {
            logger.error("Error al subir imagen para usuario ${usuario.id}: ${resultado.exceptionOrNull()?.message}")
            return ResponseEntity.status(500).body("No se pudo subir la imagen. Intenta de nuevo.")
        }

        val url = resultado.getOrThrow()
        logger.info("Foto de perfil subida para usuario ${usuario.id}: $url")
        return ResponseEntity.ok(mapOf("url" to url))
    }
}
