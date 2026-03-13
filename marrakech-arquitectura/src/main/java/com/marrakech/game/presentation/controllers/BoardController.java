package com.marrakech.game.presentation.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class BoardController {
    @FXML private GridPane boardGrid;

    public void initialize() {
        if (boardGrid == null) return;
        final int BOARD_SIZE = 7;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                StackPane tile = new StackPane();
                tile.setPrefSize(80, 80);
                tile.getStyleClass().add("tile");
                boardGrid.add(tile, col, row);
            }
        }
    }
}
