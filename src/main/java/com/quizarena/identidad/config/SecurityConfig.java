package com.quizarena.identidad.config;

import com.quizarena.identidad.seguridad.FiltroJwt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Configuracion de seguridad:
 *  - Los endpoints de /api/auth/** son publicos (registro y login).
 *  - El registro de resultados (POST /api/historial) queda abierto para la
 *    comunicacion interna del Servicio de Juego en desarrollo.
 *  - Todo lo demas requiere un token JWT valido.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FiltroJwt filtroJwt;

    public SecurityConfig(FiltroJwt filtroJwt) {
        this.filtroJwt = filtroJwt;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Al ser una API REST sin sesiones, se desactiva CSRF
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        // Endpoint interno que llama el Servicio de Juego (dev):
                        .requestMatchers(HttpMethod.POST, "/api/historial").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bancos/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Sin sesiones: cada peticion se valida por su token
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Nuestro filtro JWT se ejecuta antes del filtro estandar
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** BCrypt: algoritmo para hashear contrasenas de forma segura. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
