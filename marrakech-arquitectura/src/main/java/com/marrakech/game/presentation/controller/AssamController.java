package com.marrakech.game.presentation.controller;

import com.marrakech.game.service.AssamServicio;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

public class AssamController {

    private final AssamServicio assamSvc;
    private final ImageView assamView;
    private final GridPane boardGrid;
    private final Image[] assamImages = new Image[4];

    public AssamController(AssamServicio assamSvc, GridPane boardGrid) {
        this.assamSvc = assamSvc;
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
        boardGrid.add(iv, assamSvc.getX(), assamSvc.getY());
        return iv;
    }

    public void rotarIzquierda() {
        assamSvc.rotarIzquierda();
        setImage(assamSvc.getDir());
    }

    public void rotarDerecha() {
        assamSvc.rotarDerecha();
        setImage(assamSvc.getDir());
    }

    public void setDir(int dir) { assamSvc.setDir(dir); }
    public void setPosition(int x, int y) { assamSvc.setPosition(x, y); }

    public void setImage(int dir) {
        if (assamImages[dir] != null) {
            assamView.setImage(assamImages[dir]);
            assamView.setRotate(0);
        } else {
            assamView.setRotate(dir * 90);
        }
    }

    public void animarMovimiento(int pasos, Runnable onFinished) {
        int[][] path = assamSvc.computePath(pasos);
        Timeline anim = new Timeline();
        for (int p = 1; p <= pasos; p++) {
            final int px = path[p][0], py = path[p][1], pd = path[p][2];
            anim.getKeyFrames().add(new KeyFrame(Duration.millis(p * 280L), e -> {
                assamSvc.setPosition(px, py);
                assamSvc.setDir(pd);
                GridPane.setColumnIndex(assamView, assamSvc.getX());
                GridPane.setRowIndex(assamView, assamSvc.getY());
                setImage(assamSvc.getDir());
                assamView.toFront();
            }));
        }
        anim.getKeyFrames().add(new KeyFrame(Duration.millis(pasos * 280L + 60), e -> onFinished.run()));
        anim.play();
    }

    public void actualizarPosicionEnGrid() {
        GridPane.setColumnIndex(assamView, assamSvc.getX());
        GridPane.setRowIndex(assamView, assamSvc.getY());
        setImage(assamSvc.getDir());
        assamView.toFront();
    }

    public ImageView getView() { return assamView; }
    public int getX() { return assamSvc.getX(); }
    public int getY() { return assamSvc.getY(); }
    public int getDir() { return assamSvc.getDir(); }
    public AssamServicio getAssamSvc() { return assamSvc; }
}
