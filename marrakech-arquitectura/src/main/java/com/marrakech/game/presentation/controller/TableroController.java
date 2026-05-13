package com.marrakech.game.presentation.controller;

import com.marrakech.game.service.CarpetValidador;
import com.marrakech.game.service.JuegoServicio;
import com.marrakech.game.presentation.render.GameRenderEngine;

import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class TableroController {

    private static final int CELL = 61;

    private final GridPane boardGrid;
    private final StackPane[][] tiles = new StackPane[7][7];
    private final int[][] tileOwner = new int[7][7];
    private final int[][] carpetOrientation = new int[7][7];
    private final GameRenderEngine renderEngine;

    private int firstCarpetX = -1, firstCarpetY = -1;
    private int currentPhase;
    private int currentPlayerIdx;
    private int assamX, assamY;
    private int[] rugsGlobal;

    private Runnable onCarpetPlaced;
    private Runnable onGameEnded;

    public TableroController(GridPane boardGrid, GameRenderEngine renderEngine) {
        this.boardGrid = boardGrid;
        this.renderEngine = renderEngine;
    }

    public void setOnCarpetPlaced(Runnable r) { this.onCarpetPlaced = r; }
    public void setOnGameEnded(Runnable r) { this.onGameEnded = r; }

    public void actualizarContexto(int phase, int playerIdx,
                                    int ax, int ay, int[] rugs) {
        this.currentPhase = phase;
        this.currentPlayerIdx = playerIdx;
        this.assamX = ax;
        this.assamY = ay;
        this.rugsGlobal = rugs;
    }

    public void inicializar() {
        boardGrid.getChildren().clear();
        for (int row = 0; row < 7; row++)
            for (int col = 0; col < 7; col++) {
                tileOwner[col][row] = 0;
                carpetOrientation[col][row] = 0;
                StackPane tile = new StackPane();
                tile.setPrefSize(CELL, CELL);
                tile.getStyleClass().add("tile");
                final int r = row, c = col;
                tile.setOnMouseClicked(e -> handleTileClick(c, r));
                tiles[col][row] = tile;
                boardGrid.add(tile, col, row);
            }
    }

    public void limpiarHighlights() {
        for (int row = 0; row < 7; row++)
            for (int col = 0; col < 7; col++)
                if (tiles[col][row] != null) tiles[col][row].setStyle("");
    }

    public void redibujar(ImageView assamView) {
        renderEngine.redibujarTableroCompleto(tileOwner, carpetOrientation, assamView);
    }

    public void highlightTile(int x, int y) {
        tiles[x][y].setStyle("-fx-background-color: rgba(255,255,255,0.25);");
    }

    public int procesarClick(int x, int y) {
        if (currentPhase == 1) {
            boolean adj = JuegoServicio.esAdyacenteA(x, y, assamX, assamY);
            boolean noAssam = JuegoServicio.noEsAssam(x, y, assamX, assamY);
            if (adj && noAssam && CarpetValidador.tiene2daOpcionValida(x, y, assamX, assamY)) {
                firstCarpetX = x; firstCarpetY = y;
                highlightTile(x, y);
                currentPhase = 2;
                return 2;
            }
            return adj && noAssam ? -1 : 0;
        }

        if (currentPhase == 2 && firstCarpetX >= 0) {
            boolean adj = JuegoServicio.esAlfombraAdyacente(firstCarpetX, firstCarpetY, x, y);
            boolean noAssam = JuegoServicio.noEsAssam(x, y, assamX, assamY);
            boolean dentro = CarpetValidador.esCarpetValida(firstCarpetX, firstCarpetY, x, y);

            if (adj && noAssam && dentro) {
                tiles[firstCarpetX][firstCarpetY].setStyle("");
                int player = currentPlayerIdx + 1;
                boolean horiz = (y == firstCarpetY);

                CarpetValidador.repararAlfombraAfectada(firstCarpetX, firstCarpetY, carpetOrientation);
                CarpetValidador.repararAlfombraAfectada(x, y, carpetOrientation);
                tileOwner[firstCarpetX][firstCarpetY] = player;
                tileOwner[x][y] = player;

                int topCol = Math.min(firstCarpetX, x);
                int topRow = Math.min(firstCarpetY, y);
                int botCol = Math.max(firstCarpetX, x);
                int botRow = Math.max(firstCarpetY, y);
                carpetOrientation[firstCarpetX][firstCarpetY] = 0;
                carpetOrientation[x][y] = 0;
                carpetOrientation[topCol][topRow] = horiz ? 2 : 1;
                carpetOrientation[botCol][botRow] = -1;

                rugsGlobal[currentPlayerIdx]--;
                firstCarpetX = -1; firstCarpetY = -1;

                if (JuegoServicio.juegoTerminado(rugsGlobal)) {
                    if (onGameEnded != null) onGameEnded.run();
                } else {
                    if (onCarpetPlaced != null) onCarpetPlaced.run();
                }
                return 3;
            }
            return -1;
        }

        return 0;
    }

    private void handleTileClick(int x, int y) {
        procesarClick(x, y);
    }

    public int[][] getTileOwner() { return tileOwner; }
    public int[][] getCarpetOrientation() { return carpetOrientation; }
    public int getFirstCarpetX() { return firstCarpetX; }
    public int getFirstCarpetY() { return firstCarpetY; }

    public void setFirstCarpetX(int x) { this.firstCarpetX = x; }
    public void setFirstCarpetY(int y) { this.firstCarpetY = y; }
    public void setCurrentPlayerIdx(int idx) { this.currentPlayerIdx = idx; }
    public void setAssamPos(int x, int y) { this.assamX = x; this.assamY = y; }

    public void setTileOwner(int[][] owner) {
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                tileOwner[c][r] = owner[c][r];
    }
    public void setCarpetOrientation(int[][] orient) {
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                carpetOrientation[c][r] = orient[c][r];
    }
    public StackPane[][] getTiles() { return tiles; }
}
