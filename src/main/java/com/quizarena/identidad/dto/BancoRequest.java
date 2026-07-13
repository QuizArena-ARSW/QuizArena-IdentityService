package com.quizarena.identidad.dto;

import jakarta.validation.constraints.NotBlank;

/** Datos para crear un banco de preguntas. */
public record BancoRequest(
        @NotBlank String nombre,
        @NotBlank String materia
) {}
