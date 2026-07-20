package com.quizarena.identidad.servicio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Envio de correos transaccionales (verificacion de cuenta). */
@Service
public class ServicioCorreo {

    private final JavaMailSender mailSender;
    private final String remitente;

    public ServicioCorreo(JavaMailSender mailSender,
                          @Value("${quizarena.mail.remitente}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    public void enviarCodigoVerificacion(String correoDestino, String nombre, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(correoDestino);
        mensaje.setSubject("Tu código de verificación de QuizArena");
        mensaje.setText(
                "Hola " + nombre + ",\n\n" +
                "Tu código de verificación es: " + codigo + "\n\n" +
                "Ingrésalo en la página para activar tu cuenta. Vence en 15 minutos.\n\n" +
                "Si no creaste esta cuenta, ignora este correo.\n\n" +
                "— QuizArena"
        );
        mailSender.send(mensaje);
    }
}
