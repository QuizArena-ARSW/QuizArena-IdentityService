package com.quizarena.identidad.servicio;

import com.quizarena.identidad.modelo.EstadoSolicitud;
import com.quizarena.identidad.modelo.SolicitudAmistad;
import com.quizarena.identidad.modelo.Usuario;
import com.quizarena.identidad.repositorio.SolicitudAmistadRepository;
import com.quizarena.identidad.repositorio.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** Logica de busqueda de usuarios y del sistema de amigos. */
@Service
public class ServicioAmigos {

    private static final int LIMITE_BUSQUEDA = 20;

    private final SolicitudAmistadRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    public ServicioAmigos(SolicitudAmistadRepository solicitudRepository, UsuarioRepository usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> buscar(String q, UUID self) {
        return usuarioRepository.buscar(q, self, PageRequest.of(0, LIMITE_BUSQUEDA));
    }

    /** Envia una solicitud de amistad. Si ya existe una rechazada entre el par, la reactiva. */
    public SolicitudAmistad enviarSolicitud(UUID remitente, UUID destinatario) {
        if (remitente.equals(destinatario)) {
            throw new IllegalArgumentException("No puedes enviarte una solicitud a ti mismo");
        }

        SolicitudAmistad existente = solicitudRepository.buscarEntre(remitente, destinatario).orElse(null);
        if (existente == null) {
            return solicitudRepository.save(new SolicitudAmistad(remitente, destinatario));
        }
        if (existente.getEstado() == EstadoSolicitud.RECHAZADA) {
            existente.reenviar(remitente, destinatario);
            return solicitudRepository.save(existente);
        }
        throw new IllegalArgumentException("Ya existe una solicitud o amistad con este usuario");
    }

    /** Acepta o rechaza una solicitud recibida. Solo el destinatario puede responder. */
    public SolicitudAmistad responder(UUID idSolicitud, UUID yo, boolean aceptar) {
        SolicitudAmistad solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        if (!solicitud.getIdDestinatario().equals(yo)) {
            throw new IllegalArgumentException("No puedes responder una solicitud que no te fue enviada a ti");
        }
        if (aceptar) {
            solicitud.aceptar();
        } else {
            solicitud.rechazar();
        }
        return solicitudRepository.save(solicitud);
    }

    public List<SolicitudAmistad> recibidas(UUID yo) {
        return solicitudRepository.findByIdDestinatarioAndEstado(yo, EstadoSolicitud.PENDIENTE);
    }

    public List<SolicitudAmistad> enviadas(UUID yo) {
        return solicitudRepository.findByIdRemitenteAndEstado(yo, EstadoSolicitud.PENDIENTE);
    }

    public List<SolicitudAmistad> amigos(UUID yo) {
        return solicitudRepository.buscarAmistadesDe(yo);
    }

    /** Elimina la amistad aceptada entre los dos usuarios, si existe. */
    public void eliminarAmigo(UUID yo, UUID idAmigo) {
        SolicitudAmistad amistad = solicitudRepository.buscarEntre(yo, idAmigo)
                .filter(s -> s.getEstado() == EstadoSolicitud.ACEPTADA)
                .orElseThrow(() -> new IllegalArgumentException("No son amigos"));
        solicitudRepository.delete(amistad);
    }

    public Usuario obtenerUsuario(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }
}
