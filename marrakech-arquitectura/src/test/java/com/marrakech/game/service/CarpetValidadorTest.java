package com.marrakech.game.service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class CarpetValidadorTest {

    @Test
    void esCarpetValida_ambasDentro_retornaTrue() {
        assertTrue(CarpetValidador.esCarpetValida(2, 3, 2, 4));
    }

    @Test
    void esCarpetValida_unaFuera_retornaFalse() {
        assertFalse(CarpetValidador.esCarpetValida(6, 6, 7, 6));
    }

    @Test
    void esCarpetValida_ambasFuera_retornaFalse() {
        assertFalse(CarpetValidador.esCarpetValida(-1, 0, 0, -1));
    }

    @Test
    void esCarpetValida_esquinaInferiorDerecha_retornaTrue() {
        assertTrue(CarpetValidador.esCarpetValida(6, 6, 5, 6));
    }

    @Test
    void esCarpetValida_esquinaSuperiorIzquierda_retornaTrue() {
        assertTrue(CarpetValidador.esCarpetValida(0, 0, 0, 1));
    }

    @Test
    void esCarpetValida_mismaCelda_retornaTrue() {
        assertTrue(CarpetValidador.esCarpetValida(3, 3, 3, 3));
    }

    @Test
    void tiene2daOpcionValida_centroConVecinos_retornaTrue() {
        assertTrue(CarpetValidador.tiene2daOpcionValida(3, 3, -1, -1));
    }

    @Test
    void tiene2daOpcionValida_esquinaConVecinos_retornaTrue() {
        assertTrue(CarpetValidador.tiene2daOpcionValida(0, 0, -1, -1));
    }

    @Test
    void tiene2daOpcionValida_assamBloqueaUnVecino_peroOtroValido_retornaTrue() {
        assertTrue(CarpetValidador.tiene2daOpcionValida(0, 0, 1, 0));
    }

    @Test
    void tiene2daOpcionValida_assamTapaUnVecinoPeroOtroValido_retornaTrue() {
        assertTrue(CarpetValidador.tiene2daOpcionValida(0, 0, 0, 1));
    }

    @Test
    void tiene2daOpcionValida_assamBloqueaUnVecino_otroSigueSiendoValido_retornaTrue() {
        assertTrue(CarpetValidador.tiene2daOpcionValida(0, 0, 0, 1));
    }

    @Test
    void repararAlfombraAfectada_orientacion0_noCambia() {
        int[][] ori = new int[7][7];
        ori[3][3] = 0;
        CarpetValidador.repararAlfombraAfectada(3, 3, ori);
        assertEquals(0, ori[3][3]);
    }

    @Test
    void repararAlfombraAfectada_orientacion3_seMantieneEn3() {
        int[][] ori = new int[7][7];
        ori[3][3] = 3;
        CarpetValidador.repararAlfombraAfectada(3, 3, ori);
        assertEquals(3, ori[3][3]);
    }

    @Test
    void repararAlfombraAfectada_ori1_conPartnerVertical_reparaPartner() {
        int[][] ori = new int[7][7];
        ori[2][3] = 1;
        ori[2][4] = -1;
        CarpetValidador.repararAlfombraAfectada(2, 3, ori);
        assertEquals(3, ori[2][4]);
        assertEquals(0, ori[2][3]);
    }

    @Test
    void repararAlfombraAfectada_ori1_sinPartner_noCambiaPartner() {
        int[][] ori = new int[7][7];
        ori[2][3] = 1;
        ori[2][4] = 0;
        CarpetValidador.repararAlfombraAfectada(2, 3, ori);
        assertEquals(0, ori[2][4]);
        assertEquals(0, ori[2][3]);
    }

    @Test
    void repararAlfombraAfectada_ori2_conPartnerHorizontal_reparaPartner() {
        int[][] ori = new int[7][7];
        ori[3][2] = 2;
        ori[4][2] = -1;
        CarpetValidador.repararAlfombraAfectada(3, 2, ori);
        assertEquals(3, ori[4][2]);
        assertEquals(0, ori[3][2]);
    }

    @Test
    void repararAlfombraAfectada_ori2_sinPartner_noCambiaPartner() {
        int[][] ori = new int[7][7];
        ori[3][2] = 2;
        ori[4][2] = 0;
        CarpetValidador.repararAlfombraAfectada(3, 2, ori);
        assertEquals(0, ori[4][2]);
        assertEquals(0, ori[3][2]);
    }

    @Test
    void repararAlfombraAfectada_menos1_conPartnerArriba_reparaArriba() {
        int[][] ori = new int[7][7];
        ori[2][3] = -1;
        ori[2][2] = 1;
        CarpetValidador.repararAlfombraAfectada(2, 3, ori);
        assertEquals(3, ori[2][2]);
        assertEquals(0, ori[2][3]);
    }

    @Test
    void repararAlfombraAfectada_menos1_conPartnerIzquierda_reparaIzquierda() {
        int[][] ori = new int[7][7];
        ori[3][2] = -1;
        ori[2][2] = 2;
        CarpetValidador.repararAlfombraAfectada(3, 2, ori);
        assertEquals(3, ori[2][2]);
        assertEquals(0, ori[3][2]);
    }

    @Test
    void repararAlfombraAfectada_menos1_sinPartner_noCambiaNada() {
        int[][] ori = new int[7][7];
        ori[2][3] = -1;
        CarpetValidador.repararAlfombraAfectada(2, 3, ori);
        assertEquals(0, ori[2][3]);
    }

    @Test
    void contarContiguas_celdaVacia_retornaCero() {
        int[][] owner = new int[7][7];
        assertEquals(0, CarpetValidador.contarContiguas(3, 3, 1, owner));
    }

    @Test
    void contarContiguas_unaSola_retornaUno() {
        int[][] owner = new int[7][7];
        owner[3][3] = 1;
        assertEquals(1, CarpetValidador.contarContiguas(3, 3, 1, owner));
    }

    @Test
    void contarContiguas_dosAdyacentes_retornaDos() {
        int[][] owner = new int[7][7];
        owner[3][3] = 1;
        owner[3][4] = 1;
        assertEquals(2, CarpetValidador.contarContiguas(3, 3, 1, owner));
    }

    @Test
    void contarContiguas_bloqueEnL_retornaCuatro() {
        int[][] owner = new int[7][7];
        owner[2][2] = 1; owner[2][3] = 1;
        owner[3][2] = 1; owner[3][3] = 1;
        assertEquals(4, CarpetValidador.contarContiguas(2, 2, 1, owner));
    }

    @Test
    void contarContiguas_noCuentaDeOtroOwner() {
        int[][] owner = new int[7][7];
        owner[2][2] = 1; owner[2][3] = 2;
        assertEquals(1, CarpetValidador.contarContiguas(2, 2, 1, owner));
    }

    @Test
    void contarContiguas_ownerDiferenteNoCuenta() {
        int[][] owner = new int[7][7];
        owner[3][3] = 2;
        assertEquals(0, CarpetValidador.contarContiguas(3, 3, 1, owner));
    }

    @Test
    void contarContiguas_lineaHorizontal_retornaCinco() {
        int[][] owner = new int[7][7];
        for (int c = 1; c <= 5; c++) owner[c][3] = 1;
        assertEquals(5, CarpetValidador.contarContiguas(1, 3, 1, owner));
    }

    @Test
    void contarContiguas_celdaAisladaPorOtroOwner_retornaUno() {
        int[][] owner = new int[7][7];
        owner[3][3] = 1;
        owner[2][3] = 2; owner[4][3] = 2;
        owner[3][2] = 2; owner[3][4] = 2;
        assertEquals(1, CarpetValidador.contarContiguas(3, 3, 1, owner));
    }

    @Test
    void contarContiguas_tableroCompletoMismoOwner_retornaCuarentaYNueve() {
        int[][] owner = new int[7][7];
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                owner[c][r] = 1;
        assertEquals(49, CarpetValidador.contarContiguas(0, 0, 1, owner));
    }

    @Test
    void repararAlfombraAfectada_ori1_alBordeSinPartner_noFalla() {
        int[][] ori = new int[7][7];
        ori[0][6] = 1;
        CarpetValidador.repararAlfombraAfectada(0, 6, ori);
        assertEquals(0, ori[0][6]);
    }

    @Test
    void repararAlfombraAfectada_menos1_alBordeSinPartner_noFalla() {
        int[][] ori = new int[7][7];
        ori[0][0] = -1;
        CarpetValidador.repararAlfombraAfectada(0, 0, ori);
        assertEquals(0, ori[0][0]);
    }

    @Test
    void contarContiguas_puntoPartidaNoEsOwner_retornaCero() {
        int[][] owner = new int[7][7];
        owner[3][3] = 1;
        assertEquals(0, CarpetValidador.contarContiguas(0, 0, 1, owner));
    }
}
