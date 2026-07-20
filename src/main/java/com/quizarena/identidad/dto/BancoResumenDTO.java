package com.quizarena.identidad.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** Vista ligera de un banco, para listarlos en la interfaz. */
public record BancoResumenDTO(
        UUID id,
        String nombre,
        String materia,
        int cantidadPreguntas,
        LocalDateTime fechaCreacion
) {}
