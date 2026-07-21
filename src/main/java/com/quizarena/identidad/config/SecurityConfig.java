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
 * Configuracion de seguridad.
 *
 * IMPORTANTE: el ORDEN de las reglas importa (gana la primera que coincide).
 * Por eso /api/bancos/mios se declara ANTES que la regla general de
 * /api/bancos/**, para que exija token aunque la otra sea publica.
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
                // CSRF protege sesiones basadas en cookies, donde el navegador adjunta
                // credenciales automaticamente en cualquier peticion cross-site. Esta
                // API es STATELESS y se autentica con "Authorization: Bearer <jwt>"
                // (nunca con cookies), cabecera que un sitio malicioso no puede forzar
                // a adjuntar: por eso CSRF no aplica aqui y desactivarlo es lo correcto,
                // no un descuido (SonarCloud java:S4502 marca esto para revision manual).
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Publicos: registro y login
                        .requestMatchers("/api/auth/**").permitAll()

                        // "Mis bancos" SIEMPRE requiere token. Debe ir ANTES
                        // que la regla general de /api/bancos/**.
                        .requestMatchers(HttpMethod.GET, "/api/bancos/mios").authenticated()

                        // Comunicacion interna del Servicio de Juego (dev):
                        // lee un banco para armar la partida y guarda resultados.
                        .requestMatchers(HttpMethod.GET, "/api/bancos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/historial").permitAll()

                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
