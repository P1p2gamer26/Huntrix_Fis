package com.marrakech.game.presentation.views;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class WelcomeView extends StackPane {

    private Button btnCrearCuenta;
    private Button btnYaTengoCuenta;
    private Button btnSalir;

    public WelcomeView() {
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
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());
        getChildren().add(overlay);
    }

    private void configurarContenido() {
        VBox contenido = new VBox(22);
        contenido.setAlignment(Pos.CENTER);
        contenido.setMaxWidth(380);

        Text titulo = new Text("ACCESO");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 64));
        titulo.setFill(Color.web("#D4A017"));
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#7A5000"));
        sombra.setRadius(10);
        sombra.setOffsetX(3);
        sombra.setOffsetY(4);
        titulo.setEffect(sombra);

        btnCrearCuenta = crearBotonRelleno("CREAR NUEVA CUENTA");
        btnYaTengoCuenta = crearBotonContorno("YA TENGO CUENTA");

        VBox sep = new VBox(4);
        sep.setAlignment(Pos.CENTER);
        Text linea = new Text("──────────────");
        linea.setFill(Color.web("#5C3A10"));
        linea.setFont(Font.font(12));
        sep.getChildren().add(linea);

        btnSalir = crearBotonContorno("SALIR");
        btnSalir.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #885533;" +
            "-fx-border-color: #5C3A10;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-cursor: hand;");
        btnSalir.setOnMouseEntered(e -> btnSalir.setStyle(
            "-fx-background-color: rgba(100,40,20,0.2);" +
            "-fx-text-fill: #AA6655;" +
            "-fx-border-color: #885533;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-cursor: hand;"));
        btnSalir.setOnMouseExited(e -> btnSalir.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #885533;" +
            "-fx-border-color: #5C3A10;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-cursor: hand;"));

        contenido.getChildren().addAll(titulo, btnCrearCuenta, btnYaTengoCuenta, sep, btnSalir);
        getChildren().add(contenido);
    }

    private Button crearBotonRelleno(String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(52);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String estiloNormal =
            "-fx-background-color: #C9922A;" +
            "-fx-text-fill: #1A0A00;" +
            "-fx-border-color: #E8C97A;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-cursor: hand;";
        String estiloHover =
            "-fx-background-color: #E8A830;" +
            "-fx-text-fill: #1A0A00;" +
            "-fx-border-color: #F0D88A;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-cursor: hand;";
        btn.setStyle(estiloNormal);
        btn.setOnMouseEntered(e -> btn.setStyle(estiloHover));
        btn.setOnMouseExited(e -> btn.setStyle(estiloNormal));
        return btn;
    }

    private Button crearBotonContorno(String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(52);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String estiloNormal =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #D4A017;" +
            "-fx-border-color: #C9922A;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-cursor: hand;";
        String estiloHover =
            "-fx-background-color: rgba(201,146,42,0.15);" +
            "-fx-text-fill: #E8C97A;" +
            "-fx-border-color: #E8C97A;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-cursor: hand;";
        btn.setStyle(estiloNormal);
        btn.setOnMouseEntered(e -> btn.setStyle(estiloHover));
        btn.setOnMouseExited(e -> btn.setStyle(estiloNormal));
        return btn;
    }

    public Button getBtnCrearCuenta()    { return btnCrearCuenta; }
    public Button getBtnYaTengoCuenta()  { return btnYaTengoCuenta; }
    public Button getBtnSalir()          { return btnSalir; }
}