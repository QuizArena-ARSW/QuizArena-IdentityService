package com.quizarena.identidad.controlador;

import com.quizarena.identidad.dto.BancoRequest;
import com.quizarena.identidad.dto.PreguntaRequest;
import com.quizarena.identidad.modelo.BancoPreguntas;
import com.quizarena.identidad.servicio.ServicioBancos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints de bancos de preguntas (requieren token JWT).
 * El id del autor se obtiene del token autenticado, no del cuerpo.
 */
@RestController
@RequestMapping("/api/bancos")

public class BancoController {

    private final ServicioBancos servicioBancos;

    public BancoController(ServicioBancos servicioBancos) {
        this.servicioBancos = servicioBancos;
    }

    @PostMapping
    public ResponseEntity<?> crearBanco(@Valid @RequestBody BancoRequest req, Authentication auth) {
        UUID idAutor = UUID.fromString(auth.getName()); // el id viene del token
        BancoPreguntas banco = servicioBancos.crearBanco(req.nombre(), req.materia(), idAutor);
        return ResponseEntity.ok(banco);
    }

    @PostMapping("/{id}/preguntas")
    public ResponseEntity<?> agregarPregunta(@PathVariable UUID id,
                                             @Valid @RequestBody PreguntaRequest req) {
        try {
            return ResponseEntity.ok(servicioBancos.agregarPregunta(id, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public List<BancoPreguntas> buscar(@RequestParam(defaultValue = "") String materia) {
        return servicioBancos.buscarPorMateria(materia);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(servicioBancos.obtenerBanco(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
