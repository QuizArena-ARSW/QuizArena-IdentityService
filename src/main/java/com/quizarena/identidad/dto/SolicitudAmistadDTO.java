package com.quizarena.identidad.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** Una solicitud de amistad vista desde la perspectiva del usuario autenticado. */
public record SolicitudAmistadDTO(
        UUID id,
        UsuarioResumenDTO otroUsuario,
        String estado,
        LocalDateTime fechaCreacion
) {}
