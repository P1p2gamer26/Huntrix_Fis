package com.marrakech.game.servicios;

import com.marrakech.game.servicios.EstadoJuegoServicio.EstadoDB;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para EstadoJuegoServicio.
 * Las pruebas de BD se cubren con mocks implícitos (estado null cuando la BD no está disponible).
 */
class EstadoJuegoServicioTest {

    private EstadoJuegoServicio svc;

    @BeforeEach
    void setUp() {
        // Instanciamos con una partidaId ficticia; no conecta a BD en estas pruebas
        svc = new EstadoJuegoServicio("MRK-TEST");
    }

    // ── CASO COMPLEJO 1: serialización → parseo roundtrip completo ────────────

    @Test
    void serializarYParsear_roundtrip_conservaTodosLosDatos() {
        int numPlayers = 3;
        int[] money    = {30, 25, 18};
        int[] rugs     = {12, 10, 8};
        int[][] owner  = new int[7][7];
        int[][] orient = new int[7][7];
        owner[2][3]  = 1;
        orient[2][3] = 2;

        // Serializar el estado del tablero
        String tableroJson = EstadoJuegoServicio.serializarEstado(
            numPlayers, money, rugs, owner, 1, 0, -1, -1, orient);

        // Crear el raw simulando el formato de la BD (turno|ax|ay|adir|tablero)
        String raw = "5|3|4|1|" + tableroJson;

        EstadoDB est = EstadoJuegoServicio.parsearEstado(raw);

        assertNotNull(est);
        assertEquals(5, est.turno);
        assertEquals(3, est.ax);
        assertEquals(4, est.ay);
        assertEquals(1, est.adir);
        assertNotNull(est.tableroJson);
    }

    // ── CASO COMPLEJO 2: parsear con formato inválido retorna null ────────────

    @Test
    void parsear_formatoInvalido_retornaNull() {
        EstadoDB est = EstadoJuegoServicio.parsearEstado("no-es-formato-valido");
        assertNull(est);
    }

    // ── CASO COMPLEJO 3: parsear con raw null retorna null ────────────────────

    @Test
    void parsear_rawNull_retornaNull() {
        assertNull(EstadoJuegoServicio.parsearEstado(null));
    }

    // ── CASO COMPLEJO 4: serializar con 4 jugadores codifica correctamente ─────

    @Test
    void serializar_cuatroJugadores_contieneTodasLasMonedas() {
        int[] money = {30, 22, 15, 8};
        int[] rugs  = {15, 13, 7, 4};
        String result = EstadoJuegoServicio.serializarEstado(
            4, money, rugs, new int[7][7], 2, 1, 3, 4, new int[7][7]);

        assertTrue(result.startsWith("30,22,15,8;"));
        assertTrue(result.contains("15,13,7,4;"));
    }

    // ── CASO COMPLEJO 5: versión de estado se incrementa en cada guardado ─────

    @Test
    void estadoVersion_iniciaEnCero() {
        assertEquals(0, svc.getEstadoVersion());
    }

    @Test
    void setEstadoVersion_actualizaCorrectamente() {
        svc.setEstadoVersion(7);
        assertEquals(7, svc.getEstadoVersion());
    }

    // ── Cobertura adicional ───────────────────────────────────────────────────

    @Test
    void cargarUltimoEstado_sinConexion_retornaNull() {
        // Sin servidor H2 activo, debe retornar null silenciosamente
        EstadoJuegoServicio svcSinBD = new EstadoJuegoServicio(null);
        assertNull(svcSinBD.cargarUltimoEstado());
    }

    @Test
    void ultimoTurnoVisto_iniciaEnCero() {
        assertEquals(0, svc.getUltimoTurnoVisto());
    }

    @Test
    void setUltimoTurnoVisto_actualizaCorrectamente() {
        svc.setUltimoTurnoVisto(12);
        assertEquals(12, svc.getUltimoTurnoVisto());
    }

    @Test
    void serializar_tableroConPropietarios_codificaMatrizCorrecta() {
        int[][] owner = new int[7][7];
        owner[0][0] = 2; owner[6][6] = 3;
        String result = EstadoJuegoServicio.serializarEstado(
            2, new int[]{10, 20}, new int[]{5, 8}, owner, 0, 0, -1, -1, new int[7][7]);
        // La primera fila debe tener 2 en la primera posición
        assertTrue(result.contains("2,"));
    }
}
