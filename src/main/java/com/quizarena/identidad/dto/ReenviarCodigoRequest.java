package com.quizarena.identidad.dto;

import jakarta.validation.constraints.NotBlank;

/** Datos para pedir un nuevo codigo de verificacion. */
public record ReenviarCodigoRequest(@NotBlank String correo) {}
