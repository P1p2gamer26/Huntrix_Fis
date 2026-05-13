package com.marrakech.game.entitie;

import java.util.List;

/**
 * Estado de una partida local (entidad de solo datos).
 * La lógica de turnos, dados y pagos vive en GameController.
 */
public class Game {
    private final List<Player> players;
    private int                currentPlayerIndex;
    private final Board        board;
    private final Assam        assam;

    public Game(List<Player> players) {
        this.players            = players;
        this.board              = new Board();
        this.assam              = new Assam(3, 3);
        this.currentPlayerIndex = 0;
    }

    public List<Player> getPlayers()          { return players; }
    public Player       getCurrentPlayer()    { return players.get(currentPlayerIndex); }
    public int          getCurrentPlayerIndex(){ return currentPlayerIndex; }
    public void         setCurrentPlayerIndex(int idx) { this.currentPlayerIndex = idx; }
    public Board        getBoard()            { return board; }
    public Assam        getAssam()            { return assam; }
}
