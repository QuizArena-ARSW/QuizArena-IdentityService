package com.quizarena.identidad.controlador;

import com.quizarena.identidad.dto.RegistroPartidaRequest;
import com.quizarena.identidad.modelo.RegistroPartida;
import com.quizarena.identidad.servicio.ServicioHistorial;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints del historial de partidas.
 *  - POST /api/historial : lo llama el Servicio de Juego al terminar una partida.
 *  - GET  /api/historial/{idUsuario} : consulta el historial de un usuario (requiere token).
 */
@RestController
@RequestMapping("/api/historial")

public class HistorialController {

    private final ServicioHistorial servicioHistorial;

    public HistorialController(ServicioHistorial servicioHistorial) {
        this.servicioHistorial = servicioHistorial;
    }

    @PostMapping
    public RegistroPartida registrar(@RequestBody RegistroPartidaRequest req) {
        return servicioHistorial.registrarResultado(req);
    }

    @GetMapping("/{idUsuario}")
    public List<RegistroPartida> historial(@PathVariable UUID idUsuario) {
        return servicioHistorial.obtenerHistorial(idUsuario);
    }
}
