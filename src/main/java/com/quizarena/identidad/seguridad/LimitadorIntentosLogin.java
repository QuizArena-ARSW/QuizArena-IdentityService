package com.quizarena.identidad.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limita los intentos de login fallidos por correo, en memoria: Identidad
 * corre en una sola instancia (a diferencia del Juego, que usa Redis para
 * compartir estado entre replicas), asi que no hace falta esa dependencia
 * solo para esto. Tras demasiados fallos en una ventana de tiempo, bloquea
 * el correo temporalmente para frenar fuerza bruta.
 */
@Component
public class LimitadorIntentosLogin {

    private final int maxIntentos;
    private final long ventanaMs;
    private final long bloqueoMs;

    private final ConcurrentHashMap<String, Estado> estados = new ConcurrentHashMap<>();

    public LimitadorIntentosLogin(
            @Value("${quizarena.login.max-intentos:5}") int maxIntentos,
            @Value("${quizarena.login.ventana-minutos:10}") long ventanaMinutos,
            @Value("${quizarena.login.bloqueo-minutos:5}") long bloqueoMinutos) {
        this.maxIntentos = maxIntentos;
        this.ventanaMs = ventanaMinutos * 60_000;
        this.bloqueoMs = bloqueoMinutos * 60_000;
    }

    /** Segundos restantes de bloqueo para este correo, o 0 si no esta bloqueado. */
    public long segundosBloqueadoRestantes(String correo) {
        Estado estado = estados.get(normalizar(correo));
        if (estado == null || estado.bloqueadoHasta == 0) return 0;
        long restante = estado.bloqueadoHasta - System.currentTimeMillis();
        return restante > 0 ? restante / 1000 : 0;
    }

    /** Registra un intento fallido; si supera el limite en la ventana actual, bloquea el correo. */
    public void registrarFallo(String correo) {
        Estado estado = estados.computeIfAbsent(normalizar(correo), k -> new Estado());
        long ahora = System.currentTimeMillis();

        synchronized (estado) {
            if (ahora - estado.primerFalloTs > ventanaMs) {
                estado.fallos.set(0);
                estado.primerFalloTs = ahora;
            }
            if (estado.fallos.incrementAndGet() >= maxIntentos) {
                estado.bloqueadoHasta = ahora + bloqueoMs;
            }
        }
    }

    /** Limpia el contador de este correo tras un login exitoso. */
    public void registrarExito(String correo) {
        estados.remove(normalizar(correo));
    }

    private String normalizar(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase();
    }

    private static class Estado {
        final AtomicInteger fallos = new AtomicInteger(0);
        volatile long primerFalloTs = System.currentTimeMillis();
        volatile long bloqueadoHasta = 0;
    }
}
