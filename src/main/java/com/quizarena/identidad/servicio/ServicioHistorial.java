package com.quizarena.identidad.servicio;

import com.quizarena.identidad.dto.RegistroPartidaRequest;
import com.quizarena.identidad.modelo.RegistroPartida;
import com.quizarena.identidad.repositorio.RegistroPartidaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** Logica del historial de partidas. */
@Service
public class ServicioHistorial {

    private final RegistroPartidaRepository registroRepository;

    public ServicioHistorial(RegistroPartidaRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    /** Guarda el resultado de una partida (lo llama el Servicio de Juego). */
    public RegistroPartida registrarResultado(RegistroPartidaRequest req) {
        RegistroPartida registro = new RegistroPartida(
                req.idUsuario(), req.idBanco(), req.materia(),
                req.puntajeObtenido(), req.posicionFinal());
        return registroRepository.save(registro);
    }

    public List<RegistroPartida> obtenerHistorial(UUID idUsuario) {
        return registroRepository.findByIdUsuarioOrderByFechaDesc(idUsuario);
    }
}
