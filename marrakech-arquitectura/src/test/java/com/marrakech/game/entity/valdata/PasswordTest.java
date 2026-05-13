package com.marrakech.game.entity.valdata;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {
    
    @Test
    void testPasswordValida() {
        Password password = new Password("123456");
        assertEquals("123456", password.value());
    }
    
    @Test
    void testPasswordLarga() {
        Password password = new Password("contrase�aSegura123");
        assertEquals("contrase�aSegura123", password.value());
    }
    
    @Test
    void testPasswordMinima() {
        Password password = new Password("abcdef");
        assertEquals("abcdef", password.value());
    }
    
    @Test
    void testPasswordNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Password(null);
        });
    }
    
    @Test
    void testPasswordCorta() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Password("12345");
        });
    }
    
    @Test
    void testPasswordVacia() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Password("");
        });
    }
    
    @Test
    void testPasswordConEspacios() {
        Password password = new Password("pass word");
        assertEquals("pass word", password.value());
    }
}
