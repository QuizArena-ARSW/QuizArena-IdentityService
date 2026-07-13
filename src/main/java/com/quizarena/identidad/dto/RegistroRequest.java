package com.quizarena.identidad.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para registrar un usuario nuevo. */
public record RegistroRequest(
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres") String contrasena,
        @NotBlank String nombre
) {}
