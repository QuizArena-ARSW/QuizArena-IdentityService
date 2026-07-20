package com.quizarena.identidad.controlador;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Traduce los errores de @Valid al mismo formato {"error": "..."} que ya usan
 * los controladores, para que el frontend muestre el mensaje real (ej. "la
 * contrasena debe incluir un caracter especial") en vez de un 400 generico.
 */
@RestControllerAdvice
public class ManejadorErroresValidacion {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Datos invalidos");
        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }
}
