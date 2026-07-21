package com.quizarena.identidad.controlador;

import com.quizarena.identidad.dto.SolicitudAmistadDTO;
import com.quizarena.identidad.dto.SolicitudAmistadRequest;
import com.quizarena.identidad.dto.UsuarioResumenDTO;
import com.quizarena.identidad.modelo.Rol;
import com.quizarena.identidad.modelo.SolicitudAmistad;
import com.quizarena.identidad.modelo.Usuario;
import com.quizarena.identidad.servicio.ServicioAmigos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AmigoControllerTest {

    @Mock ServicioAmigos servicioAmigos;
    @Mock Authentication auth;
    AmigoController controller;

    UUID yo = UUID.randomUUID();
    UUID ana = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        controller = new AmigoController(servicioAmigos);
        lenient().when(auth.getName()).thenReturn(yo.toString());
    }

    private Usuario usuarioConId(UUID id, String nombre, String correo) throws Exception {
        Usuario u = new Usuario(correo, "hash", nombre, Rol.ESTUDIANTE);
        Field campo = Usuario.class.getDeclaredField("id");
        campo.setAccessible(true);
        campo.set(u, id);
        return u;
    }

    @Test
    void buscarDevuelveLosUsuariosEncontrados() throws Exception {
        when(servicioAmigos.buscar("ana", yo)).thenReturn(List.of(usuarioConId(ana, "Ana", "ana@mail.com")));

        ResponseEntity<?> resp = controller.buscar("ana", auth);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        List<UsuarioResumenDTO> body = (List<UsuarioResumenDTO>) resp.getBody();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).nombre()).isEqualTo("Ana");
    }

    @Test
    void enviarSolicitudExitosaDevuelveLaSolicitud() throws Exception {
        SolicitudAmistad solicitud = new SolicitudAmistad(yo, ana);
        when(servicioAmigos.enviarSolicitud(yo, ana)).thenReturn(solicitud);
        when(servicioAmigos.obtenerUsuario(ana)).thenReturn(usuarioConId(ana, "Ana", "ana@mail.com"));

        ResponseEntity<?> resp = controller.enviarSolicitud(new SolicitudAmistadRequest(ana), auth);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        SolicitudAmistadDTO dto = (SolicitudAmistadDTO) resp.getBody();
        assertThat(dto.otroUsuario().nombre()).isEqualTo("Ana");
        assertThat(dto.estado()).isEqualTo("PENDIENTE");
    }

    @Test
    void enviarSolicitudDuplicadaDevuelve400() {
        when(servicioAmigos.enviarSolicitud(yo, ana))
                .thenThrow(new IllegalArgumentException("Ya existe una solicitud o amistad con este usuario"));

        ResponseEntity<?> resp = controller.enviarSolicitud(new SolicitudAmistadRequest(ana), auth);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void recibidasDevuelveLasSolicitudesPendientes() throws Exception {
        SolicitudAmistad solicitud = new SolicitudAmistad(ana, yo);
        when(servicioAmigos.recibidas(yo)).thenReturn(List.of(solicitud));
        when(servicioAmigos.obtenerUsuario(ana)).thenReturn(usuarioConId(ana, "Ana", "ana@mail.com"));

        ResponseEntity<?> resp = controller.recibidas(auth);

        assertThat(((List<?>) resp.getBody())).hasSize(1);
    }

    @Test
    void enviadasDevuelveLasSolicitudesQueEnvieYo() throws Exception {
        SolicitudAmistad solicitud = new SolicitudAmistad(yo, ana);
        when(servicioAmigos.enviadas(yo)).thenReturn(List.of(solicitud));
        when(servicioAmigos.obtenerUsuario(ana)).thenReturn(usuarioConId(ana, "Ana", "ana@mail.com"));

        ResponseEntity<?> resp = controller.enviadas(auth);

        assertThat(((List<?>) resp.getBody())).hasSize(1);
    }

    @Test
    void aceptarLlamaAlServicioConAceptarEnTrue() throws Exception {
        UUID idSolicitud = UUID.randomUUID();
        SolicitudAmistad solicitud = new SolicitudAmistad(ana, yo);
        solicitud.aceptar();
        when(servicioAmigos.responder(idSolicitud, yo, true)).thenReturn(solicitud);
        when(servicioAmigos.obtenerUsuario(ana)).thenReturn(usuarioConId(ana, "Ana", "ana@mail.com"));

        ResponseEntity<?> resp = controller.aceptar(idSolicitud, auth);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        verify(servicioAmigos).responder(idSolicitud, yo, true);
    }

    @Test
    void rechazarLlamaAlServicioConAceptarEnFalse() throws Exception {
        UUID idSolicitud = UUID.randomUUID();
        SolicitudAmistad solicitud = new SolicitudAmistad(ana, yo);
        solicitud.rechazar();
        when(servicioAmigos.responder(idSolicitud, yo, false)).thenReturn(solicitud);
        when(servicioAmigos.obtenerUsuario(ana)).thenReturn(usuarioConId(ana, "Ana", "ana@mail.com"));

        ResponseEntity<?> resp = controller.rechazar(idSolicitud, auth);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        verify(servicioAmigos).responder(idSolicitud, yo, false);
    }

    @Test
    void responderUnaSolicitudInvalidaDevuelve400() {
        UUID idSolicitud = UUID.randomUUID();
        when(servicioAmigos.responder(idSolicitud, yo, true))
                .thenThrow(new IllegalArgumentException("Solicitud no encontrada"));

        ResponseEntity<?> resp = controller.aceptar(idSolicitud, auth);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void misAmigosResuelveElUsuarioContrarioDeCadaAmistad() throws Exception {
        SolicitudAmistad amistad = new SolicitudAmistad(yo, ana);
        amistad.aceptar();
        when(servicioAmigos.amigos(yo)).thenReturn(List.of(amistad));
        when(servicioAmigos.obtenerUsuario(ana)).thenReturn(usuarioConId(ana, "Ana", "ana@mail.com"));

        ResponseEntity<?> resp = controller.misAmigos(auth);

        List<UsuarioResumenDTO> body = (List<UsuarioResumenDTO>) resp.getBody();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).nombre()).isEqualTo("Ana");
    }

    @Test
    void eliminarAmigoExitoso() {
        ResponseEntity<?> resp = controller.eliminarAmigo(ana, auth);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat((Map<String, Object>) resp.getBody()).containsEntry("ok", true);
        verify(servicioAmigos).eliminarAmigo(yo, ana);
    }

    @Test
    void eliminarAmigoQueNoLoEsDevuelve400() {
        doThrow(new IllegalArgumentException("No son amigos")).when(servicioAmigos).eliminarAmigo(yo, ana);

        ResponseEntity<?> resp = controller.eliminarAmigo(ana, auth);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
    }
}
