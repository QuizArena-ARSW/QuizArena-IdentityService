package com.quizarena.identidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Datos para agregar una pregunta a un banco. */
public record PreguntaRequest(
        @NotBlank String enunciado,
        @NotBlank String tipo,            // "OPCION_MULTIPLE" o "VERDADERO_FALSO"
        int tiempoLimiteSegundos,
        @NotEmpty List<OpcionRequest> opciones
) {}
