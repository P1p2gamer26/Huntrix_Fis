package com.marrakech.game.domain.models;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private int money;
    private final String color; // Cambiado de javafx.scene.paint.Color a String para no mezclar UI con Dominio
    private final List<Carpet> carpets;
    private int carpetCount;

    public Player(String name, String color) {
        this.name = name;
        this.money = 30;
        this.color = color;
        this.carpets = new ArrayList<>();
        this.carpetCount = 15;
    }

    public void pay(int amount) { this.money -= amount; }
    public void earn(int amount) { this.money += amount; }

    public Carpet placeCarpet() {
        if (carpetCount > 0) {
            Carpet newCarpet = new Carpet(this);
            this.carpets.add(newCarpet);
            this.carpetCount--;
            return newCarpet;
        }
        return null;
    }

    public String getName() { return name; }
    public int getMoney() { return money; }
    public String getColor() { return color; }
    public int getCarpetCount() { return carpetCount; }
}
