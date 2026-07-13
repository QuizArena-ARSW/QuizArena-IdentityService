package com.quizarena.identidad.dto;

import jakarta.validation.constraints.NotBlank;

/** Una opcion al crear una pregunta. */
public record OpcionRequest(
        @NotBlank String texto,
        boolean esCorrecta
) {}
