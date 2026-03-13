package com.marrakech.game.domain.models;

public class Carpet {
    private final Player owner;
    private final String color;

    public Carpet(Player owner) {
        this.owner = owner;
        this.color = owner.getColor();
    }

    public Player getOwner() { return owner; }
    public String getColor() { return color; }
}
