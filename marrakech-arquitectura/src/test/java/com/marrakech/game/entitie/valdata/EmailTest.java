package com.marrakech.game.entitie.valdata;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class EmailTest {
    
    @Test
    void testEmailValido() {
        Email email = new Email("user@example.com");
        assertEquals("user@example.com", email.value());
    }
    
    @Test
    void testEmailConMayusculas() {
        Email email = new Email("User@EXAMPLE.COM");
        assertEquals("user@example.com", email.value());
    }
    
    @Test
    void testEmailConEspacios() {
        Email email = new Email("  user@example.com  ");
        assertEquals("user@example.com", email.value());
    }
    
    @Test
    void testEmailNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Email(null);
        });
    }
    
    @Test
    void testEmailSinArroba() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Email("userexample.com");
        });
    }
    
    @Test
    void testEmailSinDominio() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Email("user@");
        });
    }
    
    @Test
    void testEmailSinExtension() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Email("user@example");
        });
    }
    
    @Test
    void testEmailVacio() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Email("");
        });
    }
    
    @Test
    void testEmailConCaracteresEspeciales() {
        Email email = new Email("user.name+tag@example.co.uk");
        assertEquals("user.name+tag@example.co.uk", email.value());
    }
}
