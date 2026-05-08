package com.kotliners.adoptaPerrito.controllers

import com.kotliners.adoptaPerrito.services.UsuarioService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import com.kotliners.adoptaPerrito.utils.TokenExtractor
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

/**
 * Controlador para la subida y servicio de imagenes de perfil.
 * Guarda los archivos en el directorio configurado en upload.dir
 * y los sirve en GET /uploads/{filename}.
 */
@RestController
@RequestMapping("/uploads")
class FotoController {

    private val logger: Logger = LoggerFactory.getLogger(FotoController::class.java)

    @Autowired
    lateinit var usuarioService: UsuarioService

    /** URL base del servidor, configurable via app.base-url. */
    @Value("\${app.base-url:http://localhost:8080}")
    lateinit var baseUrl: String

    /** Directorio de uploads, configurable via upload.dir. */
    @Value("\${upload.dir:#{systemProperties['user.dir']}/uploads}")
    lateinit var uploadDirPath: String

    private val tiposPermitidos = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
    private val maxBytes = 5 * 1024 * 1024L

    /**
     * Sube una imagen de perfil para el usuario autenticado.
     * Valida token, tipo de archivo (jpg/png/webp) y tamano maximo (5MB).
     *
     * URL:    POST /uploads/foto-perfil
     * Header: Authorization: Bearer <token>
     * Body:   multipart/form-data con campo "file"
     *
     * @param token Token de sesion del usuario autenticado.
     * @param file  Archivo de imagen a subir.
     * @return URL publica de la imagen subida.
     */
    @PostMapping("/foto-perfil")
    fun subirFotoPerfil(
        @RequestHeader("Authorization", required = false) token: String?,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Any> {
        if (token == null) return ResponseEntity.status(401).body("Token requerido")

        val usuario = TokenExtractor.resolveUser(token, usuarioService)
            ?: return ResponseEntity.status(401).body(if (token == null) "Token requerido" else "Token invalido")

        val contentType = file.contentType ?: ""
        if (contentType !in tiposPermitidos) {
            return ResponseEntity.badRequest().body("Solo se permiten imagenes JPG, PNG o WebP.")
        }

        if (file.size > maxBytes) {
            return ResponseEntity.badRequest().body("La imagen no puede superar 5MB.")
        }

        val uploadDir = Paths.get(uploadDirPath)
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir)

        // Nombre UUID para evitar colisiones y path traversal
        val extension = when (contentType) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val nombreArchivo = "${UUID.randomUUID()}.$extension"
        val destino = uploadDir.resolve(nombreArchivo).normalize()

        // Verificar que el destino esta dentro del directorio de uploads (proteccion path traversal)
        if (!destino.startsWith(uploadDir.normalize())) {
            logger.warn("Intento de path traversal detectado")
            return ResponseEntity.badRequest().body("Nombre de archivo invalido.")
        }

        file.transferTo(destino.toFile())
        logger.info("Imagen guardada: $nombreArchivo para usuario ${usuario.id}")

        return ResponseEntity.ok(mapOf("url" to "$baseUrl/uploads/$nombreArchivo"))
    }

    /**
     * Sirve una imagen subida por su nombre de archivo.
     * URL: GET /uploads/{filename}
     *
     * @param filename Nombre del archivo a servir.
     * @return El archivo de imagen como recurso.
     */
    @GetMapping("/{filename}")
    fun servirImagen(@PathVariable filename: String): ResponseEntity<Resource> {
        // Proteccion contra path traversal
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build()
        }

        val uploadDir = Paths.get(uploadDirPath)
        val archivo = uploadDir.resolve(filename).normalize()

        if (!archivo.startsWith(uploadDir.normalize()) || !Files.exists(archivo)) {
            return ResponseEntity.notFound().build()
        }

        val resource = FileSystemResource(archivo)
        val contentType = Files.probeContentType(archivo) ?: "application/octet-stream"
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$filename\"")
            .contentType(MediaType.parseMediaType(contentType))
            .body(resource)
    }
}
