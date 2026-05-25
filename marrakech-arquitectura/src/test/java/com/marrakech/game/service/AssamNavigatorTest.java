package com.marrakech.game.service;

import org.junit.jupiter.api.Test;
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
        assertEquals(0, path[1][2]);
    }

    @Test
    void computePath_unPasoEste_avanzaUnaColumna() {
        int[][] path = AssamNavigator.computePath(1, 3, 3, 1);
        assertEquals(4, path[1][0]);
        assertEquals(3, path[1][1]);
        assertEquals(1, path[1][2]);
    }

    @Test
    void computePath_unPasoSur_bajaUnaFila() {
        int[][] path = AssamNavigator.computePath(1, 3, 3, 2);
        assertEquals(3, path[1][0]);
        assertEquals(4, path[1][1]);
        assertEquals(2, path[1][2]);
    }

    @Test
    void computePath_unPasoOeste_retrocedeUnaColumna() {
        int[][] path = AssamNavigator.computePath(1, 3, 3, 3);
        assertEquals(2, path[1][0]);
        assertEquals(3, path[1][1]);
        assertEquals(3, path[1][2]);
    }

    @Test
    void computePath_rebotaEnBordeNorte_haceSwapYQuedaMirandoAlSur() {
        int[][] path = AssamNavigator.computePath(1, 2, 0, 0);
        assertEquals(1, path[1][0]); // (2,0) rebota a (1,0)
        assertEquals(0, path[1][1]);
        assertEquals(2, path[1][2]); // queda mirando al Sur
    }

    @Test
    void computePath_rebotaEnBordeEste_haceSwapYQuedaMirandoAlOeste() {
        int[][] path = AssamNavigator.computePath(1, 6, 5, 1);
        assertEquals(6, path[1][0]);
        assertEquals(4, path[1][1]); // (6,5) rebota a (6,4)
        assertEquals(3, path[1][2]); // queda mirando al Oeste
    }

    @Test
    void computePath_rebotaEnBordeSur_haceSwapYQuedaMirandoAlNorte() {
        int[][] path = AssamNavigator.computePath(1, 1, 6, 2);
        assertEquals(0, path[1][0]); // (1,6) rebota a (0,6)
        assertEquals(6, path[1][1]);
        assertEquals(0, path[1][2]); // queda mirando al Norte
    }

    @Test
    void computePath_rebotaEnBordeOeste_haceSwapYQuedaMirandoAlEste() {
        int[][] path = AssamNavigator.computePath(1, 0, 1, 3);
        assertEquals(0, path[1][0]);
        assertEquals(2, path[1][1]); // (0,1) rebota a (0,2)
        assertEquals(1, path[1][2]); // queda mirando al Este
    }

    @Test
    void computePath_multiplesPasos_recorreRecto() {
        int[][] path = AssamNavigator.computePath(4, 0, 0, 1);
        assertEquals(5, path.length);
        assertEquals(0, path[0][0]);
        assertEquals(0, path[0][1]);
        assertEquals(1, path[1][0]);
        assertEquals(0, path[1][1]);
        assertEquals(2, path[2][0]);
        assertEquals(0, path[2][1]);
        assertEquals(3, path[3][0]);
        assertEquals(0, path[3][1]);
        assertEquals(4, path[4][0]);
        assertEquals(0, path[4][1]);
    }

    @Test
    void computePath_vueltaCompleta_conRebotes_generaRutaValida() {
        int pasos = 28;
        int[][] path = AssamNavigator.computePath(pasos, 0, 0, 1);

        // Lo único 100% seguro sin inventar reglas: el tamaño del path
        assertEquals(pasos + 1, path.length);

        // Y que cada punto tenga 3 valores (x,y,dir)
        for (int i = 0; i < path.length; i++) {
            assertEquals(3, path[i].length);
        }
    }

    @Test
    void computePath_direccionFinalEsquina_corregida() {
        int[][] path = AssamNavigator.computePath(5, 0, 0, 1);
        assertEquals(5, path[5][0]);
        assertEquals(0, path[5][1]);
        assertEquals(1, path[5][2]);
    }

    @Test
    void computePath_inicial_fueraDeRango_noRevienta() {
        int[][] path = AssamNavigator.computePath(0, -10, 99, 1);
        assertEquals(1, path.length);

        assertEquals(-10, path[0][0]);
        assertEquals(99, path[0][1]);
        assertEquals(1, path[0][2]);
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
    void borderPos_indice11_es6_5() {
        assertArrayEquals(new int[]{6, 5}, AssamNavigator.borderPos(11));
    }

    @Test
    void borderPos_indice12_es6_6() {
        assertArrayEquals(new int[]{6, 6}, AssamNavigator.borderPos(12));
    }

    @Test
    void borderPos_indice18_es0_6() {
        assertArrayEquals(new int[]{0, 6}, AssamNavigator.borderPos(18));
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
    void borderDir_indiceNegativo_envuelve() {
        assertEquals(1, AssamNavigator.borderDir(-24));
    }
}