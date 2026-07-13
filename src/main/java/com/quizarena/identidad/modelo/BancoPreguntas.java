package com.quizarena.identidad.modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Banco de preguntas de una materia. Tabla: banco_preguntas */
@Entity
@Table(name = "banco_preguntas")
public class BancoPreguntas {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String materia;

    @Column(name = "id_autor", nullable = false)
    private UUID idAutor;

    @Column(nullable = false)
    private boolean publico;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    // Un banco tiene muchas preguntas. cascade = ALL: guardar el banco guarda sus preguntas.
    @OneToMany(mappedBy = "banco", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PreguntaBanco> preguntas = new ArrayList<>();

    protected BancoPreguntas() { }

    public BancoPreguntas(String nombre, String materia, UUID idAutor) {
        this.nombre = nombre;
        this.materia = materia;
        this.idAutor = idAutor;
        this.publico = false;
        this.fechaCreacion = LocalDateTime.now();
    }

    public void agregarPregunta(PreguntaBanco pregunta) {
        pregunta.setBanco(this);
        this.preguntas.add(pregunta);
    }

    public int cantidadPreguntas() {
        return preguntas.size();
    }

    public void hacerPublico() { this.publico = true; }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getMateria() { return materia; }
    public UUID getIdAutor() { return idAutor; }
    public boolean isPublico() { return publico; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public List<PreguntaBanco> getPreguntas() { return preguntas; }
}
