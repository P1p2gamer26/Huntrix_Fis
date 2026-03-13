package com.marrakech.game.domain.models;

public class Assam {
    private int x;
    private int y;
    private int orientation; 

    public Assam(int x, int y) {
        this.x = x;
        this.y = y;
        this.orientation = 0; 
    }

    public void move(int steps) {
        switch (orientation) {
            case 0: this.y -= steps; break; 
            case 1: this.x += steps; break; 
            case 2: this.y += steps; break; 
            case 3: this.x -= steps; break; 
        }
    }

    public void rotate(int direction) {
        this.orientation = (this.orientation + direction + 4) % 4;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public int getOrientation() { return orientation; }
}
