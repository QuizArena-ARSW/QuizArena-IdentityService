package com.quizarena.identidad.servicio;

import com.quizarena.identidad.dto.LoginRequest;
import com.quizarena.identidad.dto.TokenResponse;
import com.quizarena.identidad.modelo.Rol;
import com.quizarena.identidad.modelo.Usuario;
import com.quizarena.identidad.repositorio.UsuarioRepository;
import com.quizarena.identidad.seguridad.LimitadorIntentosLogin;
import com.quizarena.identidad.seguridad.ProveedorJwt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Cubre el flujo de login: credenciales validas/invalidas, verificacion de
 * correo obligatoria y el bloqueo por fuerza bruta (LimitadorIntentosLogin),
 * todo con dependencias externas simuladas (sin BD real).
 */
@ExtendWith(MockitoExtension.class)
class ServicioAutenticacionTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ProveedorJwt proveedorJwt;
    @Mock ServicioCorreo servicioCorreo;
    @Mock LimitadorIntentosLogin limitadorIntentos;

    ServicioAutenticacion servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioAutenticacion(
                usuarioRepository, passwordEncoder, proveedorJwt, servicioCorreo, limitadorIntentos, 15);
    }

    private Usuario usuarioVerificado(String correo, String hash) {
        Usuario usuario = new Usuario(correo, hash, "Juan", Rol.ESTUDIANTE);
        usuario.marcarVerificado();
        return usuario;
    }

    @Test
    void loginConCredencialesCorrectasDevuelveToken() {
        Usuario usuario = usuarioVerificado("juan@mail.com", "hash-correcto");
        when(limitadorIntentos.segundosBloqueadoRestantes("juan@mail.com")).thenReturn(0L);
        when(usuarioRepository.findByCorreo("juan@mail.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("secreta123!", "hash-correcto")).thenReturn(true);
        when(proveedorJwt.generarToken(usuario)).thenReturn("token-falso");

        TokenResponse resp = servicio.autenticar(new LoginRequest("juan@mail.com", "secreta123!"));

        assertThat(resp.token()).isEqualTo("token-falso");
        verify(limitadorIntentos).registrarExito("juan@mail.com");
        verify(limitadorIntentos, never()).registrarFallo(any());
    }

    @Test
    void loginConContrasenaIncorrectaRegistraElFalloYRechaza() {
        Usuario usuario = usuarioVerificado("juan@mail.com", "hash-correcto");
        when(limitadorIntentos.segundosBloqueadoRestantes("juan@mail.com")).thenReturn(0L);
        when(usuarioRepository.findByCorreo("juan@mail.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave-mala", "hash-correcto")).thenReturn(false);

        assertThatThrownBy(() -> servicio.autenticar(new LoginRequest("juan@mail.com", "clave-mala")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciales incorrectas");

        verify(limitadorIntentos).registrarFallo("juan@mail.com");
        verify(proveedorJwt, never()).generarToken(any());
    }

    @Test
    void loginConCorreoInexistenteSeComportaIgualQueContrasenaIncorrecta() {
        when(limitadorIntentos.segundosBloqueadoRestantes("fantasma@mail.com")).thenReturn(0L);
        when(usuarioRepository.findByCorreo("fantasma@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.autenticar(new LoginRequest("fantasma@mail.com", "loquesea")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciales incorrectas");

        // No debe intentar comparar contra un hash que no existe.
        verify(passwordEncoder, never()).matches(any(), any());
        verify(limitadorIntentos).registrarFallo("fantasma@mail.com");
    }

    @Test
    void loginBloqueadoPorFuerzaBrutaRechazaSinConsultarLaBaseDeDatos() {
        when(limitadorIntentos.segundosBloqueadoRestantes("juan@mail.com")).thenReturn(125L);

        assertThatThrownBy(() -> servicio.autenticar(new LoginRequest("juan@mail.com", "secreta123!")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Demasiados intentos fallidos")
                .hasMessageContaining("3 minuto"); // 125s redondeados hacia arriba -> 3 min

        verify(usuarioRepository, never()).findByCorreo(any());
    }

    @Test
    void loginConCorreoNoVerificadoSeRechazaAunConCredencialesCorrectas() {
        Usuario usuario = new Usuario("juan@mail.com", "hash-correcto", "Juan", Rol.ESTUDIANTE); // sin marcarVerificado()
        when(limitadorIntentos.segundosBloqueadoRestantes("juan@mail.com")).thenReturn(0L);
        when(usuarioRepository.findByCorreo("juan@mail.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("secreta123!", "hash-correcto")).thenReturn(true);

        assertThatThrownBy(() -> servicio.autenticar(new LoginRequest("juan@mail.com", "secreta123!")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("verificar tu correo");

        // Las credenciales eran correctas: esto NO cuenta como intento fallido de fuerza bruta.
        verify(limitadorIntentos, never()).registrarFallo(any());
        verify(limitadorIntentos, never()).registrarExito(any());
    }
}
