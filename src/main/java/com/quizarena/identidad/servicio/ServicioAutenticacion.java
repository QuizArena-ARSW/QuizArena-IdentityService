package com.quizarena.identidad.servicio;

import com.quizarena.identidad.dto.LoginRequest;
import com.quizarena.identidad.dto.ReenviarCodigoRequest;
import com.quizarena.identidad.dto.RegistroRequest;
import com.quizarena.identidad.dto.RegistroResponse;
import com.quizarena.identidad.dto.TokenResponse;
import com.quizarena.identidad.dto.VerificarCorreoRequest;
import com.quizarena.identidad.modelo.Rol;
import com.quizarena.identidad.modelo.Usuario;
import com.quizarena.identidad.repositorio.UsuarioRepository;
import com.quizarena.identidad.seguridad.LimitadorIntentosLogin;
import com.quizarena.identidad.seguridad.ProveedorJwt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/** Logica de registro, verificacion de correo y autenticacion de usuarios. */
@Service
public class ServicioAutenticacion {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProveedorJwt proveedorJwt;
    private final ServicioCorreo servicioCorreo;
    private final LimitadorIntentosLogin limitadorIntentos;
    private final long expiracionCodigoMinutos;

    public ServicioAutenticacion(UsuarioRepository usuarioRepository,
                                 PasswordEncoder passwordEncoder,
                                 ProveedorJwt proveedorJwt,
                                 ServicioCorreo servicioCorreo,
                                 LimitadorIntentosLogin limitadorIntentos,
                                 @Value("${quizarena.verificacion.expiracion-minutos}") long expiracionCodigoMinutos) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.proveedorJwt = proveedorJwt;
        this.servicioCorreo = servicioCorreo;
        this.limitadorIntentos = limitadorIntentos;
        this.expiracionCodigoMinutos = expiracionCodigoMinutos;
    }

    /** Registra un usuario nuevo (sin verificar) y le envia un codigo por correo. */
    public RegistroResponse registrar(RegistroRequest req) {
        if (usuarioRepository.existsByCorreo(req.correo())) {
            throw new IllegalArgumentException("El correo ya esta en uso");
        }
        // NUNCA se guarda la contrasena en texto plano: se guarda su hash
        String hash = passwordEncoder.encode(req.contrasena());
        Usuario usuario = new Usuario(req.correo(), hash, req.nombre(), Rol.ESTUDIANTE);
        asignarYEnviarCodigo(usuario);
        usuarioRepository.save(usuario);

        return new RegistroResponse(usuario.getCorreo(), "Te enviamos un código de verificación a tu correo.");
    }

    /** Verifica credenciales y devuelve un token si son correctas y el correo esta verificado. */
    public TokenResponse autenticar(LoginRequest req) {
        long bloqueoSeg = limitadorIntentos.segundosBloqueadoRestantes(req.correo());
        if (bloqueoSeg > 0) {
            long minutos = (bloqueoSeg + 59) / 60;
            throw new IllegalArgumentException(
                    "Demasiados intentos fallidos. Intenta de nuevo en " + minutos + " minuto(s).");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(req.correo());
        boolean credencialesValidas = usuarioOpt.isPresent()
                && passwordEncoder.matches(req.contrasena(), usuarioOpt.get().getHashContrasena());
        if (!credencialesValidas) {
            limitadorIntentos.registrarFallo(req.correo());
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        Usuario usuario = usuarioOpt.get();
        if (!usuario.isVerificado()) {
            throw new IllegalArgumentException("Debes verificar tu correo antes de iniciar sesión");
        }

        limitadorIntentos.registrarExito(req.correo());
        String token = proveedorJwt.generarToken(usuario);
        return new TokenResponse(token, usuario.getNombre(), usuario.getCorreo());
    }

    /** Confirma el codigo de verificacion y, si es valido, entrega un token de una vez. */
    public TokenResponse verificarCorreo(VerificarCorreoRequest req) {
        Usuario usuario = usuarioRepository.findByCorreo(req.correo())
                .orElseThrow(() -> new IllegalArgumentException("Código incorrecto o vencido"));

        if (usuario.isVerificado()) {
            throw new IllegalArgumentException("Este correo ya está verificado");
        }
        if (!usuario.codigoValido(req.codigo())) {
            throw new IllegalArgumentException("Código incorrecto o vencido");
        }

        usuario.marcarVerificado();
        usuarioRepository.save(usuario);

        String token = proveedorJwt.generarToken(usuario);
        return new TokenResponse(token, usuario.getNombre(), usuario.getCorreo());
    }

    /** Genera y reenvia un nuevo codigo de verificacion a un usuario aun no verificado. */
    public void reenviarCodigo(ReenviarCodigoRequest req) {
        Usuario usuario = usuarioRepository.findByCorreo(req.correo())
                .orElseThrow(() -> new IllegalArgumentException("No existe una cuenta con ese correo"));

        if (usuario.isVerificado()) {
            throw new IllegalArgumentException("Este correo ya está verificado");
        }

        asignarYEnviarCodigo(usuario);
        usuarioRepository.save(usuario);
    }

    private void asignarYEnviarCodigo(Usuario usuario) {
        String codigo = generarCodigo();
        usuario.asignarCodigoVerificacion(codigo, LocalDateTime.now().plusMinutes(expiracionCodigoMinutos));
        servicioCorreo.enviarCodigoVerificacion(usuario.getCorreo(), usuario.getNombre(), codigo);
    }

    private String generarCodigo() {
        return String.format("%06d", ALEATORIO.nextInt(1_000_000));
    }
}
