package com.marrakech.game.entitie;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class AssamTest {

    private Assam assam;

    @BeforeEach
    void setUp() {
        assam = new Assam(3, 3);
    }

    @Test
    void testCrearAssam() {
        assertNotNull(assam);
        assertEquals(3, assam.getX());
        assertEquals(3, assam.getY());
        assertEquals(0, assam.getOrientation());
    }

    @Test
    void testSetPosition() {
        assam.setPosition(5, 2);
        assertEquals(5, assam.getX());
        assertEquals(2, assam.getY());
    }

    @Test
    void testSetOrientation() {
        assam.setOrientation(2);
        assertEquals(2, assam.getOrientation());
    }
}
