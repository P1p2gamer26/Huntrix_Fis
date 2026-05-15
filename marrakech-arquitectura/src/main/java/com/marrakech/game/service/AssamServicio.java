package com.marrakech.game.service;

public class AssamServicio {

    private int x = 3, y = 3, dir = 0;

    public void rotarIzquierda() {
        dir = (dir + 3) % 4;
    }

    public void rotarDerecha() {
        dir = (dir + 1) % 4;
    }

    public int[][] computePath(int pasos) {
        return AssamNavigator.computePath(pasos, x, y, dir);
    }

    public void setPosition(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    public void setDir(int newDir) {
        this.dir = newDir;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getDir() { return dir; }
}
