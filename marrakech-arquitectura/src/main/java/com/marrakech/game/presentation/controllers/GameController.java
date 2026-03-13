package com.marrakech.game.presentation.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import java.util.Random;

public class GameController {

    @FXML private GridPane boardGrid;
    @FXML private Label statusLabel;
    @FXML private Label turnLabel;
    @FXML private Button rollDiceBtn;

    private ImageView assamView;
    private int assamX = 3, assamY = 3;
    private int assamDir = 0; // 0=Norte, 1=Este, 2=Sur, 3=Oeste

    // Lógica del juego
    private enum Phase { MOVE, CARPET_1, CARPET_2 }
    private Phase currentPhase = Phase.MOVE;
    private int currentPlayer = 1; // 1=Rojo, 2=Azul
    private int firstCarpetX = -1, firstCarpetY = -1;
    private StackPane[][] tiles = new StackPane[7][7];

    public void initialize() {
        if (boardGrid == null) return;

        // 1. Crear la cuadrícula interactiva
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                StackPane tile = new StackPane();
                tile.setPrefSize(70, 70);
                tile.getStyleClass().add("tile");
                
                final int r = row, c = col;
                tile.setOnMouseClicked(e -> handleTileClick(c, r));
                
                tiles[col][row] = tile;
                boardGrid.add(tile, col, row);
            }
        }

        // 2. Cargar a Assam
        try {
            Image assamImg = new Image(getClass().getResourceAsStream("/images/assam.png"));
            assamView = new ImageView(assamImg);
            assamView.setFitWidth(40);
            assamView.setFitHeight(55);
            boardGrid.add(assamView, assamX, assamY);
        } catch (Exception e) {
            System.out.println("No se encontró assam.png. Revisa src/main/resources/images/");
        }
    }

    @FXML protected void rotateLeft() {
        if(currentPhase != Phase.MOVE) return;
        assamDir = (assamDir + 3) % 4; // -90 grados
        updateAssamRotation();
    }

    @FXML protected void rotateRight() {
        if(currentPhase != Phase.MOVE) return;
        assamDir = (assamDir + 1) % 4; // +90 grados
        updateAssamRotation();
    }

    private void updateAssamRotation() {
        assamView.setRotate(assamDir * 90);
    }

    @FXML protected void onRollDiceClick() {
        if(currentPhase != Phase.MOVE) return;
        
        int pasos = new Random().nextInt(4) + 1;
        
        // Mover según dirección
        switch(assamDir) {
            case 0: assamY -= pasos; break; // Norte
            case 1: assamX += pasos; break; // Este
            case 2: assamY += pasos; break; // Sur
            case 3: assamX -= pasos; break; // Oeste
        }
        
        // Límite temporal básico
        if(assamX < 0 || assamX > 6 || assamY < 0 || assamY > 6) {
            assamX = 3; assamY = 3; 
        }

        GridPane.setColumnIndex(assamView, assamX);
        GridPane.setRowIndex(assamView, assamY);
        assamView.toFront(); // Que Assam siempre quede encima

        currentPhase = Phase.CARPET_1;
        rollDiceBtn.setDisable(true);
        statusLabel.setText("Dado: " + pasos + ". Selecciona 1ra mitad de tu alfombra junto a Assam.");
    }

    private void handleTileClick(int x, int y) {
        if (currentPhase == Phase.CARPET_1) {
            if ((Math.abs(assamX - x) + Math.abs(assamY - y) == 1)) {
                firstCarpetX = x;
                firstCarpetY = y;
                pintarAlfombra(x, y, currentPlayer);
                currentPhase = Phase.CARPET_2;
                statusLabel.setText("Selecciona la 2da mitad adyacente a la primera.");
            }
        } 
        else if (currentPhase == Phase.CARPET_2) {
            if ((Math.abs(firstCarpetX - x) + Math.abs(firstCarpetY - y) == 1) && !(x == assamX && y == assamY)) {
                pintarAlfombra(x, y, currentPlayer);
                
                currentPlayer = (currentPlayer == 1) ? 2 : 1;
                turnLabel.setText("TURNO: JUGADOR " + currentPlayer + (currentPlayer == 1 ? " (ROJO)" : " (AZUL)"));
                statusLabel.setText("Paso 1: Rota a Assam y lanza el dado.");
                currentPhase = Phase.MOVE;
                rollDiceBtn.setDisable(false);
                assamView.toFront();
            }
        }
    }

    private void pintarAlfombra(int x, int y, int player) {
        StackPane tile = tiles[x][y];
        tile.getStyleClass().clear();
        tile.getStyleClass().add("tile");
        if (player == 1) tile.getStyleClass().add("carpet-p1");
        else tile.getStyleClass().add("carpet-p2");
    }
}
