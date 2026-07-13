package com.quizarena.identidad.seguridad;

import com.quizarena.identidad.modelo.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Genera y valida los tokens JWT.
 *
 * Un JWT es una credencial firmada: cuando un usuario hace login, le damos un
 * token; en cada peticion posterior, el cliente lo envia y aqui verificamos
 * que sea valido y de quien dice ser, sin tener que consultar la base de datos.
 */
@Component
public class ProveedorJwt {

    private final SecretKey clave;
    private final long expiracionMs;

    public ProveedorJwt(
            @Value("${quizarena.jwt.secret}") String secret,
            @Value("${quizarena.jwt.expiracion-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    /** Genera un token para un usuario autenticado. */
    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionMs);
        return Jwts.builder()
                .subject(usuario.getId().toString())   // el "dueño" del token
                .claim("correo", usuario.getCorreo())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(clave)
                .compact();
    }

    /** Extrae el id del usuario desde un token. */
    public String extraerIdUsuario(String token) {
        return parse(token).getSubject();
    }

    /** Devuelve true si el token es valido (firma correcta y no expirado). */
    public boolean esValido(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
