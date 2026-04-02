package com.marrakech.game.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

public class ReglasView extends StackPane {

    private Button btnVolver;

    public ReglasView() {
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
        VBox panel = new VBox(22);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(42, 52, 42, 52));
        panel.setMaxWidth(660);
        panel.setStyle(
            "-fx-background-color: rgba(10,4,0,0.88);" +
            "-fx-border-color: #8B6914;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#000000", 0.8));
        sombra.setRadius(30);
        panel.setEffect(sombra);

        Text titulo = new Text("REGLAS");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        titulo.setFill(Color.web("#D4A017"));

        TextFlow cuerpo = new TextFlow();
        cuerpo.setTextAlignment(TextAlignment.JUSTIFY);
        cuerpo.setMaxWidth(560);

        String[] parrafos = {
            "Marrakesh es un juego de mesa estratégico en el que los jugadores compiten por controlar el mercado colocando alfombras y gestionando su dinero.\n\n",
            "Cada jugador controla a un comerciante y, en su turno, lanza el dado para mover al personaje Assam por el tablero. La dirección y distancia del movimiento determinarán las interacciones con las alfombras.\n\n",
            "Si Assam termina su movimiento sobre una alfombra rival, el jugador debe pagar monedas según el tamaño del área conectada de ese color.\n\n",
            "El juego termina cuando se han colocado todas las alfombras. El ganador es el jugador con más monedas y alfombras visibles en el tablero."
        };

        for (String p : parrafos) {
            Text t = new Text(p);
            t.setFont(Font.font("Georgia", 14));
            t.setFill(Color.web("#D4B87A"));
            cuerpo.getChildren().add(t);
        }

        btnVolver = crearBoton("VOLVER");

        panel.getChildren().addAll(titulo, cuerpo, btnVolver);
        getChildren().add(panel);
    }

    private Button crearBoton(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(160);
        btn.setPrefHeight(42);
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

    public Button getBtnVolver() { return btnVolver; }
}