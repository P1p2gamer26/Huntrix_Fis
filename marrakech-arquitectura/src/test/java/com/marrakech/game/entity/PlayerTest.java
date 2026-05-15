package com.marrakech.game.entity;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de la entidad Player (solo datos). */
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
    void testSetMoney() {
        player.setMoney(20);
        assertEquals(20, player.getMoney());
    }

    @Test
    void testSetCarpetCount() {
        player.setCarpetCount(10);
        assertEquals(10, player.getCarpetCount());
    }

    @Test
    void testDineroNoNegativo() {
        // El servicio evita negativos; aquí solo verificamos que setMoney acepta 0
        player.setMoney(0);
        assertEquals(0, player.getMoney());
    }

    @Test
    void testAlfombrasCeroEsValido() {
        player.setCarpetCount(0);
        assertEquals(0, player.getCarpetCount());
    }
}
