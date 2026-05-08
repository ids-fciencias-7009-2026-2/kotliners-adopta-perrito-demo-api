package com.kotliners.adoptaPerrito.adapters

import com.cloudinary.Cloudinary
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * Adaptador para subida de imagenes a Cloudinary.
 * Devuelve la URL segura (https) de la imagen subida.
 */
@Service
class CloudinaryAdapter(
    @Value("\${cloudinary.cloud-name}") cloudName: String,
    @Value("\${cloudinary.api-key}") apiKey: String,
    @Value("\${cloudinary.api-secret}") apiSecret: String
) {
    private val logger = LoggerFactory.getLogger(CloudinaryAdapter::class.java)

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to cloudName,
            "api_key" to apiKey,
            "api_secret" to apiSecret,
            "secure" to true
        )
    )

    /**
     * Sube un archivo de imagen a Cloudinary.
     * @param file Archivo multipart a subir.
     * @param folder Carpeta destino en Cloudinary (default: "colitas").
     * @return URL segura de la imagen subida.
     */
    fun subirImagen(file: MultipartFile, folder: String = "colitas"): Result<String> {
        return try {
            val result = cloudinary.uploader().upload(
                file.bytes,
                mapOf(
                    "folder" to folder,
                    "resource_type" to "image",
                    "quality" to "auto"
                )
            )
            val url = result["secure_url"] as String
            logger.info("Imagen subida a Cloudinary: $url")
            Result.success(url)
        } catch (e: Exception) {
            logger.error("Error al subir imagen a Cloudinary: ${e.message}")
            Result.failure(e)
        }
    }
}
