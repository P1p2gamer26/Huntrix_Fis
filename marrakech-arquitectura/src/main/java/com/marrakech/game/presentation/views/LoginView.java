package com.marrakech.game.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class LoginView extends StackPane {

    private Button       btnEntrar;
    private Button       btnVolver;
    private TextField    campoApodo;
    private PasswordField campoContrasena;
    private Label        lblError;

    public LoginView() {
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
        overlay.setStyle("-fx-background-color:rgba(0,0,0,0.45);");
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());
        getChildren().add(overlay);
    }

    private void configurarContenido() {
        VBox panel = new VBox(16);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(36, 40, 36, 40));
        panel.setMaxWidth(360);
        panel.setStyle(
            "-fx-background-color:rgba(30,10,0,0.72);" +
            "-fx-border-color:#8B6914;-fx-border-width:1.5;" +
            "-fx-border-radius:6;-fx-background-radius:6;");
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#000000", 0.7)); sombra.setRadius(25);
        panel.setEffect(sombra);

        Text titulo = new Text("INICIAR SESIÓN");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        titulo.setFill(Color.web("#D4A017"));

        campoApodo      = crearCampoTexto("Apodo");
        campoContrasena = crearCampoPassword("Contraseña");

        lblError = new Label("");
        lblError.setWrapText(true);
        lblError.setMaxWidth(300);
        lblError.setVisible(false);
        lblError.setStyle(
            "-fx-text-fill:#FF6B6B;" +
            "-fx-font-size:12px;" +
            "-fx-background-color:rgba(200,50,50,0.18);" +
            "-fx-background-radius:4;" +
            "-fx-padding:6 10 6 10;");

        btnEntrar = crearBotonRelleno("ENTRAR");
        btnVolver = crearBotonContorno("VOLVER");

        panel.getChildren().addAll(titulo, campoApodo, campoContrasena,
                                   lblError, btnEntrar, btnVolver);
        getChildren().add(panel);
    }

    public boolean validarCampos() {
        String apodo = campoApodo.getText().trim();
        String pass  = campoContrasena.getText();
        if (apodo.isEmpty()) {
            mostrarError("Ingresa tu apodo.");
            return false;
        }
        if (pass.isEmpty()) {
            mostrarError("Ingresa tu contraseña.");
            return false;
        }
        return true;
    }

    public void mostrarError(String mensaje) {
        lblError.setText("⚠  " + mensaje);
        lblError.setVisible(true);
    }

    public void limpiarError() {
        lblError.setVisible(false);
    }

    private TextField crearCampoTexto(String placeholder) {
        TextField campo = new TextField();
        campo.setPromptText(placeholder);
        campo.setPrefHeight(38);
        campo.setStyle(
            "-fx-background-color:rgba(15,5,0,0.65);" +
            "-fx-text-fill:#E8C97A;" +
            "-fx-prompt-text-fill:#9E7A3A;" +
            "-fx-border-color:#8B6914;" +
            "-fx-border-width:1;" +
            "-fx-border-radius:3;" +
            "-fx-background-radius:3;" +
            "-fx-font-size:13;");
        return campo;
    }

    private PasswordField crearCampoPassword(String placeholder) {
        PasswordField campo = new PasswordField();
        campo.setPromptText(placeholder);
        campo.setPrefHeight(38);
        campo.setStyle(
            "-fx-background-color:rgba(15,5,0,0.65);" +
            "-fx-text-fill:#E8C97A;" +
            "-fx-prompt-text-fill:#9E7A3A;" +
            "-fx-border-color:#8B6914;" +
            "-fx-border-width:1;" +
            "-fx-border-radius:3;" +
            "-fx-background-radius:3;" +
            "-fx-font-size:13;");
        return campo;
    }

    private Button crearBotonRelleno(String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE); btn.setPrefHeight(42);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String n = "-fx-background-color:#C9922A;-fx-text-fill:#1A0A00;-fx-border-color:#E8C97A;-fx-border-width:1;-fx-border-radius:3;-fx-background-radius:3;-fx-cursor:hand;";
        String h = "-fx-background-color:#E8A830;-fx-text-fill:#1A0A00;-fx-border-color:#F0D88A;-fx-border-width:1;-fx-border-radius:3;-fx-background-radius:3;-fx-cursor:hand;";
        btn.setStyle(n); btn.setOnMouseEntered(e->btn.setStyle(h)); btn.setOnMouseExited(e->btn.setStyle(n));
        return btn;
    }

    private Button crearBotonContorno(String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE); btn.setPrefHeight(42);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String n = "-fx-background-color:transparent;-fx-text-fill:#D4A017;-fx-border-color:#C9922A;-fx-border-width:2;-fx-border-radius:3;-fx-background-radius:3;-fx-cursor:hand;";
        String h = "-fx-background-color:rgba(201,146,42,0.15);-fx-text-fill:#E8C97A;-fx-border-color:#E8C97A;-fx-border-width:2;-fx-border-radius:3;-fx-background-radius:3;-fx-cursor:hand;";
        btn.setStyle(n); btn.setOnMouseEntered(e->btn.setStyle(h)); btn.setOnMouseExited(e->btn.setStyle(n));
        return btn;
    }

    public Button        getBtnEntrar()        { return btnEntrar; }
    public Button        getBtnVolver()        { return btnVolver; }
    public TextField     getCampoApodo()       { return campoApodo; }
    public PasswordField getCampoContrasena()  { return campoContrasena; }
}