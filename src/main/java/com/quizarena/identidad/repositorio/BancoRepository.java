package com.quizarena.identidad.repositorio;

import com.quizarena.identidad.modelo.BancoPreguntas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Acceso a datos de bancos de preguntas. */
public interface BancoRepository extends JpaRepository<BancoPreguntas, UUID> {
    List<BancoPreguntas> findByMateriaContainingIgnoreCase(String materia);
    List<BancoPreguntas> findByIdAutor(UUID idAutor);
}
