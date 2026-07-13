package com.quizarena.identidad.servicio;

import com.quizarena.identidad.dto.PreguntaRequest;
import com.quizarena.identidad.modelo.*;
import com.quizarena.identidad.repositorio.BancoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** Logica de gestion de bancos de preguntas. */
@Service
public class ServicioBancos {

    private final BancoRepository bancoRepository;

    public ServicioBancos(BancoRepository bancoRepository) {
        this.bancoRepository = bancoRepository;
    }

    public BancoPreguntas crearBanco(String nombre, String materia, UUID idAutor) {
        BancoPreguntas banco = new BancoPreguntas(nombre, materia, idAutor);
        return bancoRepository.save(banco);
    }

    /** Agrega una pregunta (con sus opciones) a un banco existente. */
    public BancoPreguntas agregarPregunta(UUID idBanco, PreguntaRequest req) {
        BancoPreguntas banco = bancoRepository.findById(idBanco)
                .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado"));

        TipoPregunta tipo = TipoPregunta.valueOf(req.tipo());
        PreguntaBanco pregunta = new PreguntaBanco(req.enunciado(), tipo, req.tiempoLimiteSegundos());

        req.opciones().forEach(o -> pregunta.agregarOpcion(new OpcionBanco(o.texto(), o.esCorrecta())));

        if (!pregunta.tieneRespuestaCorrecta()) {
            throw new IllegalArgumentException("La pregunta debe tener al menos una opcion correcta");
        }

        banco.agregarPregunta(pregunta);
        return bancoRepository.save(banco);
    }

    public List<BancoPreguntas> buscarPorMateria(String materia) {
        return bancoRepository.findByMateriaContainingIgnoreCase(materia);
    }

    public BancoPreguntas obtenerBanco(UUID idBanco) {
        return bancoRepository.findById(idBanco)
                .orElseThrow(() -> new IllegalArgumentException("Banco no encontrado"));
    }
}
