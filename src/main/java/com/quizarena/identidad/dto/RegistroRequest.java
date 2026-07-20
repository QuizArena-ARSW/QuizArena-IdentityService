package com.quizarena.identidad.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos para registrar un usuario nuevo. */
public record RegistroRequest(
        @NotBlank @Email String correo,
        @NotBlank
        @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
        @Pattern(
                regexp = ".*[^a-zA-Z0-9].*",
                message = "La contrasena debe incluir al menos un caracter especial"
        )
        String contrasena,
        @NotBlank String nombre
) {}
