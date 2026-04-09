package com.kotliners.adoptaPerrito

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig : WebMvcConfigurer {

    /**
     * Configura CORS para permitir solicitudes desde el frontend (Next.js) en localhost:3000.
     */
    private val logger = LoggerFactory.getLogger(CorsConfig::class.java)

    /**
     * Permite solicitudes CORS desde http://localhost:3000 para todas las rutas y métodos HTTP.
     */
    override fun addCorsMappings(registry: CorsRegistry) {
        logger.info("CORS configurado para localhost:3000 (Next.js)")
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
    }
}