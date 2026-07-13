package com.quizarena.identidad.dto;

import java.util.UUID;

/** Datos que envia el Servicio de Juego para guardar el resultado de una partida. */
public record RegistroPartidaRequest(
        UUID idUsuario,
        UUID idBanco,
        String materia,
        int puntajeObtenido,
        int posicionFinal
) {}
