package com.quizarena.identidad.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Datos para enviar una solicitud de amistad a otro usuario. */
public record SolicitudAmistadRequest(
        @NotNull UUID idDestinatario
) {}
