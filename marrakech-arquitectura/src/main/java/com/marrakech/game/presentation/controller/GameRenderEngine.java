package com.marrakech.game.presentation.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.Random;

/**
 * Motor de renderizado para el tablero y el dado.
 * Dibuja alfombras con Canvas (sin imágenes PNG) y anima el dado de chancletas.
 */
public class GameRenderEngine {

    private static final int CELL = 61;

    private static final Color[] COLORES_BASE = {
        Color.rgb(231, 76,  60,  0.82),
        Color.rgb(52,  152, 219, 0.82),
        Color.rgb(46,  204, 113, 0.82),
        Color.rgb(243, 156, 18,  0.82)
    };
    private static final Color[] COLORES_BORDE = {
        Color.rgb(180, 30,  10),
        Color.rgb(20,  90,  180),
        Color.rgb(20,  140, 70),
        Color.rgb(180, 110, 5)
    };

    private final GridPane  boardGrid;
    private final Canvas    diceCanvas;

    public GameRenderEngine(GridPane boardGrid, Canvas diceCanvas) {
        this.boardGrid  = boardGrid;
        this.diceCanvas = diceCanvas;
    }

    // ── Dado ──────────────────────────────────────────────────────────────────

    /** Dibuja el dado en estado inicial (signo '?'). */
    public void dibujarDadoInicial() {
        if (diceCanvas == null) return;
        GraphicsContext gc = diceCanvas.getGraphicsContext2D();
        double w = diceCanvas.getWidth(), h = diceCanvas.getHeight(), m = 8;

        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.rgb(0, 0, 0, 0.35));
        gc.fillRoundRect(m + 3, m + 3, w - m * 2, h - m * 2, 18, 18);
        gc.setFill(Color.rgb(185, 120, 55));
        gc.fillRoundRect(m, m, w - m * 2, h - m * 2, 16, 16);
        gc.setStroke(Color.rgb(140, 80, 20));
        gc.setLineWidth(2);
        gc.strokeRoundRect(m, m, w - m * 2, h - m * 2, 16, 16);
        dibujarVetaMadera(gc, w, h, m);

