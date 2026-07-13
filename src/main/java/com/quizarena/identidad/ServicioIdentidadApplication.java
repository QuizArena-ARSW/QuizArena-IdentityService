package com.quizarena.identidad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del Servicio de Identidad y Contenido de QuizArena.
 * Maneja cuentas (registro/login con JWT), bancos de preguntas e historial.
 */
@SpringBootApplication
public class ServicioIdentidadApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServicioIdentidadApplication.class, args);
    }
}
