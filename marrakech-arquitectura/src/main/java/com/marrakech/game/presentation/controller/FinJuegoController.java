package com.marrakech.game.presentation.controller;

import com.marrakech.game.service.ChatServicio;
import com.marrakech.game.service.EstadoJuegoServicio;
import com.marrakech.game.service.GestionJuegoServicio;
import com.marrakech.game.service.PartidaServicio;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class FinJuegoController {

    private final StackPane endScreen;
    private final HBox gameScreen;
    private final Label resultadoLabel;
    private final Label winnerLabel;
    private final Label finalScores;
    private final Button btnVolverSala;
    private final Button btnVolverMenu;

    private final String[] playerColors = {"#e74c3c","#3498db","#2ecc71","#f39c12"};
    private final String[] playerNames = {"J1 (ROJO)","J2 (AZUL)","J3 (VERDE)","J4 (AMARILLO)"};

    private Runnable onVolverSala;
    private Runnable onVolverMenu;

    public FinJuegoController(StackPane endScreen, HBox gameScreen,
                               Label resultadoLabel, Label winnerLabel, Label finalScores,
                               Button btnVolverSala, Button btnVolverMenu) {
        this.endScreen = endScreen;
        this.gameScreen = gameScreen;
        this.resultadoLabel = resultadoLabel;
        this.winnerLabel = winnerLabel;
        this.finalScores = finalScores;
        this.btnVolverSala = btnVolverSala;
        this.btnVolverMenu = btnVolverMenu;
    }

    public void setOnVolverSala(Runnable r) { this.onVolverSala = r; }
    public void setOnVolverMenu(Runnable r) { this.onVolverMenu = r; }

    public void mostrar(GestionJuegoServicio juegoSvc,
                         boolean modoMultijugador, int miIndice, String usuarioActual,
                         PartidaServicio partidaSvc,
                         EstadoJuegoServicio estadoSvc,
                         ChatServicio chatSvc) {
        if (estadoSvc != null) estadoSvc.detenerPolling();
        if (chatSvc != null) chatSvc.detenerPolling();

        int numPlayers = juegoSvc.getNumPlayers();
        int[] money = juegoSvc.getMoney();
        int[][] tileOwner = juegoSvc.getTileOwner();

        int win = juegoSvc.calcularGanador();

        if (modoMultijugador && partidaSvc != null) partidaSvc.registrarVictoria(usuarioActual);

        boolean yoGane = !modoMultijugador || (win == miIndice);
        if (resultadoLabel != null) {
            resultadoLabel.setText(yoGane ? "✦  ¡HAS GANADO!  ✦" : "HAS PERDIDO");
            resultadoLabel.setStyle("-fx-font-size:54px;-fx-font-weight:bold;-fx-text-fill:"
                + (yoGane ? playerColors[win] : "#707070") + ";");
        }
        if (winnerLabel != null) {
            winnerLabel.setText("Ganador: " + playerNames[win]);
            winnerLabel.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:"
                + playerColors[win] + ";");
        }
        if (finalScores != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < numPlayers; i++) {
                int enTablero = juegoSvc.contarAlfombrasEnTablero(i);
                sb.append(playerNames[i]).append(":  ").append(money[i]).append(" Dh")
                  .append("   |   Alfombras en tablero: ").append(enTablero);
                if (i < numPlayers - 1) sb.append("\n");
            }
            finalScores.setText(sb.toString());
        }
        gameScreen.setVisible(false);
        endScreen.setVisible(true);
    }

    public void configurarBotones(Runnable volverSala, Runnable volverMenu) {
        btnVolverSala.setOnAction(e -> {
            if (volverSala != null) volverSala.run();
        });
        btnVolverMenu.setOnAction(e -> {
            if (volverMenu != null) volverMenu.run();
        });
    }
}
