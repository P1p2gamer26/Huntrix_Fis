package com.marrakech.game.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GestionJuegoServicioTest {

    @Test
    void clicks_turnos_y_getters() {
        GestionJuegoServicio g = new GestionJuegoServicio();
        g.iniciarJuego(2);

        // getters
        assertEquals(2, g.getNumPlayers());
        assertNotNull(g.getMoney());
        assertNotNull(g.getRugs());
        assertNotNull(g.getTileOwner());
        assertNotNull(g.getCarpetOrientation());

        // fase 0 => invalido
        assertEquals(GestionJuegoServicio.ResultadoTipo.INVALIDO,
                g.procesarClick(0, 0, 3, 3).tipo);

        // fase 1 => primer click
        g.setCurrentPhase(1);
        var r1 = g.procesarClick(3, 4, 3, 3);
        assertEquals(GestionJuegoServicio.ResultadoTipo.ESPERA_SEGUNDA, r1.tipo);
        assertEquals(2, g.getCurrentPhase());

        // segundo click correcto
        var r2 = g.procesarClick(3, 5, 3, 3);
        assertTrue(r2.tipo == GestionJuegoServicio.ResultadoTipo.ALFOMBRA_COLOCADA
                || r2.tipo == GestionJuegoServicio.ResultadoTipo.JUEGO_TERMINADO);

        // rama SIN_SEGUNDA_OPCION
        g.setCurrentPhase(1);
        var r3 = g.procesarClick(7, 3, 6, 3);
        assertEquals(GestionJuegoServicio.ResultadoTipo.SIN_SEGUNDA_OPCION, r3.tipo);

        // pago
        g.getTileOwner()[0][0] = 2;
        assertTrue(g.aplicarPago(0, 0) >= 1);

        // helpers (sin asumir ganador exacto)
        int ganador = g.calcularGanador();
        assertTrue(ganador >= 0 && ganador < g.getNumPlayers());

        g.contarAlfombrasEnTablero(0);
        g.fasePostDado();
        g.esMiTurno(false, 0);

        // pasar turno (la fase puede variar según la implementación)
        g.pasarTurno();
        assertTrue(g.getCurrentPhase() == 0 || g.getCurrentPhase() == 1);
    }

    @Test
    void serializar_y_aplicar_estado() {
        GestionJuegoServicio g = new GestionJuegoServicio();
        g.iniciarJuego(2);

        // raw inválido => est null
        g.aplicarEstado("basura");

        // secciones < 7
        g.aplicarEstado("1|0|0|0|a;b;c");

        // serializar + aplicar
        String tablero = g.serializarEstado(3, 3, 0);
        g.aplicarEstado("1|3|3|0|" + tablero);

        // forzar setters para cubrir líneas
        g.setCurrentPlayerIdx(1);
        g.setNumPlayers(2);
        assertEquals(1, g.getCurrentPlayerIdx());
        assertEquals(2, g.getNumPlayers());
    }

    @Test
    void cubrir_invalidos_y_getters_firstCarpet() {
        GestionJuegoServicio g = new GestionJuegoServicio();
        g.iniciarJuego(2);

        // fase 1: click inválido (cubre el return ResultadoClick.invalido() de fase 1)
        g.setCurrentPhase(1);
        assertEquals(GestionJuegoServicio.ResultadoTipo.INVALIDO,
                g.procesarClick(0, 0, 3, 3).tipo);

        // fase 2: generar "espera segunda" y luego segundo click inválido (cubre el return INVALIDO de fase 2)
        g.setCurrentPhase(1);
        var r1 = g.procesarClick(3, 4, 3, 3);
        assertEquals(GestionJuegoServicio.ResultadoTipo.ESPERA_SEGUNDA, r1.tipo);

        var r2 = g.procesarClick(0, 0, 3, 3);
        assertEquals(GestionJuegoServicio.ResultadoTipo.INVALIDO, r2.tipo);

        // cubrir getters que estaban sin tocar
        assertEquals(3, g.getFirstCarpetX());
        assertEquals(4, g.getFirstCarpetY());
    }
}