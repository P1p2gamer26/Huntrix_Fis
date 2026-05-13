package com.marrakech.game.entitie;

/**
 * Representa a un jugador (entidad de solo datos).
 * Las operaciones de pago y colocación de alfombras están en GameController.
 */
public class Player {
    private final String name;
    private final String color;
    private int money;
    private int carpetCount;

    public Player(String name, String color) {
        this.name        = name;
        this.color       = color;
        this.money       = 30;
        this.carpetCount = 15;
    }

    public String getName()       { return name; }
    public String getColor()      { return color; }
    public int    getMoney()      { return money; }
    public void   setMoney(int m) { this.money = m; }
    public int    getCarpetCount(){ return carpetCount; }
    public void   setCarpetCount(int c) { this.carpetCount = c; }
}