        gc.setFill(Color.rgb(240, 200, 100, 0.6));
        gc.setFont(Font.font(28));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("?", w / 2, h / 2 + 10);
    }

    /** Anima el dado (efecto de giro) y termina mostrando el resultado real. */
    public void animarDado(int resultado, Runnable onFinished) {
        Random rng = new Random();
        Timeline anim = new Timeline();
        int totalFrames = 18;

        for (int i = 0; i < totalFrames; i++) {
            anim.getKeyFrames().add(new KeyFrame(
                Duration.millis(50 + i * 28L),
                e -> dibujarDadoCanvas(rng.nextInt(4) + 1, true)));
        }
        anim.getKeyFrames().add(new KeyFrame(
            Duration.millis(50 + totalFrames * 28L),
            e -> { dibujarDadoCanvas(resultado, false); onFinished.run(); }));
        anim.play();
    }

    private void dibujarDadoCanvas(int valor, boolean animando) {
        if (diceCanvas == null) return;
        GraphicsContext gc = diceCanvas.getGraphicsContext2D();
        double w = diceCanvas.getWidth(), h = diceCanvas.getHeight(), m = 8;

        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.rgb(0, 0, 0, 0.4));
        gc.fillRoundRect(m + 3, m + 3, w - m * 2, h - m * 2, 18, 18);
        gc.setFill(animando ? Color.rgb(220, 155, 75) : Color.rgb(205, 140, 70));
        gc.fillRoundRect(m, m, w - m * 2, h - m * 2, 16, 16);
        gc.setStroke(animando ? Color.rgb(255, 220, 100, 0.9) : Color.rgb(160, 90, 20));
        gc.setLineWidth(animando ? 2.5 : 2);
        gc.strokeRoundRect(m, m, w - m * 2, h - m * 2, 16, 16);
        dibujarVetaMadera(gc, w, h, m);

        gc.setFont(Font.font(animando ? 20 : 22));
        gc.setTextAlign(TextAlignment.CENTER);
        dibujarChancletas(gc, valor, w, h);
    }

    /** Distribuye las chancletas como en un dado real (posiciones clásicas). */
    private void dibujarChancletas(GraphicsContext gc, int valor, double w, double h) {
        double cx = w / 2, cy = h / 2, off = 20;
        double[][] pos;
        switch (valor) {
            case 1: pos = new double[][]{{cx, cy}}; break;
            case 2: pos = new double[][]{{cx - off, cy - off}, {cx + off, cy + off}}; break;
            case 3: pos = new double[][]{{cx - off, cy - off}, {cx, cy}, {cx + off, cy + off}}; break;
            case 4: pos = new double[][]{{cx-off,cy-off},{cx+off,cy-off},{cx-off,cy+off},{cx+off,cy+off}}; break;
            default: pos = new double[][]{{cx, cy}};
        }
        gc.setFill(Color.rgb(30, 10, 0, 0.85));
        for (double[] p : pos) gc.fillText("🥿", p[0], p[1] + 8);
    }

    private void dibujarVetaMadera(GraphicsContext gc, double w, double h, double m) {
        gc.setStroke(Color.rgb(160, 95, 35, 0.3));
        gc.setLineWidth(1);
        for (int i = 0; i < 4; i++) {
            double lx = m + 10 + i * 18;
            gc.strokeLine(lx, m + 6, lx + 5, h - m - 6);
        }
    }

    // ── Alfombras ─────────────────────────────────────────────────────────────

    /**
     * Redibuja todas las alfombras del tablero desde cero.
     * Elimina canvases anteriores y pinta solo las celdas con orientación válida.
     */
    public void redibujarTableroCompleto(int[][] tileOwner, int[][] carpetOrientation,
                                          javafx.scene.image.ImageView assamView) {
        boardGrid.getChildren().removeIf(
            n -> (n instanceof Canvas) || (n instanceof javafx.scene.image.ImageView && n != assamView));

        for (int row = 0; row < 7; row++)
            for (int col = 0; col < 7; col++) {
                int ori = carpetOrientation[col][row];
                if ((ori == 1 || ori == 2 || ori == 3) && tileOwner[col][row] > 0)
                    redibujarCelda(col, row, tileOwner[col][row], carpetOrientation);
            }
        assamView.toFront();
    }

    private void redibujarCelda(int col, int row, int player, int[][] carpetOrientation) {
        int ori = carpetOrientation[col][row];
        if (ori == 0 || ori == -1) return;
        if (ori == 3) { colocarAlfombraCanvas(col, row, false, player, false); return; }
        colocarAlfombraCanvas(col, row, ori == 2, player, true);
    }

    /**
     * Pinta una alfombra como Canvas con diseño árabe geométrico.
     * @param horizontal true = 2 celdas horizontales, false = 2 celdas verticales.
     * @param dosCeldas  false = solo una celda (mitad huérfana).
     */
    public void colocarAlfombraCanvas(int col, int row, boolean horizontal,
                                       int player, boolean dosCeldas) {
        int    spanC = 1, spanR = 1;
        double anchoReal, altoReal;

        if (!dosCeldas) {
            anchoReal = CELL; altoReal = CELL;
        } else if (horizontal) {
            spanC = 2; anchoReal = CELL * 2; altoReal = CELL;
        } else {
            spanR = 2; anchoReal = CELL; altoReal = CELL * 2;
        }

        Canvas canvas = new Canvas(anchoReal, altoReal);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Color base  = COLORES_BASE[player - 1];
        Color borde = COLORES_BORDE[player - 1];

        double ox = 2, oy = 2, iw = anchoReal - 4, ih = altoReal - 4;

        gc.setFill(base);
        gc.fillRoundRect(ox, oy, iw, ih, 6, 6);
        gc.setStroke(borde);
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(ox, oy, iw, ih, 6, 6);

        // Patrón árabe geométrico
        gc.setStroke(Color.color(1, 1, 1, 0.28));
        gc.setLineWidth(1.0);
        double inset = 6;
        gc.strokeRect(ox + inset, oy + inset, iw - inset * 2, ih - inset * 2);
        gc.strokeLine(ox + iw / 2, oy + inset,  ox + iw / 2, oy + ih - inset);
        gc.strokeLine(ox + inset,  oy + ih / 2, ox + iw - inset, oy + ih / 2);
        double cs = Math.min(iw, ih) * 0.20;
        gc.strokeLine(ox + inset,      oy + inset,      ox + inset + cs,     oy + inset + cs);
        gc.strokeLine(ox + iw - inset, oy + inset,      ox + iw - inset - cs,oy + inset + cs);
        gc.strokeLine(ox + inset,      oy + ih - inset, ox + inset + cs,     oy + ih - inset - cs);
        gc.strokeLine(ox + iw - inset, oy + ih - inset, ox + iw - inset - cs,oy + ih - inset - cs);
        gc.setFill(Color.color(1, 1, 1, 0.55));
        gc.fillOval(ox + iw / 2 - 3, oy + ih / 2 - 3, 6, 6);

        canvas.setMouseTransparent(true);
        if (spanC > 1) GridPane.setColumnSpan(canvas, spanC);
        if (spanR > 1) GridPane.setRowSpan(canvas, spanR);
        boardGrid.add(canvas, col, row);
    }
}
