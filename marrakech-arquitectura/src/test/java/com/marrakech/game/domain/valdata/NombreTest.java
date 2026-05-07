package com.marrakech.game.domain.valdata;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class NombreTest {
    
    @Test
    void testNombreValido() {
        Nombre nombre = new Nombre("Juan");
        assertEquals("Juan", nombre.value());
    }
    
    @Test
    void testNombreConEspacios() {
        Nombre nombre = new Nombre("  Ana Maria  ");
        assertEquals("Ana Maria", nombre.value());
    }
    
    @Test
    void testNombreCompleto() {
        Nombre nombre = new Nombre("Juan Pérez García");
        assertEquals("Juan Pérez García", nombre.value());
    }
    
    @Test
    void testNombreNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Nombre(null);
        });
    }
    
    @Test
    void testNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Nombre("");
        });
    }
    
    @Test
    void testNombreSoloEspacios() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Nombre("   ");
        });
    }
}
