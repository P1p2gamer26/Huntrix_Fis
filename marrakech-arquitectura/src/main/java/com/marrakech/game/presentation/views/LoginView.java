package com.marrakech.game.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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

public class LoginView extends StackPane {

    private TextField campoApodo;
    private PasswordField campoContrasena;
    private Button btnEntrar;

    public LoginView() {
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
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());
        getChildren().add(overlay);
    }

    private void configurarContenido() {
        // Panel central semitransparente
        VBox panel = new VBox(16);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(36, 40, 36, 40));
        panel.setMaxWidth(360);
        panel.setStyle(
            "-fx-background-color: rgba(30,10,0,0.72);" +
            "-fx-border-color: #8B6914;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        );
        DropShadow panelSombra = new DropShadow();
        panelSombra.setColor(Color.web("#000000", 0.7));
        panelSombra.setRadius(25);
        panel.setEffect(panelSombra);

        // Título
        Text titulo = new Text("INICIAR SESIÓN");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        titulo.setFill(Color.web("#D4A017"));
        DropShadow sombraTitulo = new DropShadow();
        sombraTitulo.setColor(Color.web("#7A5000"));
        sombraTitulo.setRadius(6);
        sombraTitulo.setOffsetX(2);
        sombraTitulo.setOffsetY(2);
        titulo.setEffect(sombraTitulo);

        // Campos
        campoApodo = crearCampoTexto("Apodo");
        campoContrasena = crearCampoPassword("Contraseña");

        // Botón Entrar
        btnEntrar = new Button("ENTRAR");
        btnEntrar.setMaxWidth(Double.MAX_VALUE);
        btnEntrar.setPrefHeight(42);
        btnEntrar.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String estiloNormal =
            "-fx-background-color: #C9922A;" +
            "-fx-text-fill: #1A0A00;" +
            "-fx-border-color: #E8C97A;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-cursor: hand;";
        String estiloHover =
            "-fx-background-color: #E8A830;" +
            "-fx-text-fill: #1A0A00;" +
            "-fx-border-color: #F0D88A;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-cursor: hand;";
        btnEntrar.setStyle(estiloNormal);
        btnEntrar.setOnMouseEntered(e -> btnEntrar.setStyle(estiloHover));
        btnEntrar.setOnMouseExited(e -> btnEntrar.setStyle(estiloNormal));

        panel.getChildren().addAll(titulo, campoApodo, campoContrasena, btnEntrar);
        getChildren().add(panel);
    }

    private TextField crearCampoTexto(String placeholder) {
        TextField campo = new TextField();
        campo.setPromptText(placeholder);
        campo.setPrefHeight(38);
        campo.setStyle(
            "-fx-background-color: rgba(15,5,0,0.65);" +
            "-fx-text-fill: #E8C97A;" +
            "-fx-prompt-text-fill: #9E7A3A;" +
            "-fx-border-color: #8B6914;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-font-size: 13;"
        );
        return campo;
    }

    private PasswordField crearCampoPassword(String placeholder) {
        PasswordField campo = new PasswordField();
        campo.setPromptText(placeholder);
        campo.setPrefHeight(38);
        campo.setStyle(
            "-fx-background-color: rgba(15,5,0,0.65);" +
            "-fx-text-fill: #E8C97A;" +
            "-fx-prompt-text-fill: #9E7A3A;" +
            "-fx-border-color: #8B6914;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-font-size: 13;"
        );
        return campo;
    }

    public TextField getCampoApodo()          { return campoApodo; }
    public PasswordField getCampoContrasena() { return campoContrasena; }
    public Button getBtnEntrar()              { return btnEntrar; }
}