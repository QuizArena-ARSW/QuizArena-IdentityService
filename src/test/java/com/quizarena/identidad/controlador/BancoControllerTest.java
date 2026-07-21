package com.quizarena.identidad.controlador;

import com.quizarena.identidad.dto.BancoRequest;
import com.quizarena.identidad.dto.OpcionRequest;
import com.quizarena.identidad.dto.PreguntaRequest;
import com.quizarena.identidad.modelo.BancoPreguntas;
import com.quizarena.identidad.servicio.ServicioBancos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BancoControllerTest {

    @Mock ServicioBancos servicioBancos;
    @Mock Authentication auth;
    BancoController controller;

    UUID autor = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new BancoController(servicioBancos);
    }

    @Test
    void crearBancoUsaElAutorDelToken() {
        lenient().when(auth.getName()).thenReturn(autor.toString());
        BancoPreguntas banco = new BancoPreguntas("Parcial 1", "Arquitectura", autor);
        when(servicioBancos.crearBanco("Parcial 1", "Arquitectura", autor)).thenReturn(banco);

        ResponseEntity<?> resp = controller.crearBanco(new BancoRequest("Parcial 1", "Arquitectura"), auth);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void misBancosSinAutenticacionDevuelve401() {
        ResponseEntity<?> resp = controller.misBancos(null);

        assertThat(resp.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void misBancosDevuelveLosDelUsuarioAutenticado() {
        when(auth.getName()).thenReturn(autor.toString());
        when(servicioBancos.buscarPorAutor(autor))
                .thenReturn(List.of(new BancoPreguntas("Parcial 1", "Arquitectura", autor)));

        ResponseEntity<?> resp = controller.misBancos(auth);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat((List<?>) resp.getBody()).hasSize(1);
    }

    @Test
    void agregarPreguntaDevuelveElBancoActualizado() {
        UUID idBanco = UUID.randomUUID();
        BancoPreguntas banco = new BancoPreguntas("Parcial 1", "Arquitectura", autor);
        PreguntaRequest req = new PreguntaRequest("¿2+2?", "OPCION_MULTIPLE", 20,
                List.of(new OpcionRequest("4", true)));
        when(servicioBancos.agregarPregunta(idBanco, req)).thenReturn(banco);

        ResponseEntity<?> resp = controller.agregarPregunta(idBanco, req);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void agregarPreguntaAUnBancoInexistenteDevuelve400() {
        UUID idBanco = UUID.randomUUID();
        PreguntaRequest req = new PreguntaRequest("¿2+2?", "OPCION_MULTIPLE", 20,
                List.of(new OpcionRequest("4", true)));
        when(servicioBancos.agregarPregunta(idBanco, req))
                .thenThrow(new IllegalArgumentException("Banco no encontrado"));

        ResponseEntity<?> resp = controller.agregarPregunta(idBanco, req);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void buscarPorMateriaDelegaEnElServicio() {
        when(servicioBancos.buscarPorMateria("Arquitectura"))
                .thenReturn(List.of(new BancoPreguntas("Parcial 1", "Arquitectura", autor)));

        assertThat(controller.buscar("Arquitectura")).hasSize(1);
    }

    @Test
    void oficialesDelegaEnElServicio() {
        when(servicioBancos.bancosOficiales())
                .thenReturn(List.of(new BancoPreguntas("Demo", "General", autor)));

        assertThat(controller.oficiales()).hasSize(1);
    }

    @Test
    void obtenerUnBancoExistente() {
        UUID idBanco = UUID.randomUUID();
        when(servicioBancos.obtenerBanco(idBanco)).thenReturn(new BancoPreguntas("Parcial 1", "Arquitectura", autor));

        ResponseEntity<?> resp = controller.obtener(idBanco);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void obtenerUnBancoInexistenteDevuelve404() {
        UUID idBanco = UUID.randomUUID();
        when(servicioBancos.obtenerBanco(idBanco)).thenThrow(new IllegalArgumentException("Banco no encontrado"));

        ResponseEntity<?> resp = controller.obtener(idBanco);

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        assertThat((Map<String, Object>) resp.getBody()).containsEntry("error", "Banco no encontrado");
    }
}
