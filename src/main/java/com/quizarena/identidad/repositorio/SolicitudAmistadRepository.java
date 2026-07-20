package com.quizarena.identidad.repositorio;

import com.quizarena.identidad.modelo.EstadoSolicitud;
import com.quizarena.identidad.modelo.SolicitudAmistad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Acceso a datos de solicitudes/amistades. */
public interface SolicitudAmistadRepository extends JpaRepository<SolicitudAmistad, UUID> {

    /** Cualquier fila (en cualquier estado) entre los dos usuarios, sin importar direccion. */
    @Query("select s from SolicitudAmistad s where " +
            "(s.idRemitente = :a and s.idDestinatario = :b) or " +
            "(s.idRemitente = :b and s.idDestinatario = :a)")
    Optional<SolicitudAmistad> buscarEntre(@Param("a") UUID a, @Param("b") UUID b);

    List<SolicitudAmistad> findByIdDestinatarioAndEstado(UUID idDestinatario, EstadoSolicitud estado);

    List<SolicitudAmistad> findByIdRemitenteAndEstado(UUID idRemitente, EstadoSolicitud estado);

    @Query("select s from SolicitudAmistad s where s.estado = 'ACEPTADA' and " +
            "(s.idRemitente = :idUsuario or s.idDestinatario = :idUsuario)")
    List<SolicitudAmistad> buscarAmistadesDe(@Param("idUsuario") UUID idUsuario);
}
