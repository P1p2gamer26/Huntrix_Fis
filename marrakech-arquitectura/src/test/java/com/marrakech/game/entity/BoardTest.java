package com.marrakech.game.entity;

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
    void testGetTile_devuelveTileEnPosicion() {
        assertNotNull(board.getTile(2, 3));
    }
    
    @Test
    void testGetTile_fueraDelTablero_devuelveNull() {
        assertNull(board.getTile(-1, -1));
        assertNull(board.getTile(7, 7));
    }
}
