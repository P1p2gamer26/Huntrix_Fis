package com.marrakech.game.entity;

public class Assam {
    private int x;
    private int y;
    private int orientation;

    public Assam(int x, int y) {
        this(x, y, 0);
    }

    public Assam(int x, int y, int orientation) {
        this.x = x;
        this.y = y;
        this.orientation = orientation;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getOrientation() { return orientation; }
}
