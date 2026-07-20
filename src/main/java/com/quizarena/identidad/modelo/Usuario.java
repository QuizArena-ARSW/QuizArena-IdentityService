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

    // Boolean (no boolean primitivo) para que usuarios preexistentes con la
    // columna en NULL (agregada via ddl-auto=update, sin backfill) no rompan el mapeo.
    private Boolean verificado = false;

    @Column(length = 6)
    private String codigoVerificacion;

    private LocalDateTime codigoVerificacionExpira;

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

    /** Asigna un nuevo codigo de verificacion con vencimiento. */
    public void asignarCodigoVerificacion(String codigo, LocalDateTime expira) {
        this.codigoVerificacion = codigo;
        this.codigoVerificacionExpira = expira;
    }

    /** Valida el codigo recibido contra el asignado y su vencimiento, sin revelar cual fallo. */
    public boolean codigoValido(String codigo) {
        return codigoVerificacion != null
                && codigoVerificacion.equals(codigo)
                && codigoVerificacionExpira != null
                && codigoVerificacionExpira.isAfter(LocalDateTime.now());
    }

    public void marcarVerificado() {
        this.verificado = true;
        this.codigoVerificacion = null;
        this.codigoVerificacionExpira = null;
    }

    public UUID getId() { return id; }
    public String getCorreo() { return correo; }
    public String getHashContrasena() { return hashContrasena; }
    public String getNombre() { return nombre; }
    public Rol getRol() { return rol; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public boolean isVerificado() { return Boolean.TRUE.equals(verificado); }

    public void setHashContrasena(String hashContrasena) { this.hashContrasena = hashContrasena; }
}
