package com.quizarena.identidad.repositorio;

import com.quizarena.identidad.modelo.Usuario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Acceso a datos de usuarios. Spring Data genera la implementacion. */
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);

    /** Busca por nombre o correo (parcial, sin distinguir mayusculas), excluyendo al propio usuario. */
    @Query("select u from Usuario u where u.id <> :self and " +
            "(lower(u.nombre) like lower(concat('%', :q, '%')) or lower(u.correo) like lower(concat('%', :q, '%')))")
    List<Usuario> buscar(@Param("q") String q, @Param("self") UUID self, Pageable limite);
}
