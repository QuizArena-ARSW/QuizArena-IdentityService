package com.quizarena.identidad.modelo;

import jakarta.persistence.*;

import java.util.UUID;

/** Una opcion de una pregunta. Tabla: opcion */
@Entity
@Table(name = "opcion")
public class OpcionBanco {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 255)
    private String texto;

    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private PreguntaBanco pregunta;

    protected OpcionBanco() { }

    public OpcionBanco(String texto, boolean esCorrecta) {
        this.texto = texto;
        this.esCorrecta = esCorrecta;
    }

    public UUID getId() { return id; }
    public String getTexto() { return texto; }
    public boolean isEsCorrecta() { return esCorrecta; }

    public void setPregunta(PreguntaBanco pregunta) { this.pregunta = pregunta; }
}
