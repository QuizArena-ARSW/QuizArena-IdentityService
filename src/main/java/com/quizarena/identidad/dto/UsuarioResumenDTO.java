package com.quizarena.identidad.dto;

import java.util.UUID;

/** Vista publica minima de un usuario (sin contrasena). */
public record UsuarioResumenDTO(
        UUID id,
        String nombre,
        String correo
) {}
