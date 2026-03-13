package com.marrakech.game.domain.models;

public class Tile {
    private Carpet carpet;
    private boolean hasAssam;

    public Tile() {
        this.carpet = null;
        this.hasAssam = false;
    }

    public Carpet getCarpet() { return carpet; }
    public void setCarpet(Carpet carpet) { this.carpet = carpet; }
    public boolean hasAssam() { return hasAssam; }
    public void setAssam(boolean hasAssam) { this.hasAssam = hasAssam; }
}
