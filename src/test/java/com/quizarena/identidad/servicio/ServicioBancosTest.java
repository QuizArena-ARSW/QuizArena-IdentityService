package com.quizarena.identidad.servicio;

import com.quizarena.identidad.dto.OpcionRequest;
import com.quizarena.identidad.dto.PreguntaRequest;
import com.quizarena.identidad.modelo.BancoPreguntas;
import com.quizarena.identidad.modelo.Rol;
import com.quizarena.identidad.modelo.Usuario;
import com.quizarena.identidad.repositorio.BancoRepository;
import com.quizarena.identidad.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioBancosTest {

    @Mock BancoRepository bancoRepository;
    @Mock UsuarioRepository usuarioRepository;

    ServicioBancos servicio;
    UUID autor = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        servicio = new ServicioBancos(bancoRepository, usuarioRepository, "oficial@quizarena.com");
        lenient().when(bancoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void crearBancoLoGuardaConElAutorCorrecto() {
        BancoPreguntas banco = servicio.crearBanco("Parcial 1", "Arquitectura", autor);

        assertThat(banco.getNombre()).isEqualTo("Parcial 1");
        assertThat(banco.getIdAutor()).isEqualTo(autor);
        verify(bancoRepository).save(banco);
    }

    @Test
    void agregarPreguntaConUnaOpcionCorrectaSeGuarda() {
        BancoPreguntas banco = new BancoPreguntas("Parcial 1", "Arquitectura", autor);
        UUID idBanco = UUID.randomUUID();
        when(bancoRepository.findById(idBanco)).thenReturn(Optional.of(banco));
        PreguntaRequest req = new PreguntaRequest("¿2+2?", "OPCION_MULTIPLE", 20, List.of(
                new OpcionRequest("4", true), new OpcionRequest("5", false)));

        BancoPreguntas resultado = servicio.agregarPregunta(idBanco, req);

        assertThat(resultado.cantidadPreguntas()).isEqualTo(1);
        assertThat(resultado.getPreguntas().get(0).tieneRespuestaCorrecta()).isTrue();
    }

    @Test
    void agregarPreguntaSinNingunaOpcionCorrectaFalla() {
        BancoPreguntas banco = new BancoPreguntas("Parcial 1", "Arquitectura", autor);
        UUID idBanco = UUID.randomUUID();
        when(bancoRepository.findById(idBanco)).thenReturn(Optional.of(banco));
        PreguntaRequest req = new PreguntaRequest("¿2+2?", "OPCION_MULTIPLE", 20, List.of(
                new OpcionRequest("4", false), new OpcionRequest("5", false)));

        assertThatThrownBy(() -> servicio.agregarPregunta(idBanco, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos una opcion correcta");
        verify(bancoRepository, never()).save(any());
    }

    @Test
    void agregarPreguntaAUnBancoInexistenteFalla() {
        UUID idBanco = UUID.randomUUID();
        when(bancoRepository.findById(idBanco)).thenReturn(Optional.empty());
        PreguntaRequest req = new PreguntaRequest("¿2+2?", "OPCION_MULTIPLE", 20, List.of(
                new OpcionRequest("4", true)));

        assertThatThrownBy(() -> servicio.agregarPregunta(idBanco, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Banco no encontrado");
    }

    @Test
    void buscarPorMateriaDelegaEnElRepositorio() {
        when(bancoRepository.findByMateriaContainingIgnoreCase("Arquitectura"))
                .thenReturn(List.of(new BancoPreguntas("Parcial 1", "Arquitectura", autor)));

        assertThat(servicio.buscarPorMateria("Arquitectura")).hasSize(1);
    }

    @Test
    void buscarPorAutorDelegaEnElRepositorio() {
        when(bancoRepository.findByIdAutor(autor))
                .thenReturn(List.of(new BancoPreguntas("Parcial 1", "Arquitectura", autor)));

        assertThat(servicio.buscarPorAutor(autor)).hasSize(1);
    }

    @Test
    void obtenerBancoInexistenteFalla() {
        UUID idBanco = UUID.randomUUID();
        when(bancoRepository.findById(idBanco)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtenerBanco(idBanco))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bancosOficialesDevuelveLosDeLaCuentaOficial() {
        Usuario oficial = new Usuario("oficial@quizarena.com", "hash", "QuizArena", Rol.DOCENTE);
        when(usuarioRepository.findByCorreo("oficial@quizarena.com")).thenReturn(Optional.of(oficial));
        when(bancoRepository.findByIdAutor(oficial.getId()))
                .thenReturn(List.of(new BancoPreguntas("Demo", "General", oficial.getId())));

        assertThat(servicio.bancosOficiales()).hasSize(1);
    }

    @Test
    void bancosOficialesDevuelveVacioSiNoExisteLaCuentaOficial() {
        when(usuarioRepository.findByCorreo("oficial@quizarena.com")).thenReturn(Optional.empty());

        assertThat(servicio.bancosOficiales()).isEmpty();
    }
}
