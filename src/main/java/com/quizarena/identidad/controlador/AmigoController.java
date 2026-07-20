package com.quizarena.identidad.controlador;

import com.quizarena.identidad.dto.SolicitudAmistadDTO;
import com.quizarena.identidad.dto.SolicitudAmistadRequest;
import com.quizarena.identidad.dto.UsuarioResumenDTO;
import com.quizarena.identidad.modelo.SolicitudAmistad;
import com.quizarena.identidad.modelo.Usuario;
import com.quizarena.identidad.servicio.ServicioAmigos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoints del sistema de amigos: busqueda de usuarios, solicitudes de
 * amistad y lista de amigos. El id del usuario autenticado se obtiene del
 * token, nunca del cuerpo de la peticion.
 */
@RestController
@RequestMapping("/api/amigos")
public class AmigoController {

    private final ServicioAmigos servicioAmigos;

    public AmigoController(ServicioAmigos servicioAmigos) {
        this.servicioAmigos = servicioAmigos;
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscar(@RequestParam(defaultValue = "") String q, Authentication auth) {
        UUID yo = UUID.fromString(auth.getName());
        List<UsuarioResumenDTO> resultado = servicioAmigos.buscar(q, yo).stream()
                .map(this::aResumen)
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/solicitudes")
    public ResponseEntity<?> enviarSolicitud(@Valid @RequestBody SolicitudAmistadRequest req, Authentication auth) {
        UUID yo = UUID.fromString(auth.getName());
        try {
            SolicitudAmistad solicitud = servicioAmigos.enviarSolicitud(yo, req.idDestinatario());
            return ResponseEntity.ok(aDTO(solicitud, yo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/solicitudes/recibidas")
    public ResponseEntity<?> recibidas(Authentication auth) {
        UUID yo = UUID.fromString(auth.getName());
        return ResponseEntity.ok(servicioAmigos.recibidas(yo).stream().map(s -> aDTO(s, yo)).toList());
    }

    @GetMapping("/solicitudes/enviadas")
    public ResponseEntity<?> enviadas(Authentication auth) {
        UUID yo = UUID.fromString(auth.getName());
        return ResponseEntity.ok(servicioAmigos.enviadas(yo).stream().map(s -> aDTO(s, yo)).toList());
    }

    @PostMapping("/solicitudes/{id}/aceptar")
    public ResponseEntity<?> aceptar(@PathVariable UUID id, Authentication auth) {
        return responder(id, auth, true);
    }

    @PostMapping("/solicitudes/{id}/rechazar")
    public ResponseEntity<?> rechazar(@PathVariable UUID id, Authentication auth) {
        return responder(id, auth, false);
    }

    private ResponseEntity<?> responder(UUID id, Authentication auth, boolean aceptar) {
        UUID yo = UUID.fromString(auth.getName());
        try {
            SolicitudAmistad solicitud = servicioAmigos.responder(id, yo, aceptar);
            return ResponseEntity.ok(aDTO(solicitud, yo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> misAmigos(Authentication auth) {
        UUID yo = UUID.fromString(auth.getName());
        List<UsuarioResumenDTO> amigos = servicioAmigos.amigos(yo).stream()
                .map(s -> aResumen(servicioAmigos.obtenerUsuario(s.idContrario(yo))))
                .toList();
        return ResponseEntity.ok(amigos);
    }

    @DeleteMapping("/{idAmigo}")
    public ResponseEntity<?> eliminarAmigo(@PathVariable UUID idAmigo, Authentication auth) {
        UUID yo = UUID.fromString(auth.getName());
        try {
            servicioAmigos.eliminarAmigo(yo, idAmigo);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private UsuarioResumenDTO aResumen(Usuario u) {
        return new UsuarioResumenDTO(u.getId(), u.getNombre(), u.getCorreo());
    }

    private SolicitudAmistadDTO aDTO(SolicitudAmistad s, UUID yo) {
        Usuario otro = servicioAmigos.obtenerUsuario(s.idContrario(yo));
        return new SolicitudAmistadDTO(s.getId(), aResumen(otro), s.getEstado().name(), s.getFechaCreacion());
    }
}
