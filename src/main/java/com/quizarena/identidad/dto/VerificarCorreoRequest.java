package com.quizarena.identidad.dto;

import jakarta.validation.constraints.NotBlank;

/** Datos para confirmar el codigo de verificacion enviado por correo. */
public record VerificarCorreoRequest(
        @NotBlank String correo,
        @NotBlank String codigo
) {}
