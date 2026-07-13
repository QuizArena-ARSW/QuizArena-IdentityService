package com.quizarena.identidad.servicio;

import com.quizarena.identidad.dto.LoginRequest;
import com.quizarena.identidad.dto.RegistroRequest;
import com.quizarena.identidad.dto.TokenResponse;
import com.quizarena.identidad.modelo.Rol;
import com.quizarena.identidad.modelo.Usuario;
import com.quizarena.identidad.repositorio.UsuarioRepository;
import com.quizarena.identidad.seguridad.ProveedorJwt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Logica de registro y autenticacion de usuarios. */
@Service
public class ServicioAutenticacion {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProveedorJwt proveedorJwt;

    public ServicioAutenticacion(UsuarioRepository usuarioRepository,
                                 PasswordEncoder passwordEncoder,
                                 ProveedorJwt proveedorJwt) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.proveedorJwt = proveedorJwt;
    }

    /** Registra un usuario nuevo y devuelve su token. */
    public TokenResponse registrar(RegistroRequest req) {
        if (usuarioRepository.existsByCorreo(req.correo())) {
            throw new IllegalArgumentException("El correo ya esta en uso");
        }
        // NUNCA se guarda la contrasena en texto plano: se guarda su hash
        String hash = passwordEncoder.encode(req.contrasena());
        Usuario usuario = new Usuario(req.correo(), hash, req.nombre(), Rol.ESTUDIANTE);
        usuarioRepository.save(usuario);

        String token = proveedorJwt.generarToken(usuario);
        return new TokenResponse(token, usuario.getNombre(), usuario.getCorreo());
    }

    /** Verifica credenciales y devuelve un token si son correctas. */
    public TokenResponse autenticar(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByCorreo(req.correo())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(req.contrasena(), usuario.getHashContrasena())) {
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        String token = proveedorJwt.generarToken(usuario);
        return new TokenResponse(token, usuario.getNombre(), usuario.getCorreo());
    }
}
