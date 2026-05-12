package com.marrakech.game.presentation.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
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

    /** Dibuja el dado en estado inicial. */
    public void dibujarDadoInicial() {
        if (diceCanvas == null) return;
        dibujarCaraDado(Color.rgb(195, 130, 60), Color.rgb(140, 80, 20));
        GraphicsContext gc = diceCanvas.getGraphicsContext2D();
        double w = diceCanvas.getWidth(), h = diceCanvas.getHeight();
        gc.setFill(Color.rgb(200, 160, 80, 0.5));
        gc.fillOval(w / 2 - 8, h / 2 - 8, 16, 16);
    }

    /** Anima el dado (efecto de giro) y termina mostrando el resultado real. */
    public void animarDado(int resultado, Runnable onFinished) {
        Random rng = new Random();
        Timeline anim = new Timeline();
        int totalFrames = 18;

        for (int i = 0; i < totalFrames; i++) {
            anim.getKeyFrames().add(new KeyFrame(
                Duration.millis(50 + i * 28L),
                e -> dibujarDadoCanvas(rng.nextInt(6) + 1, true)));
        }
        anim.getKeyFrames().add(new KeyFrame(
            Duration.millis(50 + totalFrames * 28L),
            e -> { dibujarDadoCanvas(resultado, false); onFinished.run(); }));
        anim.play();
    }

    private static final double[][] POSICIONES_DADO = {
        {55, 55},                                              // 1: centro
        {35, 35}, {75, 75},                                   // 2: diagonal
        {35, 35}, {55, 55}, {75, 75},                         // 3: diagonal + centro
        {35, 35}, {75, 35}, {35, 75}, {75, 75},              // 4: 4 esquinas
        {35, 35}, {75, 35}, {55, 55}, {35, 75}, {75, 75},    // 5: 4 esquinas + centro
        {35, 35}, {55, 35}, {75, 35}, {35, 75}, {55, 75}, {75, 75} // 6: 2 columnas
    };
    private static final int[] INDICES_DADO = {0, 1, 3, 6, 10, 15};

    private void dibujarCaraDado(Color fondo, Color borde) {
        if (diceCanvas == null) return;
        GraphicsContext gc = diceCanvas.getGraphicsContext2D();
        double w = diceCanvas.getWidth(), h = diceCanvas.getHeight(), m = 8;
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.rgb(0, 0, 0, 0.4));
        gc.fillRoundRect(m + 3, m + 3, w - m * 2, h - m * 2, 18, 18);
        gc.setFill(fondo);
        gc.fillRoundRect(m, m, w - m * 2, h - m * 2, 16, 16);
        gc.setStroke(borde);
        gc.setLineWidth(2);
        gc.strokeRoundRect(m, m, w - m * 2, h - m * 2, 16, 16);
        dibujarVetaMadera(gc, w, h, m);
    }

    private void dibujarPips(GraphicsContext gc, int valor, double pr) {
        int idx = INDICES_DADO[Math.min(valor - 1, 5)];
        int fin = Math.min(valor, 6);
        gc.setFill(Color.rgb(40, 14, 3));
        for (int i = idx; i < idx + fin; i++) {
            double[] p = POSICIONES_DADO[i];
            gc.fillOval(p[0] - pr, p[1] - pr, pr * 2, pr * 2);
        }
        gc.setFill(Color.rgb(245, 215, 170));
        for (int i = idx; i < idx + fin; i++) {
            double[] p = POSICIONES_DADO[i];
            gc.fillOval(p[0] - pr + 1.5, p[1] - pr + 1.5, pr * 2 - 3, pr * 2 - 3);
        }
    }

    private void dibujarDadoCanvas(int valor, boolean animando) {
        dibujarCaraDado(
            animando ? Color.rgb(230, 165, 80) : Color.rgb(210, 148, 72),
            animando ? Color.rgb(255, 220, 100, 0.9) : Color.rgb(160, 90, 20));
        if (diceCanvas == null) return;
        GraphicsContext gc = diceCanvas.getGraphicsContext2D();
        dibujarPips(gc, valor, animando ? 5.5 : 7);
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
