package com.quizarena.identidad.controlador;

import com.quizarena.identidad.dto.BancoRequest;
import com.quizarena.identidad.dto.BancoResumenDTO;
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
 * Endpoints de bancos de preguntas.
 * El id del autor se obtiene del token autenticado, nunca del cuerpo.
 *
 * Nota: no lleva @CrossOrigin. CORS lo maneja el API Gateway de forma
 * centralizada (si se duplicara, el navegador rechazaria las respuestas).
 */
@RestController
@RequestMapping("/api/bancos")
public class BancoController {

    private final ServicioBancos servicioBancos;

    public BancoController(ServicioBancos servicioBancos) {
        this.servicioBancos = servicioBancos;
    }

    /** Crea un banco. El autor es el usuario del token. */
    @PostMapping
    public ResponseEntity<?> crearBanco(@Valid @RequestBody BancoRequest req, Authentication auth) {
        UUID idAutor = UUID.fromString(auth.getName());
        BancoPreguntas banco = servicioBancos.crearBanco(req.nombre(), req.materia(), idAutor);
        return ResponseEntity.ok(aResumen(banco));
    }

    /**
     * Lista los bancos del usuario autenticado ("mis bancos").
     * Devuelve un resumen ligero (sin volcar todas las preguntas).
     */
    @GetMapping("/mios")
    public ResponseEntity<?> misBancos(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).body(List.of());
        }
        UUID idAutor = UUID.fromString(auth.getName());
        List<BancoResumenDTO> bancos = servicioBancos.buscarPorAutor(idAutor).stream()
                .map(this::aResumen)
                .toList();
        return ResponseEntity.ok(bancos);
    }

    /** Agrega una pregunta (con sus opciones) a un banco. */
    @PostMapping("/{id}/preguntas")
    public ResponseEntity<?> agregarPregunta(@PathVariable UUID id,
                                             @Valid @RequestBody PreguntaRequest req) {
        try {
            BancoPreguntas banco = servicioBancos.agregarPregunta(id, req);
            return ResponseEntity.ok(aResumen(banco));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /** Busca bancos por materia. */
    @GetMapping
    public List<BancoResumenDTO> buscar(@RequestParam(defaultValue = "") String materia) {
        return servicioBancos.buscarPorMateria(materia).stream()
                .map(this::aResumen)
                .toList();
    }

    /** Bancos predeterminados de QuizArena, disponibles para cualquier usuario. */
    @GetMapping("/oficiales")
    public List<BancoResumenDTO> oficiales() {
        return servicioBancos.bancosOficiales().stream()
                .map(this::aResumen)
                .toList();
    }

    /**
     * Devuelve un banco COMPLETO (con preguntas y opciones).
     * Lo consume el Servicio de Juego para armar la partida.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(servicioBancos.obtenerBanco(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    private BancoResumenDTO aResumen(BancoPreguntas b) {
        return new BancoResumenDTO(b.getId(), b.getNombre(), b.getMateria(),
                b.cantidadPreguntas(), b.getFechaCreacion());
    }
}
