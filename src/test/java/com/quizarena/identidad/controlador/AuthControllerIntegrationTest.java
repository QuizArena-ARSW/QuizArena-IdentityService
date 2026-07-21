package com.quizarena.identidad.controlador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizarena.identidad.modelo.Usuario;
import com.quizarena.identidad.repositorio.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integracion de extremo a extremo sobre el contexto REAL de
 * Spring (controlador + seguridad + validacion + JPA), con H2 en memoria en
 * vez de Postgres. A diferencia de ServicioAutenticacionTest (unitaria, con
 * mocks), aqui se verifica que TODO el cableado funciona junto: el filtro
 * de seguridad, el manejador de errores de @Valid, el hash real de BCrypt y
 * la persistencia real via Hibernate.
 *
 * El envio de correo se reemplaza por un mock (@MockBean) para no depender
 * de credenciales SMTP reales durante las pruebas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsuarioRepository usuarioRepository;

    @MockBean JavaMailSender mailSender;

    @Test
    void flujoCompletoRegistroVerificacionYLogin() throws Exception {
        String correo = "juan@mail.com";

        // 1. Registro: contrasena valida (con caracter especial)
        mockMvc.perform(post("/api/auth/registro")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "correo", correo, "contrasena", "secreta123!", "nombre", "Juan"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value(correo));

        // 2. Login ANTES de verificar: debe rechazar
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "correo", correo, "contrasena", "secreta123!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("verificar")));

        // 3. Leer el codigo real generado (no se expone por la API, a proposito)
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElseThrow();
        String codigo = obtenerCodigo(usuario);

        // 4. Verificar con el codigo correcto
        mockMvc.perform(post("/api/auth/verificar")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "correo", correo, "codigo", codigo))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        // 5. Login con credenciales correctas: ahora si funciona
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "correo", correo, "contrasena", "secreta123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void registroConCorreoDuplicadoDaError() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(Map.of(
                "correo", "duplicado@mail.com", "contrasena", "secreta123!", "nombre", "Juan"));

        mockMvc.perform(post("/api/auth/registro").contentType("application/json").content(cuerpo))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/registro").contentType("application/json").content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El correo ya esta en uso"));
    }

    @Test
    void registroConContrasenaSinCaracterEspecialEsRechazadoPorValidacion() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "correo", "otro@mail.com", "contrasena", "abcdef1", "nombre", "Juan"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("caracter especial")));

        assertThat(usuarioRepository.existsByCorreo("otro@mail.com")).isFalse();
    }

    @Test
    void loginConCredencialesIncorrectasRepetidasBloqueaElCorreo() throws Exception {
        String correo = "bruteforce@mail.com";
        mockMvc.perform(post("/api/auth/registro")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "correo", correo, "contrasena", "secreta123!", "nombre", "Juan"))))
                .andExpect(status().isOk());

        // application-test.properties configura el limite en 3 intentos
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "correo", correo, "contrasena", "clave-mala"))))
                    .andExpect(status().isUnauthorized());
        }

        // El intento numero 4, aunque tenga la clave CORRECTA, debe seguir bloqueado
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "correo", correo, "contrasena", "secreta123!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("Demasiados intentos")));
    }

    private String obtenerCodigo(Usuario usuario) throws Exception {
        var campo = Usuario.class.getDeclaredField("codigoVerificacion");
        campo.setAccessible(true);
        return (String) campo.get(usuario);
    }
}
