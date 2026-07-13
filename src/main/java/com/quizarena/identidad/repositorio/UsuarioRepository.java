package com.quizarena.identidad.repositorio;

import com.quizarena.identidad.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Acceso a datos de usuarios. Spring Data genera la implementacion. */
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
}
