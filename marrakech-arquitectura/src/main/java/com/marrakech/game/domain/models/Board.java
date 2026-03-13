package com.marrakech.game.domain.models;

public class Board {
    private final Tile[][] grid;

    public Board() {
        this.grid = new Tile[7][5];
        initializeBoard();
    }

    private void initializeBoard() {
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 5; y++) {
                grid[x][y] = new Tile();
            }
        }
    }

    public void placeCarpet(int x, int y, Carpet carpet) {
        if (isValidPosition(x, y)) { grid[x][y].setCarpet(carpet); }
    }

    public Tile getTile(int x, int y) {
        if (isValidPosition(x, y)) { return grid[x][y]; }
        return null;
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < 7 && y >= 0 && y < 5;
    }

    public Tile[][] getGrid() { return grid; }
}
