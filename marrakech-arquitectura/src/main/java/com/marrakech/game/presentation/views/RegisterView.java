package com.marrakech.game.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class RegisterView extends StackPane {

    private TextField campoApodo;
    private TextField campoCorreo;
    private PasswordField campoContrasena;
    private Button btnRegistrar;

    public RegisterView() {
        configurarFondo();
        configurarContenido();
    }

    private void configurarFondo() {
        Image imagen = new Image(getClass().getResourceAsStream("/images/background.jpg"));
        BackgroundImage bgImage = new BackgroundImage(imagen,
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true));
        setBackground(new Background(bgImage));
        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());
        getChildren().add(overlay);
    }

    private void configurarContenido() {
        VBox panel = new VBox(16);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(36, 40, 36, 40));
        panel.setMaxWidth(360);
        panel.setStyle(
            "-fx-background-color: rgba(30,10,0,0.72);" +
            "-fx-border-color: #8B6914;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;");
        DropShadow panelSombra = new DropShadow();
        panelSombra.setColor(Color.web("#000000", 0.7));
        panelSombra.setRadius(25);
        panel.setEffect(panelSombra);

        Text titulo = new Text("CREAR CUENTA");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        titulo.setFill(Color.web("#D4A017"));
        DropShadow st = new DropShadow();
        st.setColor(Color.web("#7A5000")); st.setRadius(6); st.setOffsetX(2); st.setOffsetY(2);
        titulo.setEffect(st);

        campoApodo      = crearCampoTexto("Apodo");
        campoCorreo     = crearCampoTexto("Correo");
        campoContrasena = crearCampoPassword("Contraseña");

        btnRegistrar = new Button("REGISTRAR");
        btnRegistrar.setMaxWidth(Double.MAX_VALUE);
        btnRegistrar.setPrefHeight(42);
        btnRegistrar.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String n = "-fx-background-color:#C9922A;-fx-text-fill:#1A0A00;-fx-border-color:#E8C97A;-fx-border-width:1;-fx-border-radius:3;-fx-background-radius:3;-fx-cursor:hand;";
        String h = "-fx-background-color:#E8A830;-fx-text-fill:#1A0A00;-fx-border-color:#F0D88A;-fx-border-width:1;-fx-border-radius:3;-fx-background-radius:3;-fx-cursor:hand;";
        btnRegistrar.setStyle(n);
        btnRegistrar.setOnMouseEntered(e -> btnRegistrar.setStyle(h));
        btnRegistrar.setOnMouseExited(e -> btnRegistrar.setStyle(n));

        panel.getChildren().addAll(titulo, campoApodo, campoCorreo, campoContrasena, btnRegistrar);
        getChildren().add(panel);
    }

    private TextField crearCampoTexto(String placeholder) {
        TextField campo = new TextField();
        campo.setPromptText(placeholder);
        campo.setPrefHeight(38);
        campo.setStyle("-fx-background-color:rgba(15,5,0,0.65);-fx-text-fill:#E8C97A;-fx-prompt-text-fill:#9E7A3A;-fx-border-color:#8B6914;-fx-border-width:1;-fx-border-radius:3;-fx-background-radius:3;-fx-font-size:13;");
        return campo;
    }

    private PasswordField crearCampoPassword(String placeholder) {
        PasswordField campo = new PasswordField();
        campo.setPromptText(placeholder);
        campo.setPrefHeight(38);
        campo.setStyle("-fx-background-color:rgba(15,5,0,0.65);-fx-text-fill:#E8C97A;-fx-prompt-text-fill:#9E7A3A;-fx-border-color:#8B6914;-fx-border-width:1;-fx-border-radius:3;-fx-background-radius:3;-fx-font-size:13;");
        return campo;
    }

    public TextField getCampoApodo()          { return campoApodo; }
    public TextField getCampoCorreo()         { return campoCorreo; }
    public PasswordField getCampoContrasena() { return campoContrasena; }
    public Button getBtnRegistrar()           { return btnRegistrar; }
}
