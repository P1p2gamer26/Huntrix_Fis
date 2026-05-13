package com.marrakech.game.presentation.controller;

import com.marrakech.game.service.AssamNavigator;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

public class AssamController {

    private final ImageView assamView;
    private final GridPane boardGrid;
    private final Image[] assamImages = new Image[4];

    private int assamX = 3, assamY = 3, assamDir = 0;

    public AssamController(GridPane boardGrid) {
        this.boardGrid = boardGrid;
        this.assamView = crearImageView();
    }

    private ImageView crearImageView() {
        String[] paths = {
            "/images/marrakesh de atras.png",
            "/images/marrakesh perfil derecho.png",
            "/images/marrakesh de frente.png",
            "/images/marrakesh perfil izquierdo.png"
        };
        for (int i = 0; i < 4; i++) {
            try { assamImages[i] = new Image(getClass().getResourceAsStream(paths[i])); }
            catch (Exception e) { assamImages[i] = null; }
        }
        Image imgInicial;
        try { imgInicial = assamImages[0] != null ? assamImages[0]
                : new Image(getClass().getResourceAsStream("/images/assam.png")); }
        catch (Exception e) { imgInicial = null; }

        ImageView iv = new ImageView(imgInicial);
        iv.setFitWidth(50); iv.setFitHeight(58);
        iv.setMouseTransparent(true);
        boardGrid.add(iv, assamX, assamY);
        return iv;
    }

    public void rotarIzquierda() {
        assamDir = (assamDir + 3) % 4;
        setImage(assamDir);
    }

    public void rotarDerecha() {
        assamDir = (assamDir + 1) % 4;
        setImage(assamDir);
    }

    public void setDir(int dir) { this.assamDir = dir; }
    public void setPosition(int x, int y) { this.assamX = x; this.assamY = y; }

    public void setImage(int dir) {
        if (assamImages[dir] != null) {
            assamView.setImage(assamImages[dir]);
            assamView.setRotate(0);
        } else {
            assamView.setRotate(dir * 90);
        }
    }

    public void animarMovimiento(int pasos, Runnable onFinished) {
        int[][] path = AssamNavigator.computePath(pasos, assamX, assamY, assamDir);
        Timeline anim = new Timeline();
        for (int p = 1; p <= pasos; p++) {
            final int px = path[p][0], py = path[p][1], pd = path[p][2];
            anim.getKeyFrames().add(new KeyFrame(Duration.millis(p * 280L), e -> {
                assamX = px; assamY = py; assamDir = pd;
                GridPane.setColumnIndex(assamView, assamX);
                GridPane.setRowIndex(assamView, assamY);
                setImage(assamDir);
                assamView.toFront();
            }));
        }
        anim.getKeyFrames().add(new KeyFrame(Duration.millis(pasos * 280L + 60), e -> onFinished.run()));
        anim.play();
    }

    public void actualizarPosicionEnGrid() {
        GridPane.setColumnIndex(assamView, assamX);
        GridPane.setRowIndex(assamView, assamY);
        setImage(assamDir);
        assamView.toFront();
    }

    public ImageView getView() { return assamView; }
    public int getX() { return assamX; }
    public int getY() { return assamY; }
    public int getDir() { return assamDir; }
}
