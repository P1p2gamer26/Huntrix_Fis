package com.marrakech.game.entity;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class AssamTest {

    @Test
    void testCrearAssam() {
        Assam assam = new Assam(3, 3);
        assertNotNull(assam);
        assertEquals(3, assam.getX());
        assertEquals(3, assam.getY());
        assertEquals(0, assam.getOrientation());
    }

    @Test
    void testCrearAssamOtraPosicion() {
        Assam assam = new Assam(5, 2);
        assertEquals(5, assam.getX());
        assertEquals(2, assam.getY());
    }

    @Test
    void testCrearAssamConOrientacionPersonalizada() {
        Assam assam = new Assam(1, 4, 2);
        assertEquals(1, assam.getX());
        assertEquals(4, assam.getY());
        assertEquals(2, assam.getOrientation());
    }
}
