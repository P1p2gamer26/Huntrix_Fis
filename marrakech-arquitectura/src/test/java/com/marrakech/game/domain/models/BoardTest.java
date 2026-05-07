package com.marrakech.game.domain.models;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    
    private Board board;
    
    @BeforeEach
    void setUp() {
        board = new Board();
    }
    
    @Test
    void testCrearTablero() {
        assertNotNull(board);
        assertNotNull(board.getGrid());
        assertEquals(7, board.getGrid().length);
    }
    
    @Test
    void testColocarAlfombra() {
        Player player = new Player("Test", "green");
        Carpet carpet = new Carpet(player);
        board.placeCarpet(2, 3, carpet);
        assertNotNull(board.getTile(2, 3).getCarpet());
    }
}
