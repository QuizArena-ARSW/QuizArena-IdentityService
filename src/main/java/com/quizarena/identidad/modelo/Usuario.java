package com.quizarena.identidad.modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/** Usuario del sistema. Tabla: usuario */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(nullable = false, length = 255)
    private String hashContrasena;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    protected Usuario() { } // requerido por JPA

    public Usuario(String correo, String hashContrasena, String nombre, Rol rol) {
        this.correo = correo;
        this.hashContrasena = hashContrasena;
        this.nombre = nombre;
        this.rol = rol;
        this.fechaRegistro = LocalDateTime.now();
    }

    public boolean esDocente() {
        return rol == Rol.DOCENTE;
    }

    public UUID getId() { return id; }
    public String getCorreo() { return correo; }
    public String getHashContrasena() { return hashContrasena; }
    public String getNombre() { return nombre; }
    public Rol getRol() { return rol; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }

    public void setHashContrasena(String hashContrasena) { this.hashContrasena = hashContrasena; }
}
