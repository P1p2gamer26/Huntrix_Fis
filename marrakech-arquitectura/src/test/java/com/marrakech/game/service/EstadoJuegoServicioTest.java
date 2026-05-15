package com.marrakech.game.service;

import com.marrakech.game.repository.IEstadoJuegoRepositorio;
import com.marrakech.game.service.EstadoJuegoServicio.EstadoDB;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EstadoJuegoServicioTest {

    private IEstadoJuegoRepositorio estadoRepo;
    private EstadoJuegoServicio svc;

    @BeforeEach
    void setUp() {
        estadoRepo = mock(IEstadoJuegoRepositorio.class);
        svc = new EstadoJuegoServicio(estadoRepo, "MRK-TEST");
    }

    // ── SERIALIZAR: cantidad de jugadores ──────────────────────────────────

    @Test
    void serializar_dosJugadores_formatoCorrecto() {
        String r = EstadoJuegoServicio.serializarEstado(2, new int[]{30,20}, new int[]{15,10},
            new int[7][7], 0, 0, -1, -1, new int[7][7]);
        assertTrue(r.startsWith("30,20;15,10;"));
    }

    @Test
    void serializar_tresJugadores_tresMonedas() {
        String r = EstadoJuegoServicio.serializarEstado(3, new int[]{10,20,30}, new int[]{5,5,5},
            new int[7][7], 1, 0, -1, -1, new int[7][7]);
        assertTrue(r.startsWith("10,20,30;5,5,5;"));
    }

    @Test
    void serializar_cuatroJugadores_cuatroValores() {
        String r = EstadoJuegoServicio.serializarEstado(4, new int[]{30,22,15,8}, new int[]{15,13,7,4},
            new int[7][7], 2, 1, 3, 4, new int[7][7]);
        assertTrue(r.startsWith("30,22,15,8;"));
        assertTrue(r.contains("15,13,7,4;"));
    }

    // ── SERIALIZAR: valores extremos ───────────────────────────────────────

    @Test
    void serializar_dineroCero_seCodifica() {
        String r = EstadoJuegoServicio.serializarEstado(2, new int[]{0,0}, new int[]{15,15},
            new int[7][7], 0, 0, -1, -1, new int[7][7]);
        assertTrue(r.startsWith("0,0;15,15;"));
    }

    @Test
    void serializar_alfombrasCero_seCodifica() {
        String r = EstadoJuegoServicio.serializarEstado(2, new int[]{30,20}, new int[]{0,0},
            new int[7][7], 0, 0, -1, -1, new int[7][7]);
        assertTrue(r.contains(";0,0;"));
    }

    // ── SERIALIZAR: currentPlayerIdx, currentPhase, firstCarpet ────────────

    @Test
    void serializar_currentPlayerIdx_seCodifica() {
        for (int idx = 0; idx < 4; idx++) {
            String r = EstadoJuegoServicio.serializarEstado(4, new int[]{10,10,10,10}, new int[]{5,5,5,5},
                new int[7][7], idx, 0, -1, -1, new int[7][7]);
            String[] parts = r.split(";");
            assertEquals(String.valueOf(idx), parts[3]);
        }
    }

    @Test
    void serializar_currentPhase_seCodifica() {
        for (int ph = 0; ph <= 2; ph++) {
            String r = EstadoJuegoServicio.serializarEstado(2, new int[]{10,10}, new int[]{5,5},
                new int[7][7], 0, ph, -1, -1, new int[7][7]);
            String[] parts = r.split(";");
            assertEquals(String.valueOf(ph), parts[4]);
        }
    }

    @Test
    void serializar_firstCarpetNegativo_seCodifica() {
        String r = EstadoJuegoServicio.serializarEstado(2, new int[]{10,10}, new int[]{5,5},
            new int[7][7], 0, 1, -1, -1, new int[7][7]);
        String[] parts = r.split(";");
        assertEquals("-1", parts[5]);
        assertEquals("-1", parts[6]);
    }

    @Test
    void serializar_firstCarpetConValores_seCodifica() {
        String r = EstadoJuegoServicio.serializarEstado(2, new int[]{10,10}, new int[]{5,5},
            new int[7][7], 0, 1, 3, 4, new int[7][7]);
        String[] parts = r.split(";");
        assertEquals("3", parts[5]);
        assertEquals("4", parts[6]);
    }

    // ── SERIALIZAR: tileOwner (matriz de 7x7) ─────────────────────────────

    @Test
    void serializar_tileOwner_matrizCompleta() {
        int[][] owner = new int[7][7];
        owner[0][0] = 1; owner[6][6] = 2; owner[3][3] = 3;
        String r = EstadoJuegoServicio.serializarEstado(2, new int[]{10,10}, new int[]{5,5},
            owner, 0, 0, -1, -1, new int[7][7]);
        String rows = r.split(";")[2];
        String[] filas = rows.split("/");
        assertEquals(7, filas.length);
        assertEquals("1,0,0,0,0,0,0,", filas[0]);
        filas = rows.split("/");
        assertTrue(filas[6].startsWith("0,0,0,0,0,0,2,"));
    }

    @Test
    void serializar_tileOwner_todoCero() {
        String r = EstadoJuegoServicio.serializarEstado(2, new int[]{10,10}, new int[]{5,5},
            new int[7][7], 0, 0, -1, -1, new int[7][7]);
        String rows = r.split(";")[2];
        String[] filas = rows.split("/");
        assertEquals(7, filas.length);
        for (String f : filas) assertEquals("0,0,0,0,0,0,0,", f);
    }

    // ── SERIALIZAR: carpetOrientation (matriz de 7x7) ──────────────────────

    @Test
    void serializar_carpetOrientation_todoCero() {
        String r = EstadoJuegoServicio.serializarEstado(2, new int[]{10,10}, new int[]{5,5},
            new int[7][7], 0, 0, -1, -1, new int[7][7]);
        String orientPart = r.split(";")[7];
        String[] filas = orientPart.split("/");
        assertEquals(7, filas.length);
        for (String f : filas) assertEquals("0,0,0,0,0,0,0,", f);
    }

    @Test
    void serializar_carpetOrientation_valoresMixtos() {
        int[][] orient = new int[7][7];
        orient[0][0] = 1; orient[1][2] = -1; orient[6][6] = 3;
        String r = EstadoJuegoServicio.serializarEstado(2, new int[]{10,10}, new int[]{5,5},
            new int[7][7], 0, 0, -1, -1, orient);
        String orientPart = r.split(";")[7];
        assertTrue(orientPart.startsWith("1,0,"));
        assertTrue(orientPart.contains("-1,"));
    }

    @Test
    void serializar_carpetOrientation_todosLosValoresPosibles() {
        int[][] orient = new int[7][7];
        for (int v = -1; v <= 3; v++)
            for (int i = 0; i < Math.min(5, 7); i++)
                if (v + 1 + i * 5 < 49) orient[(v + 1 + i * 5) % 7][(v + 1 + i * 5) / 7] = v;
        assertDoesNotThrow(() -> EstadoJuegoServicio.serializarEstado(2,
            new int[]{10,10}, new int[]{5,5}, new int[7][7], 0, 0, -1, -1, orient));
    }

    // ── PARSEAR: casos válidos ─────────────────────────────────────────────

    @Test
    void parsear_formatoValido_completo() {
        String raw = "7|2|5|0|30,20;15,10;0,0,0,0,0,0,0,/0,0,0,0,0,0,0,/" +
            "0,0,0,0,0,0,0,/0,0,0,0,0,0,0,/0,0,0,0,0,0,0,/0,0,0,0,0,0,0,/0,0,0,0,0,0,0,/;" +
            "0;0;-1;-1;0,0,0,0,0,0,0,/0,0,0,0,0,0,0,/0,0,0,0,0,0,0,/0,0,0,0,0,0,0,/" +
            "0,0,0,0,0,0,0,/0,0,0,0,0,0,0,/0,0,0,0,0,0,0,/";
        EstadoDB e = EstadoJuegoServicio.parsearEstado(raw);
        assertNotNull(e);
        assertEquals(7, e.turno);
        assertEquals(2, e.ax);
        assertEquals(5, e.ay);
        assertEquals(0, e.adir);
        assertNotNull(e.tableroJson);
    }

    @Test
    void parsear_turnoCero_seLee() {
        String raw = "0|0|0|1|datos";
        EstadoDB e = EstadoJuegoServicio.parsearEstado(raw);
        assertNotNull(e);
        assertEquals(0, e.turno);
    }

    @Test
    void parsear_turnoGrande_seLee() {
        String raw = "999999|3|4|2|datos";
        EstadoDB e = EstadoJuegoServicio.parsearEstado(raw);
        assertNotNull(e);
        assertEquals(999999, e.turno);
    }

    @Test
    void parsear_axAyAdirValidos_seLee() {
        String raw = "1|6|6|3|data";
        EstadoDB e = EstadoJuegoServicio.parsearEstado(raw);
        assertEquals(6, e.ax);
        assertEquals(6, e.ay);
        assertEquals(3, e.adir);
    }

    // ── PARSEAR: casos inválidos ───────────────────────────────────────────

    @Test
    void parsear_rawNull_retornaNull() {
        assertNull(EstadoJuegoServicio.parsearEstado(null));
    }

    @Test
    void parsear_formatoInvalido_retornaNull() {
        assertNull(EstadoJuegoServicio.parsearEstado("no-es-formato-valido"));
    }

    @Test
    void parsear_stringVacio_retornaNull() {
        assertNull(EstadoJuegoServicio.parsearEstado(""));
    }

    @Test
    void parsear_menosDe5Partes_retornaNull() {
        assertNull(EstadoJuegoServicio.parsearEstado("1|2|3"));
    }

    @Test
    void parsear_partesNoNumericas_retornaNull() {
        assertNull(EstadoJuegoServicio.parsearEstado("a|b|c|d|e"));
    }

    @Test
    void parsear_axNoNumerico_retornaNull() {
        assertNull(EstadoJuegoServicio.parsearEstado("1|x|3|0|data"));
    }

    // ── ROUNDTRIP: serializar → parsear conserva datos ──────────────────────

    @Test
    void roundtrip_dosJugadores_conPrimeraAlfombra() {
        int[][] owner = new int[7][7]; owner[2][3] = 1;
        int[][] orient = new int[7][7]; orient[2][3] = 2;
        String tablero = EstadoJuegoServicio.serializarEstado(2, new int[]{30,25}, new int[]{12,10},
            owner, 1, 0, -1, -1, orient);
        String raw = "5|3|4|1|" + tablero;
        EstadoDB e = EstadoJuegoServicio.parsearEstado(raw);
        assertNotNull(e);
        assertEquals(5, e.turno);
        assertEquals(3, e.ax);
        assertEquals(4, e.ay);
        assertEquals(1, e.adir);
        assertTrue(e.tableroJson.contains("30,25"));
        assertTrue(e.tableroJson.contains("12,10"));
    }

    @Test
    void roundtrip_sinTableroJson_vacio() {
        String raw = "1|0|0|0|";
        EstadoDB e = EstadoJuegoServicio.parsearEstado(raw);
        assertNotNull(e);
        assertEquals("", e.tableroJson);
    }

    @Test
    void roundtrip_cincoPartes_exactas() {
        String raw = "42|1|2|3|contenido";
        EstadoDB e = EstadoJuegoServicio.parsearEstado(raw);
        assertEquals(42, e.turno);
        assertEquals(1, e.ax);
        assertEquals(2, e.ay);
        assertEquals(3, e.adir);
        assertEquals("contenido", e.tableroJson);
    }

    // ── GUARDAR / CARGAR ──────────────────────────────────────────────────

    @Test
    void cargarUltimoEstado_partidaIdNull_retornaNull() {
        EstadoJuegoServicio svcSinBD = new EstadoJuegoServicio(estadoRepo, null);
        assertNull(svcSinBD.cargarUltimoEstado());
    }

    @Test
    void guardarEstado_partidaIdNull_noIncrementaVersion() {
        EstadoJuegoServicio s = new EstadoJuegoServicio(estadoRepo, null);
        s.guardarEstado(0, 0, 0, "test");
        assertEquals(0, s.getEstadoVersion());
    }

    @Test
    void guardarEstadoSincrono_partidaIdNull_noIncrementaVersion() {
        EstadoJuegoServicio s = new EstadoJuegoServicio(estadoRepo, null);
        s.guardarEstadoSincrono(0, 0, 0, "test");
        assertEquals(0, s.getEstadoVersion());
    }

    // ── GETTERS / SETTERS ─────────────────────────────────────────────────

    @Test
    void estadoVersion_iniciaEnCero() {
        assertEquals(0, svc.getEstadoVersion());
    }

    @Test
    void setEstadoVersion_actualizaCorrectamente() {
        svc.setEstadoVersion(7);
        assertEquals(7, svc.getEstadoVersion());
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

    // ── POLLING ────────────────────────────────────────────────────────────

    @Test
    void detenerPolling_sinPollingActivo_noLanzaExcepcion() {
        assertDoesNotThrow(() -> svc.detenerPolling());
    }

    @Test
    void detenerPolling_dosVeces_noLanzaExcepcion() {
        svc.detenerPolling();
        assertDoesNotThrow(() -> svc.detenerPolling());
    }
}
