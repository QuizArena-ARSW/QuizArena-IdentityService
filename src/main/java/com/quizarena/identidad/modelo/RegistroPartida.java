package com.quizarena.identidad.modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro del resultado de una partida jugada por un usuario.
 * El Servicio de Juego llama a este servicio (por REST) para guardar cada
 * resultado al terminar una partida. Tabla: registro_partida
 */
@Entity
@Table(name = "registro_partida")
public class RegistroPartida {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

    @Column(name = "id_banco")
    private UUID idBanco;

    @Column(length = 100)
    private String materia;

    @Column(name = "puntaje_obtenido", nullable = false)
    private int puntajeObtenido;

    @Column(name = "posicion_final", nullable = false)
    private int posicionFinal;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected RegistroPartida() { }

    public RegistroPartida(UUID idUsuario, UUID idBanco, String materia,
                           int puntajeObtenido, int posicionFinal) {
        this.idUsuario = idUsuario;
        this.idBanco = idBanco;
        this.materia = materia;
        this.puntajeObtenido = puntajeObtenido;
        this.posicionFinal = posicionFinal;
        this.fecha = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getIdUsuario() { return idUsuario; }
    public UUID getIdBanco() { return idBanco; }
    public String getMateria() { return materia; }
    public int getPuntajeObtenido() { return puntajeObtenido; }
    public int getPosicionFinal() { return posicionFinal; }
    public LocalDateTime getFecha() { return fecha; }
}
