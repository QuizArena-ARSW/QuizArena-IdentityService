package com.quizarena.identidad.modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Solicitud de amistad entre dos usuarios. Tabla: solicitud_amistad
 *
 * Una sola fila representa tanto la solicitud pendiente como, una vez
 * aceptada, la amistad vigente (el campo estado es la fuente de verdad).
 * Evita una segunda tabla para "amigos".
 */
@Entity
@Table(name = "solicitud_amistad")
public class SolicitudAmistad {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID idRemitente;

    @Column(nullable = false)
    private UUID idDestinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaRespuesta;

    protected SolicitudAmistad() { } // requerido por JPA

    public SolicitudAmistad(UUID idRemitente, UUID idDestinatario) {
        this.idRemitente = idRemitente;
        this.idDestinatario = idDestinatario;
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    /** Reactiva esta fila como una solicitud nueva (caso: reenvio tras rechazo). */
    public void reenviar(UUID idRemitente, UUID idDestinatario) {
        this.idRemitente = idRemitente;
        this.idDestinatario = idDestinatario;
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaRespuesta = null;
    }

    public void aceptar() {
        this.estado = EstadoSolicitud.ACEPTADA;
        this.fechaRespuesta = LocalDateTime.now();
    }

    public void rechazar() {
        this.estado = EstadoSolicitud.RECHAZADA;
        this.fechaRespuesta = LocalDateTime.now();
    }

    /** Dado uno de los dos usuarios de la solicitud, devuelve el id del otro. */
    public UUID idContrario(UUID idUsuario) {
        return idRemitente.equals(idUsuario) ? idDestinatario : idRemitente;
    }

    public UUID getId() { return id; }
    public UUID getIdRemitente() { return idRemitente; }
    public UUID getIdDestinatario() { return idDestinatario; }
    public EstadoSolicitud getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
}
