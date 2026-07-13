package com.quizarena.identidad.controlador;

import com.quizarena.identidad.dto.LoginRequest;
import com.quizarena.identidad.dto.RegistroRequest;
import com.quizarena.identidad.dto.TokenResponse;
import com.quizarena.identidad.servicio.ServicioAutenticacion;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Endpoints publicos de registro e inicio de sesion. */
@RestController
@RequestMapping("/api/auth")

public class AuthController {

    private final ServicioAutenticacion servicioAuth;

    public AuthController(ServicioAutenticacion servicioAuth) {
        this.servicioAuth = servicioAuth;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequest req) {
        try {
            TokenResponse resp = servicioAuth.registrar(req);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            TokenResponse resp = servicioAuth.autenticar(req);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}
