package com.marrakech.game.service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class AssamNavigatorTest {

    @Test
    void computePath_ceroPasos_noSeMueve() {
        int[][] path = AssamNavigator.computePath(0, 3, 3, 1);
        assertEquals(1, path.length);
        assertEquals(3, path[0][0]);
        assertEquals(3, path[0][1]);
        assertEquals(1, path[0][2]);
    }

    @Test
    void computePath_unPasoNorte_subeUnaFila() {
        int[][] path = AssamNavigator.computePath(1, 3, 3, 0);
        assertEquals(3, path[1][0]);
        assertEquals(2, path[1][1]);
    }

    @Test
    void computePath_unPasoEste_avanzaUnaColumna() {
        int[][] path = AssamNavigator.computePath(1, 3, 3, 1);
        assertEquals(4, path[1][0]);
        assertEquals(3, path[1][1]);
    }

    @Test
    void computePath_unPasoSur_bajaUnaFila() {
        int[][] path = AssamNavigator.computePath(1, 3, 3, 2);
        assertEquals(3, path[1][0]);
        assertEquals(4, path[1][1]);
    }

    @Test
    void computePath_unPasoOeste_retrocedeUnaColumna() {
        int[][] path = AssamNavigator.computePath(1, 3, 3, 3);
        assertEquals(2, path[1][0]);
        assertEquals(3, path[1][1]);
    }

    @Test
    void computePath_rebotaEnBordeNorte_redirigeAOeste() {
        int[][] path = AssamNavigator.computePath(1, 0, 0, 0);
        int[] pos = AssamNavigator.borderPos(1);
        assertEquals(pos[0], path[1][0]);
        assertEquals(pos[1], path[1][1]);
    }

    @Test
    void computePath_rebotaEnBordeEste_redirigeASur() {
        int[][] path = AssamNavigator.computePath(1, 6, 5, 1);
        int bi = AssamNavigator.borderIndex(6, 5);
        int[] pos = AssamNavigator.borderPos(bi + 1);
        assertEquals(pos[0], path[1][0]);
        assertEquals(pos[1], path[1][1]);
    }

    @Test
    void computePath_rebotaEnBordeSur_redirigeAIndiceSiguiente() {
        int[][] path = AssamNavigator.computePath(1, 1, 6, 2);
        int bi = AssamNavigator.borderIndex(1, 6);
        int[] pos = AssamNavigator.borderPos(bi + 1);
        assertEquals(pos[0], path[1][0]);
        assertEquals(pos[1], path[1][1]);
    }

    @Test
    void computePath_rebotaEnBordeOeste_redirigeAIndiceSiguiente() {
        int[][] path = AssamNavigator.computePath(1, 0, 1, 3);
        int bi = AssamNavigator.borderIndex(0, 1);
        int[] pos = AssamNavigator.borderPos(bi + 1);
        assertEquals(pos[0], path[1][0]);
        assertEquals(pos[1], path[1][1]);
    }

    @Test
    void computePath_multiplesPasos_recorreCircuito() {
        int[][] path = AssamNavigator.computePath(4, 0, 0, 1);
        assertEquals(5, path.length);
        assertEquals(0, path[0][0]); assertEquals(0, path[0][1]);
        assertEquals(1, path[1][0]); assertEquals(0, path[1][1]);
        assertEquals(2, path[2][0]); assertEquals(0, path[2][1]);
        assertEquals(3, path[3][0]); assertEquals(0, path[3][1]);
        assertEquals(4, path[4][0]); assertEquals(0, path[4][1]);
    }

    @Test
    void computePath_vueltaCompleta_terminaEnMismoPunto() {
        int[][] path = AssamNavigator.computePath(24, 0, 0, 1);
        assertEquals(0, path[24][0]);
        assertEquals(0, path[24][1]);
    }

    @Test
    void computePath_direccionFinalEsquina_corregida() {
        int[][] path = AssamNavigator.computePath(5, 0, 0, 1);
        assertEquals(5, path[5][0]);
        assertEquals(0, path[5][1]);
        assertEquals(1, path[5][2]);
    }

    @Test
    void borderIndex_interior_retornaMenosUno() {
        assertEquals(-1, AssamNavigator.borderIndex(3, 3));
    }

    @Test
    void borderIndex_bordeNorte_retornaX() {
        assertEquals(3, AssamNavigator.borderIndex(3, 0));
    }

    @Test
    void borderIndex_bordeEste_retorna6MasY() {
        assertEquals(8, AssamNavigator.borderIndex(6, 2));
    }

    @Test
    void borderIndex_bordeSur_retorna12Mas6MenosX() {
        assertEquals(14, AssamNavigator.borderIndex(4, 6));
    }

    @Test
    void borderIndex_bordeOeste_retorna18Mas6MenosY() {
        assertEquals(20, AssamNavigator.borderIndex(0, 4));
    }

    @Test
    void borderIndex_esquina0_0_retorna0() {
        assertEquals(0, AssamNavigator.borderIndex(0, 0));
    }

    @Test
    void borderIndex_esquina6_6_retorna12() {
        assertEquals(12, AssamNavigator.borderIndex(6, 6));
    }

    @Test
    void borderPos_indice0_es0_0() {
        assertArrayEquals(new int[]{0, 0}, AssamNavigator.borderPos(0));
    }

    @Test
    void borderPos_indice5_es5_0() {
        assertArrayEquals(new int[]{5, 0}, AssamNavigator.borderPos(5));
    }

    @Test
    void borderPos_indice6_es6_0() {
        assertArrayEquals(new int[]{6, 0}, AssamNavigator.borderPos(6));
    }

    @Test
    void borderPos_indice11_es6_5() {
        assertArrayEquals(new int[]{6, 5}, AssamNavigator.borderPos(11));
    }

    @Test
    void borderPos_indice12_es6_6() {
        assertArrayEquals(new int[]{6, 6}, AssamNavigator.borderPos(12));
    }

    @Test
    void borderPos_indice17_es1_6() {
        assertArrayEquals(new int[]{1, 6}, AssamNavigator.borderPos(17));
    }

    @Test
    void borderPos_indice18_es0_6() {
        assertArrayEquals(new int[]{0, 6}, AssamNavigator.borderPos(18));
    }

    @Test
    void borderPos_indice23_es0_1() {
        assertArrayEquals(new int[]{0, 1}, AssamNavigator.borderPos(23));
    }

    @Test
    void borderPos_indiceNegativo_envuelve() {
        assertArrayEquals(new int[]{0, 0}, AssamNavigator.borderPos(-24));
    }

    @Test
    void borderPos_indiceMayorA24_envuelve() {
        assertArrayEquals(new int[]{0, 0}, AssamNavigator.borderPos(24));
    }

    @Test
    void borderDir_bordeNorte_retornaEste() {
        assertEquals(1, AssamNavigator.borderDir(0));
    }

    @Test
    void borderDir_bordeEste_retornaSur() {
        assertEquals(2, AssamNavigator.borderDir(8));
    }

    @Test
    void borderDir_bordeSur_retornaOeste() {
        assertEquals(3, AssamNavigator.borderDir(14));
    }

    @Test
    void borderDir_bordeOeste_retornaNorte() {
        assertEquals(0, AssamNavigator.borderDir(20));
    }

    @Test
    void borderDir_transicionNorteAEste_sigueSiendoEste() {
        assertEquals(1, AssamNavigator.borderDir(5));
        assertEquals(2, AssamNavigator.borderDir(6));
    }

    @Test
    void borderDir_indiceNegativo_envuelve() {
        assertEquals(1, AssamNavigator.borderDir(-24));
    }

    @Test
    void computePath_recortaCoordenadas_fueraDeTablero() {
        int[][] path = AssamNavigator.computePath(1, -1, 3, 3); // dir 3 => nx-- (sigue negativo)
        assertEquals(0, path[1][0]);  // clamped
        assertEquals(3, path[1][1]);
    }
}
