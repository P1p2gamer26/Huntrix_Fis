package com.marrakech.game.presentation.controller;

import com.marrakech.game.service.GestionJuegoServicio;
import com.marrakech.game.service.GestionJuegoServicio.ResultadoClick;
import com.marrakech.game.service.GestionJuegoServicio.ResultadoTipo;
import com.marrakech.game.presentation.render.GameRenderEngine;

import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class TableroController {

    private static final int CELL = 61;

    private final GridPane boardGrid;
    private final StackPane[][] tiles = new StackPane[7][7];
    private final GameRenderEngine renderEngine;
    private final GestionJuegoServicio juegoSvc;

    private int assamX, assamY;

    private Runnable onCarpetPlaced;
    private Runnable onGameEnded;

    public TableroController(GridPane boardGrid, GameRenderEngine renderEngine,
                              GestionJuegoServicio juegoSvc) {
        this.boardGrid = boardGrid;
        this.renderEngine = renderEngine;
        this.juegoSvc = juegoSvc;
    }

    public void setOnCarpetPlaced(Runnable r) { this.onCarpetPlaced = r; }
    public void setOnGameEnded(Runnable r) { this.onGameEnded = r; }

    public void actualizarContexto(int phase, int playerIdx,
                                    int ax, int ay, int[] rugs) {
        juegoSvc.setCurrentPhase(phase);
        juegoSvc.setCurrentPlayerIdx(playerIdx);
        this.assamX = ax;
        this.assamY = ay;
    }

    public void inicializar() {
        boardGrid.getChildren().clear();
        for (int row = 0; row < 7; row++)
            for (int col = 0; col < 7; col++) {
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
        renderEngine.redibujarTableroCompleto(
            juegoSvc.getTileOwner(), juegoSvc.getCarpetOrientation(), assamView);
    }

    public void highlightTile(int x, int y) {
        tiles[x][y].setStyle("-fx-background-color: rgba(255,255,255,0.25);");
    }

    private void handleTileClick(int x, int y) {
        ResultadoClick res = juegoSvc.procesarClick(x, y, assamX, assamY);
        switch (res.tipo) {
            case ESPERA_SEGUNDA:
                highlightTile(res.x1, res.y1);
                break;
            case ALFOMBRA_COLOCADA:
                redibujar(null);
                if (onCarpetPlaced != null) onCarpetPlaced.run();
                break;
            case JUEGO_TERMINADO:
                redibujar(null);
                if (onGameEnded != null) onGameEnded.run();
                break;
            default:
                break;
        }
    }

    public int[][] getTileOwner() { return juegoSvc.getTileOwner(); }
    public int[][] getCarpetOrientation() { return juegoSvc.getCarpetOrientation(); }
    public int getFirstCarpetX() { return juegoSvc.getFirstCarpetX(); }
    public int getFirstCarpetY() { return juegoSvc.getFirstCarpetY(); }

    public void setFirstCarpetX(int x) { /* no-op, state in service */ }
    public void setFirstCarpetY(int y) { /* no-op, state in service */ }
    public void setCurrentPlayerIdx(int idx) { juegoSvc.setCurrentPlayerIdx(idx); }
    public void setAssamPos(int x, int y) { this.assamX = x; this.assamY = y; }

    public void setTileOwner(int[][] owner) {
        int[][] to = juegoSvc.getTileOwner();
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                to[c][r] = owner[c][r];
    }

    public void setCarpetOrientation(int[][] orient) {
        int[][] co = juegoSvc.getCarpetOrientation();
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                co[c][r] = orient[c][r];
    }

    public StackPane[][] getTiles() { return tiles; }
}
