package com.quizarena.identidad.repositorio;

import com.quizarena.identidad.modelo.RegistroPartida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Acceso a datos del historial de partidas. */
public interface RegistroPartidaRepository extends JpaRepository<RegistroPartida, UUID> {
    List<RegistroPartida> findByIdUsuarioOrderByFechaDesc(UUID idUsuario);
}
