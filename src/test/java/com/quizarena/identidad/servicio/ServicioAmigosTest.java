package com.quizarena.identidad.servicio;

import com.quizarena.identidad.modelo.EstadoSolicitud;
import com.quizarena.identidad.modelo.SolicitudAmistad;
import com.quizarena.identidad.modelo.Usuario;
import com.quizarena.identidad.repositorio.SolicitudAmistadRepository;
import com.quizarena.identidad.repositorio.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Sistema de amigos: envio/respuesta de solicitudes y eliminacion de
 * amistad, con el repositorio simulado (sin base de datos real -- eso ya
 * lo cubre AuthControllerIntegrationTest para el flujo de autenticacion).
 */
@ExtendWith(MockitoExtension.class)
class ServicioAmigosTest {

    @Mock SolicitudAmistadRepository solicitudRepository;
    @Mock UsuarioRepository usuarioRepository;

    ServicioAmigos servicio;

    UUID juan = UUID.randomUUID();
    UUID ana = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        servicio = new ServicioAmigos(solicitudRepository, usuarioRepository);
        lenient().when(solicitudRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void noSePuedeEnviarUnaSolicitudAUnoMismo() {
        assertThatThrownBy(() -> servicio.enviarSolicitud(juan, juan))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("a ti mismo");
    }

    @Test
    void enviarSolicitudNuevaLaCreaPendiente() {
        when(solicitudRepository.buscarEntre(juan, ana)).thenReturn(Optional.empty());

        SolicitudAmistad resultado = servicio.enviarSolicitud(juan, ana);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);
        assertThat(resultado.getIdRemitente()).isEqualTo(juan);
        assertThat(resultado.getIdDestinatario()).isEqualTo(ana);
    }

    @Test
    void enviarSolicitudReactivaUnaRechazadaPreviaEnVezDeDuplicarla() {
        SolicitudAmistad rechazadaAntes = new SolicitudAmistad(ana, juan); // direccion contraria, a proposito
        rechazadaAntes.rechazar();
        when(solicitudRepository.buscarEntre(juan, ana)).thenReturn(Optional.of(rechazadaAntes));

        SolicitudAmistad resultado = servicio.enviarSolicitud(juan, ana);

        assertThat(resultado).isSameAs(rechazadaAntes); // misma fila, no una nueva
        assertThat(resultado.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);
        assertThat(resultado.getIdRemitente()).isEqualTo(juan);
    }

    @Test
    void noSePuedeEnviarUnaSolicitudSiYaHayUnaPendienteOAceptada() {
        SolicitudAmistad pendiente = new SolicitudAmistad(juan, ana);
        when(solicitudRepository.buscarEntre(juan, ana)).thenReturn(Optional.of(pendiente));

        assertThatThrownBy(() -> servicio.enviarSolicitud(juan, ana))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elDestinatarioPuedeAceptarUnaSolicitud() {
        SolicitudAmistad solicitud = new SolicitudAmistad(juan, ana);
        UUID idSolicitud = UUID.randomUUID();
        when(solicitudRepository.findById(idSolicitud)).thenReturn(Optional.of(solicitud));

        SolicitudAmistad resultado = servicio.responder(idSolicitud, ana, true);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSolicitud.ACEPTADA);
    }

    @Test
    void elDestinatarioPuedeRechazarUnaSolicitud() {
        SolicitudAmistad solicitud = new SolicitudAmistad(juan, ana);
        UUID idSolicitud = UUID.randomUUID();
        when(solicitudRepository.findById(idSolicitud)).thenReturn(Optional.of(solicitud));

        SolicitudAmistad resultado = servicio.responder(idSolicitud, ana, false);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSolicitud.RECHAZADA);
    }

    @Test
    void nadieMasQueElDestinatarioPuedeResponderLaSolicitud() {
        SolicitudAmistad solicitud = new SolicitudAmistad(juan, ana);
        UUID idSolicitud = UUID.randomUUID();
        UUID intruso = UUID.randomUUID();
        when(solicitudRepository.findById(idSolicitud)).thenReturn(Optional.of(solicitud));

        assertThatThrownBy(() -> servicio.responder(idSolicitud, intruso, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE); // no cambio
    }

    @Test
    void responderUnaSolicitudInexistenteFalla() {
        UUID idSolicitud = UUID.randomUUID();
        when(solicitudRepository.findById(idSolicitud)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.responder(idSolicitud, ana, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void eliminarAmigoBorraLaAmistadAceptada() {
        SolicitudAmistad amistad = new SolicitudAmistad(juan, ana);
        amistad.aceptar();
        when(solicitudRepository.buscarEntre(juan, ana)).thenReturn(Optional.of(amistad));

        servicio.eliminarAmigo(juan, ana);

        verify(solicitudRepository).delete(amistad);
    }

    @Test
    void noSePuedeEliminarUnaAmistadQueNoEstaAceptada() {
        SolicitudAmistad pendiente = new SolicitudAmistad(juan, ana); // aun no aceptada
        when(solicitudRepository.buscarEntre(juan, ana)).thenReturn(Optional.of(pendiente));

        assertThatThrownBy(() -> servicio.eliminarAmigo(juan, ana))
                .isInstanceOf(IllegalArgumentException.class);
        verify(solicitudRepository, never()).delete(any());
    }

    @Test
    void obtenerUsuarioDevuelveElUsuarioSiExiste() {
        Usuario usuario = new Usuario("juan@mail.com", "hash", "Juan", com.quizarena.identidad.modelo.Rol.ESTUDIANTE);
        when(usuarioRepository.findById(juan)).thenReturn(Optional.of(usuario));

        assertThat(servicio.obtenerUsuario(juan)).isSameAs(usuario);
    }

    @Test
    void obtenerUsuarioFallaSiNoExiste() {
        when(usuarioRepository.findById(juan)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtenerUsuario(juan))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
