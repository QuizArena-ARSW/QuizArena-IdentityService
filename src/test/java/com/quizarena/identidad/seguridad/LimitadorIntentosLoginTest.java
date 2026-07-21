package com.quizarena.identidad.seguridad;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cubre la mitigacion de fuerza bruta en el login (escenario de calidad de
 * seguridad): un correo debe bloquearse tras varios fallos seguidos, pero
 * nunca antes de tiempo ni de forma permanente.
 */
class LimitadorIntentosLoginTest {

    @Test
    void noBloqueaMientrasNoSeSuperenLosIntentosPermitidos() {
        LimitadorIntentosLogin limitador = new LimitadorIntentosLogin(3, 10, 5);

        limitador.registrarFallo("juan@mail.com");
        limitador.registrarFallo("juan@mail.com");

        assertThat(limitador.segundosBloqueadoRestantes("juan@mail.com")).isZero();
    }

    @Test
    void bloqueaElCorreoAlAlcanzarElMaximoDeIntentosFallidos() {
        LimitadorIntentosLogin limitador = new LimitadorIntentosLogin(3, 10, 5);

        limitador.registrarFallo("juan@mail.com");
        limitador.registrarFallo("juan@mail.com");
        limitador.registrarFallo("juan@mail.com");

        assertThat(limitador.segundosBloqueadoRestantes("juan@mail.com")).isGreaterThan(0);
    }

    @Test
    void elBloqueoNoAfectaAOtrosCorreos() {
        LimitadorIntentosLogin limitador = new LimitadorIntentosLogin(3, 10, 5);

        limitador.registrarFallo("victima@mail.com");
        limitador.registrarFallo("victima@mail.com");
        limitador.registrarFallo("victima@mail.com");

        assertThat(limitador.segundosBloqueadoRestantes("otro@mail.com")).isZero();
    }

    @Test
    void unLoginExitosoLimpiaLosIntentosFallidosPrevios() {
        LimitadorIntentosLogin limitador = new LimitadorIntentosLogin(3, 10, 5);

        limitador.registrarFallo("juan@mail.com");
        limitador.registrarFallo("juan@mail.com");
        limitador.registrarExito("juan@mail.com");
        limitador.registrarFallo("juan@mail.com");

        // Solo lleva 1 fallo desde el exito (de 3 permitidos): no deberia bloquear.
        assertThat(limitador.segundosBloqueadoRestantes("juan@mail.com")).isZero();
    }

    @Test
    void elCorreoSeNormalizaSinImportarMayusculasNiEspacios() {
        LimitadorIntentosLogin limitador = new LimitadorIntentosLogin(3, 10, 5);

        limitador.registrarFallo("Juan@Mail.com");
        limitador.registrarFallo(" juan@mail.com ");
        limitador.registrarFallo("JUAN@MAIL.COM");

        assertThat(limitador.segundosBloqueadoRestantes("juan@mail.com")).isGreaterThan(0);
    }
}
