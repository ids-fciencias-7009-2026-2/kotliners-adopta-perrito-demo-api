package com.kotliners.adoptaPerrito.exceptions

import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

import org.springframework.http.ResponseEntity

/**
 * Manejador global de excepciones para la aplicación de adopción.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Maneja las excepciones de tipo IllegalArgumentException lanzadas en cualquier parte de la aplicación.
     * 
     * Devuelve una respuesta con código 400 (Bad Request) y un mensaje de error descriptivo.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity
            .badRequest()
            .body(mapOf("error" to ex.message.orEmpty()))
    }

    /**
     * Maneja las excepciones de validación lanzadas cuando los datos de entrada no cumplen con las restricciones definidas en los DTOs.
     * 
     * Devuelve una respuesta con código 400 (Bad Request) y un mapa de errores donde 
     * la clave es el nombre del campo y el valor es el mensaje de error correspondiente.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errors = ex.bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "Error") }
        return ResponseEntity.badRequest().body(errors)
    }
}