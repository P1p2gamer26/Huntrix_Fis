package com.marrakech.game.entity;

public class Board {
    private final Tile[][] grid;

    public Board() {
        this.grid = new Tile[7][7];
        for (int x = 0; x < 7; x++)
            for (int y = 0; y < 7; y++)
                grid[x][y] = new Tile();
    }

    public Tile getTile(int x, int y) {
        if (x >= 0 && x < 7 && y >= 0 && y < 7) return grid[x][y];
        return null;
    }

    public Tile[][] getGrid() { return grid; }
}
