package com.quizarena.identidad.modelo;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Una pregunta dentro de un banco. Tabla: pregunta */
@Entity
@Table(name = "pregunta")
public class PreguntaBanco {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 1000)
    private String enunciado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPregunta tipo;

    @Column(name = "tiempo_limite_seg", nullable = false)
    private int tiempoLimiteSegundos;

    // La pregunta pertenece a un banco
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_banco", nullable = false)
    private BancoPreguntas banco;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpcionBanco> opciones = new ArrayList<>();

    protected PreguntaBanco() { }

    public PreguntaBanco(String enunciado, TipoPregunta tipo, int tiempoLimiteSegundos) {
        this.enunciado = enunciado;
        this.tipo = tipo;
        this.tiempoLimiteSegundos = tiempoLimiteSegundos;
    }

    public void agregarOpcion(OpcionBanco opcion) {
        opcion.setPregunta(this);
        this.opciones.add(opcion);
    }

    /** Valida que la pregunta tenga al menos una opcion correcta. */
    public boolean tieneRespuestaCorrecta() {
        return opciones.stream().anyMatch(OpcionBanco::isEsCorrecta);
    }

    public UUID getId() { return id; }
    public String getEnunciado() { return enunciado; }
    public TipoPregunta getTipo() { return tipo; }
    public int getTiempoLimiteSegundos() { return tiempoLimiteSegundos; }
    public List<OpcionBanco> getOpciones() { return opciones; }

    public void setBanco(BancoPreguntas banco) { this.banco = banco; }
}
