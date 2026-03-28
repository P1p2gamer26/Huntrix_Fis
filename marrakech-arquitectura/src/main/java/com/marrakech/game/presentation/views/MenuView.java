package com.marrakech.game.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class MenuView extends StackPane {

    private Button btnJugar;
    private Button btnReglas;
    private Button btnConfiguracion;

    public MenuView() {
        configurarFondo();
        configurarContenido();
    }

    private void configurarFondo() {
        Image imagen = new Image(
            getClass().getResourceAsStream("/images/background.jpg")
        );
        BackgroundImage bgImage = new BackgroundImage(
            imagen,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );
        setBackground(new Background(bgImage));

        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.62);");
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());
        getChildren().add(overlay);
    }

    private void configurarContenido() {
        VBox contenido = new VBox(18);
        contenido.setAlignment(Pos.CENTER);
        contenido.setMaxWidth(320);
        contenido.setPadding(new Insets(0, 0, 60, 0));

        Text titulo = new Text("MARRAKESH");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 72));
        titulo.setFill(Color.web("#D4A017"));
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#7A4500"));
        sombra.setRadius(14);
        sombra.setOffsetX(4);
        sombra.setOffsetY(4);
        Glow glow = new Glow(0.3);
        glow.setInput(sombra);
        titulo.setEffect(glow);

        Text subtitulo = new Text("JUEGO DE MESA DIGITAL  ·  VERSIÓN ALFA");
        subtitulo.setFont(Font.font("Georgia", 12));
        subtitulo.setFill(Color.web("#9E7A3A"));

        btnJugar        = crearBoton("JUGAR");
        btnReglas       = crearBoton("REGLAS");
        btnConfiguracion = crearBoton("CONFIGURACIÓN");

        VBox botones = new VBox(14);
        botones.setAlignment(Pos.CENTER);
        botones.getChildren().addAll(btnJugar, btnReglas, btnConfiguracion);

        contenido.getChildren().addAll(titulo, botones, subtitulo);
        getChildren().add(contenido);
    }

    private Button crearBoton(String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(46);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String normal =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #D4A017;" +
            "-fx-border-color: #8B6914;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;";
        String hover =
            "-fx-background-color: rgba(201,146,42,0.18);" +
            "-fx-text-fill: #F0D060;" +
            "-fx-border-color: #D4A017;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    public Button getBtnJugar()         { return btnJugar; }
    public Button getBtnReglas()        { return btnReglas; }
    public Button getBtnConfiguracion() { return btnConfiguracion; }
}