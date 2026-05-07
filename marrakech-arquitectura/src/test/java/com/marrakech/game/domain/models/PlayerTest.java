package com.marrakech.game.domain.models;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    
    private Player player;
    
    @BeforeEach
    void setUp() {
        player = new Player("Juan", "red");
    }
    
    @Test
    void testCrearJugador() {
        assertNotNull(player);
        assertEquals("Juan", player.getName());
        assertEquals("red", player.getColor());
        assertEquals(30, player.getMoney());
        assertEquals(15, player.getCarpetCount());
    }
    
    @Test
    void testPagarReduceDinero() {
        player.pay(10);
        assertEquals(20, player.getMoney());
    }
    
    @Test
    void testGanarAumentaDinero() {
        player.earn(20);
        assertEquals(50, player.getMoney());
    }
    
    @Test
    void testColocarAlfombra() {
        Carpet carpet = player.placeCarpet();
        assertNotNull(carpet);
        assertEquals(14, player.getCarpetCount());
        assertEquals(player, carpet.getOwner());
    }
    
    @Test
    void testNoColocarAlfombraSinInventario() {
        for (int i = 0; i < 15; i++) {
            player.placeCarpet();
        }
        Carpet carpet = player.placeCarpet();
        assertNull(carpet);
        assertEquals(0, player.getCarpetCount());
    }
}
