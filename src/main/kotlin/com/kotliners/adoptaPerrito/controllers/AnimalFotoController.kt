package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.adapters.CloudinaryAdapter
import com.kotliners.adoptaPerrito.services.AnimalService
import com.kotliners.adoptaPerrito.services.UsuarioService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * Controlador para gestionar las fotos de los animales.
 * Sube a Cloudinary y persiste en foto_animal.
 */
@RestController
@RequestMapping("/api/animales")
class AnimalFotoController {

    private val logger = LoggerFactory.getLogger(AnimalFotoController::class.java)

    @Autowired lateinit var animalService: AnimalService
    @Autowired lateinit var userService: UsuarioService
    @Autowired lateinit var cloudinaryAdapter: CloudinaryAdapter

    private val tiposPermitidos = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")

    /**
     * Sube una foto para un animal a Cloudinary y la guarda en foto_animal.
     * URL: POST /api/animales/{id}/fotos
     */
    @PostMapping("/{id}/fotos", consumes = ["multipart/form-data"])
    fun subirFoto(
        @RequestHeader("Authorization", required = false) token: String?,
        @PathVariable id: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        val userFound = userService.findByToken(token.replace("Bearer ", "").trim())
            ?: return ResponseEntity.status(401).body("Token invalido")
        if (file.contentType !in tiposPermitidos) return ResponseEntity.badRequest().body("Solo JPG, PNG o WebP.")
        if (file.size > 5 * 1024 * 1024L) return ResponseEntity.badRequest().body("Maximo 5MB.")
        return try {
            val url = animalService.agregarFoto(id, file, userFound.id!!, userFound.rol, cloudinaryAdapter)
            logger.info("Foto subida para animal $id: $url")
            ResponseEntity.ok(mapOf("url" to url))
        } catch (e: IllegalArgumentException) {
            val msg = e.message ?: "No autorizado"
            if (msg.contains("no encontrad", ignoreCase = true)) {
                ResponseEntity.status(404).body(msg)
            } else {
                ResponseEntity.status(403).body(msg)
            }
        }
    }

    /**
     * Elimina una foto de un animal por su URL.
     * URL: DELETE /api/animales/{id}/fotos
     * Body: { "url": "https://..." }
     */
    @DeleteMapping("/{id}/fotos")
    fun eliminarFoto(
        @RequestHeader("Authorization", required = false) token: String?,
        @PathVariable id: String,
        @RequestBody body: Map<String, String>
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")
        val userFound = userService.findByToken(token.replace("Bearer ", "").trim())
            ?: return ResponseEntity.status(401).body("Token invalido")
            
        val url = body["url"] ?: return ResponseEntity.badRequest().body("URL requerida")
        return try {
            animalService.eliminarFoto(id, url, userFound.id!!, userFound.rol)
            ResponseEntity.ok("Foto eliminada")
        } catch (e: IllegalArgumentException) {
            val msg = e.message ?: "No autorizado"
            if (msg.contains("no encontrad", ignoreCase = true)) {
                ResponseEntity.status(404).body(msg)
            } else {
                ResponseEntity.status(403).body(msg)
            }
        }
    }
}
