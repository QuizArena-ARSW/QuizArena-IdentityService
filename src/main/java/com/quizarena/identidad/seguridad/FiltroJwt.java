package com.quizarena.identidad.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que se ejecuta en CADA peticion: si trae un token JWT valido en la
 * cabecera Authorization, autentica al usuario para esa peticion.
 */
@Component
public class FiltroJwt extends OncePerRequestFilter {

    private final ProveedorJwt proveedorJwt;

    public FiltroJwt(ProveedorJwt proveedorJwt) {
        this.proveedorJwt = proveedorJwt;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String cabecera = request.getHeader("Authorization");

        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            String token = cabecera.substring(7);
            if (proveedorJwt.esValido(token)) {
                String idUsuario = proveedorJwt.extraerIdUsuario(token);
                var auth = new UsernamePasswordAuthenticationToken(
                        idUsuario, null, List.of(new SimpleGrantedAuthority("ROLE_USUARIO")));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
