package com.quizarena.identidad.dto;

/** Respuesta al registrarse: aun no hay token, falta verificar el correo. */
public record RegistroResponse(String correo, String mensaje) {}
