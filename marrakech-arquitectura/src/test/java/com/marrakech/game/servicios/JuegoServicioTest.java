package com.marrakech.game.servicios;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class JuegoServicioTest {

    @Test
    void juegoTerminado_conAlfombrasRestantes_retornaFalse() {
        int[] rugs = {5, 3, 0};
        assertFalse(JuegoServicio.juegoTerminado(rugs));
    }

    @Test
    void juegoTerminado_sinAlfombras_retornaTrue() {
        int[] rugs = {0, 0, 0};
        assertTrue(JuegoServicio.juegoTerminado(rugs));
    }

    @Test
    void calcularGanador_mayorDinero_retornaIndiceCorrecto() {
        int[] money = {10, 25, 15};
        int[][] tileOwner = new int[7][7];
        int win = JuegoServicio.calcularGanador(3, money, tileOwner);
        assertEquals(1, win);
    }

    @Test
    void calcularGanador_empateDinero_ganaPorMasAlfombras() {
        int[] money = {20, 20, 15};
        int[][] tileOwner = new int[7][7];
        tileOwner[0][0] = 1; tileOwner[1][0] = 1; tileOwner[2][0] = 1;
        tileOwner[0][1] = 2; tileOwner[1][1] = 2;
        int win = JuegoServicio.calcularGanador(3, money, tileOwner);
        assertEquals(0, win);
    }

    @Test
    void calcularPago_casillaVacia_retornaCero() {
        int[][] tileOwner = new int[7][7];
        assertEquals(0, JuegoServicio.calcularPago(3, 3, 0, tileOwner));
    }

    @Test
    void calcularPago_alfombraPropia_retornaCero() {
        int[][] tileOwner = new int[7][7];
        tileOwner[3][3] = 1;
        assertEquals(0, JuegoServicio.calcularPago(3, 3, 0, tileOwner));
    }

    @Test
    void esMiTurno_modoLocal_retornaTrue() {
        assertTrue(JuegoServicio.esMiTurno(false, 2, 0));
    }

    @Test
    void esMiTurno_multijugadorCoincide_retornaTrue() {
        assertTrue(JuegoServicio.esMiTurno(true, 1, 1));
    }

    @Test
    void esMiTurno_multijugadorNoCoincide_retornaFalse() {
        assertFalse(JuegoServicio.esMiTurno(true, 0, 1));
    }

    @Test
    void siguienteTurno_avanzaAlSiguiente() {
        assertEquals(1, JuegoServicio.siguienteTurno(0, 4));
    }

    @Test
    void siguienteTurno_vuelveAlInicio() {
        assertEquals(0, JuegoServicio.siguienteTurno(3, 4));
    }

    @Test
    void esAdyacenteA_mismaCasilla_retornaFalse() {
        assertFalse(JuegoServicio.esAdyacenteA(3, 3, 3, 3));
    }

    @Test
    void esAdyacenteA_casillaVecina_retornaTrue() {
        assertTrue(JuegoServicio.esAdyacenteA(3, 4, 3, 3));
        assertTrue(JuegoServicio.esAdyacenteA(2, 3, 3, 3));
    }

    @Test
    void esAdyacenteA_casillaLejana_retornaFalse() {
        assertFalse(JuegoServicio.esAdyacenteA(5, 5, 3, 3));
    }

    @Test
    void esAdyacenteA_diagonal_retornaFalse() {
        assertFalse(JuegoServicio.esAdyacenteA(4, 4, 3, 3));
    }

    @Test
    void esAlfombraAdyacente_horizontal_retornaTrue() {
        assertTrue(JuegoServicio.esAlfombraAdyacente(2, 3, 3, 3));
    }

    @Test
    void esAlfombraAdyacente_vertical_retornaTrue() {
        assertTrue(JuegoServicio.esAlfombraAdyacente(3, 2, 3, 3));
    }

    @Test
    void esAlfombraAdyacente_diagonal_retornaFalse() {
        assertFalse(JuegoServicio.esAlfombraAdyacente(2, 2, 3, 3));
    }

    @Test
    void noEsAssam_mismaCasilla_retornaFalse() {
        assertFalse(JuegoServicio.noEsAssam(3, 3, 3, 3));
    }

    @Test
    void noEsAssam_distintaCasilla_retornaTrue() {
        assertTrue(JuegoServicio.noEsAssam(2, 3, 3, 3));
    }

    @Test
    void aplicarPago_sinDueno_noCambiaDinero() {
        int[][] tileOwner = new int[7][7];
        int[] money = {30, 30};
        int pago = JuegoServicio.aplicarPago(3, 3, 0, tileOwner, money);
        assertEquals(0, pago);
        assertArrayEquals(new int[]{30, 30}, money);
    }

    @Test
    void aplicarPago_conDueno_transfiereDinero() {
        int[][] tileOwner = new int[7][7];
        tileOwner[3][3] = 2;
        int[] money = {30, 30};
        int pago = JuegoServicio.aplicarPago(3, 3, 0, tileOwner, money);
        assertTrue(pago > 0);
        assertEquals(30 - pago, money[0]);
        assertEquals(30 + pago, money[1]);
    }

    @Test
    void contarAlfombrasEnTablero_sinAlfombras_retornaCero() {
        int[][] tileOwner = new int[7][7];
        assertEquals(0, JuegoServicio.contarAlfombrasEnTablero(0, tileOwner));
    }

    @Test
    void contarAlfombrasEnTablero_conAlfombras_retornaConteo() {
        int[][] tileOwner = new int[7][7];
        tileOwner[0][0] = 1; tileOwner[1][0] = 1; tileOwner[2][0] = 1;
        assertEquals(3, JuegoServicio.contarAlfombrasEnTablero(0, tileOwner));
    }

    @Test
    void fasePostDado_conAlfombras_retornaFase1() {
        assertEquals(1, JuegoServicio.fasePostDado(5));
    }

    @Test
    void fasePostDado_sinAlfombras_retornaFase0() {
        assertEquals(0, JuegoServicio.fasePostDado(0));
    }
}
