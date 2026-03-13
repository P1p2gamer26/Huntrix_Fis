package com.marrakech.game.domain.models;

import java.util.List;
import java.util.Random;

public class Game {
    private final List<Player> players;
    private Player currentPlayer;
    private final Board board;
    private final Random dice = new Random();
    private final Assam assam;

    public Game(List<Player> players) {
        this.players = players;
        this.board = new Board();
        this.assam = new Assam(3, 3);
        if (!players.isEmpty()) {
            this.currentPlayer = players.get(0);
        }
    }

    public void switchPlayer() {
        int currentIndex = players.indexOf(currentPlayer);
        int nextIndex = (currentIndex + 1) % players.size();
        currentPlayer = players.get(nextIndex);
    }

    public Player getCurrentPlayer() { return currentPlayer; }
    public Board getBoard() { return board; }
    public List<Player> getPlayers() { return players; }
    public int[] getAssamPosition() { return new int[]{assam.getX(), assam.getY()}; }
    public Assam getAssam() { return assam; }
    public int rollDice() { return dice.nextInt(6) + 1; }

    public void playTurn(int diceResult) {
        System.out.println(currentPlayer.getName() + " is playing their turn.");
    }
}
