package com.marrakech.game.domain.models;

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
    void testMoverNorte() {
        assam.move(2);
        assertEquals(3, assam.getX());
        assertEquals(1, assam.getY());
    }
    
    @Test
    void testRotarDerecha() {
        assam.rotate(1);
        assertEquals(1, assam.getOrientation());
    }
}
