package com.marrakech.game.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssamServicioTest {

    @Test
    void rotaciones_y_path() {
        AssamServicio a = new AssamServicio();

        a.setDir(0);
        a.rotarIzquierda();
        assertEquals(3, a.getDir());

        a.rotarDerecha();
        assertEquals(0, a.getDir());

        a.setPosition(3, 3);
        int[][] path = a.computePath(2);
        assertEquals(3, path.length);
    }

    @Test
    void gettersXyY_quedanCubiertos() {
        AssamServicio a = new AssamServicio();
        a.setPosition(5, 1);

        assertEquals(5, a.getX());
        assertEquals(1, a.getY());
    }
}
