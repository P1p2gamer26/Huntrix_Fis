package com.marrakech.game.entitie;

public class Assam {
    private int x;
    private int y;
    private int orientation;

    public Assam(int x, int y) {
        this.x = x;
        this.y = y;
        this.orientation = 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getOrientation() { return orientation; }

    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void setOrientation(int orientation) { this.orientation = orientation; }
}
