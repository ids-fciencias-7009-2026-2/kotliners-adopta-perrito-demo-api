package com.kotliners.adoptaPerrito

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Configuracion de CORS para permitir solicitudes desde el frontend.
 * El origen permitido se configura via la variable de entorno FRONTEND_URL.
 */
@Configuration
class CorsConfig : WebMvcConfigurer {

    private val logger = LoggerFactory.getLogger(CorsConfig::class.java)

    @Value("\${frontend.url:http://localhost:3000}")
    lateinit var frontendUrl: String

    /**
     * Permite solicitudes CORS desde el frontend configurado en frontend.url.
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        logger.info("CORS configurado para: $frontendUrl")
        registry.addMapping("/**")
            .allowedOrigins(frontendUrl)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("*")
    }
}
