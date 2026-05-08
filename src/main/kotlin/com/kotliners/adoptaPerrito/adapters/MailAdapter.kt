package com.kotliners.adoptaPerrito.adapters

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

/**
 * Adaptador para el envio de correos electronicos usando JavaMailSender.
 * Configurado para usar Mailtrap como sandbox SMTP en desarrollo.
 */
@Service
class MailAdapter(private val mailSender: JavaMailSender) {

    private val logger = LoggerFactory.getLogger(MailAdapter::class.java)

    @Value("\${internal.email.from}")
    lateinit var fromEmail: String

    /**
     * Envia un correo HTML con soporte para CC.
     *
     * @param to Destinatario principal.
     * @param cc Destinatario en copia (opcional).
     * @param subject Asunto del correo.
     * @param htmlBody Cuerpo del correo en formato HTML.
     * @return Result.success si se envio correctamente, Result.failure si hubo error.
     */
    fun sendHtmlEmail(
        to: String,
        subject: String,
        htmlBody: String,
        cc: String? = null
    ): Result<Unit> {
        return try {
            val mimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")
            helper.setFrom(fromEmail)
            helper.setTo(to)
            if (cc != null) helper.setCc(cc)
            helper.setSubject(subject)
            helper.setText(htmlBody, true)
            mailSender.send(mimeMessage)
            logger.info("Correo enviado a $to${if (cc != null) " (CC: $cc)" else ""}")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Error al enviar correo a $to: ${e.message}")
            Result.failure(e)
        }
    }
}
