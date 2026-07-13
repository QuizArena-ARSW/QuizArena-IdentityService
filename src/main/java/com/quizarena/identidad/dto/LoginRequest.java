package com.quizarena.identidad.dto;

import jakarta.validation.constraints.NotBlank;

/** Datos para iniciar sesion. */
public record LoginRequest(
        @NotBlank String correo,
        @NotBlank String contrasena
) {}
