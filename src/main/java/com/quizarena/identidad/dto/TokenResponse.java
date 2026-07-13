package com.quizarena.identidad.dto;

/** Respuesta con el token JWT tras un login/registro exitoso. */
public record TokenResponse(String token, String nombre, String correo) {}
