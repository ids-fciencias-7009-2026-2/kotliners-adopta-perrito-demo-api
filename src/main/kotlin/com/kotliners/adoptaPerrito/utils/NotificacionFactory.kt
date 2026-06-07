package com.kotliners.adoptaPerrito.utils

/**
 * Factory Method para crear contenido de notificaciones por correo.
 * Centraliza la logica de construccion de correos segun el tipo de evento.
 */
object NotificacionFactory {

    fun interesManifestado(cuidadorNombre: String, adoptanteNombre: String, animalNombre: String, adoptanteEmail: String): Pair<String, String> {
        val subject = "Nuevo interés en $animalNombre"
        val body = EmailBuilder()
            .greeting(cuidadorNombre)
            .line("<strong>$adoptanteNombre</strong> está interesado en adoptar a <strong>$animalNombre</strong>.")
            .line("Correo de contacto: <strong>$adoptanteEmail</strong>")
            .line("Puedes contactarlo directamente para continuar el proceso.")
            .build()
        return Pair(subject, body)
    }

    fun interesRetirado(cuidadorNombre: String, adoptanteNombre: String, animalNombre: String): Pair<String, String> {
        val subject = "Interés retirado en $animalNombre"
        val body = EmailBuilder()
            .greeting(cuidadorNombre)
            .line("<strong>$adoptanteNombre</strong> ha retirado su interés en <strong>$animalNombre</strong>.")
            .line("No te preocupes, tu mascota sigue disponible para otros adoptantes.")
            .build()
        return Pair(subject, body)
    }

    fun loginExitoso(nombre: String): Pair<String, String> {
        val subject = "Nuevo inicio de sesión en Colitas Felices"
        val body = EmailBuilder()
            .greeting(nombre)
            .line("Se ha detectado un nuevo inicio de sesión en tu cuenta.")
            .line("Si no reconoces esta actividad, contacta a soporte de inmediato.")
            .build()
        return Pair(subject, body)
    }

    fun codigo2fa(nombre: String, codigo: String): Pair<String, String> {
        val subject = "Código de verificación – Colitas Felices"
        val body = EmailBuilder()
            .greeting(nombre)
            .line("Tu código de verificación es:")
            .line("<h2 style=\"letter-spacing:8px;text-align:center\">$codigo</h2>")
            .line("Este código expira en 10 minutos.")
            .build()
        return Pair(subject, body)
    }

    fun verificacionCorreo(nombre: String, enlace: String): Pair<String, String> {
        val subject = "Verifica tu correo – Colitas Felices"
        val body = EmailBuilder()
            .greeting(nombre)
            .line("Gracias por registrarte en <strong>Colitas Felices</strong>.")
            .line("Para activar tu cuenta, haz clic en el siguiente enlace:")
            .button("Verificar mi correo", enlace)
            .build()
        return Pair(subject, body)
    }

    fun recuperarContrasena(nombre: String, enlace: String): Pair<String, String> {
        val subject = "Restablecer contraseña – Colitas Felices"
        val body = EmailBuilder()
            .greeting(nombre)
            .line("Recibimos una solicitud para restablecer tu contraseña.")
            .button("Restablecer contraseña", enlace)
            .line("Si no solicitaste esto, ignora este correo.")
            .build()
        return Pair(subject, body)
    }

    fun publicacionEliminada(nombre: String, animalNombre: String, motivo: String): Pair<String, String> {
        val subject = "Publicación eliminada – Colitas Felices"
        val body = EmailBuilder()
            .greeting(nombre)
            .line("La publicación de <strong>$animalNombre</strong> fue eliminada por un administrador.")
            .line("<strong>Motivo:</strong> $motivo")
            .line("Si consideras que fue un error, contacta al equipo de soporte.")
            .build()
        return Pair(subject, body)
    }

    fun cuentaEliminada(nombre: String): Pair<String, String> {
        val subject = "Tu cuenta en Colitas Felices ha sido eliminada"
        val body = EmailBuilder()
            .greeting(nombre)
            .line("Tu cuenta ha sido eliminada exitosamente.")
            .line("Si no solicitaste esta acción, contacta a soporte de inmediato.")
            .build()
        return Pair(subject, body)
    }
}
