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

public class RegisterView extends StackPane {

    private Button    btnRegistrar;
    private Button    btnVolver;
    private TextField campoApodo;
    private TextField campoCorreo;
    private PasswordField campoContrasena;
    private Label     lblError;

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
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());
        getChildren().add(overlay);
    }

    private void configurarContenido() {
        VBox panel = new VBox(16);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(36, 40, 36, 40));
        panel.setMaxWidth(380);
        panel.setStyle(
            "-fx-background-color:rgba(30,10,0,0.72);" +
            "-fx-border-color:#8B6914;-fx-border-width:1.5;" +
            "-fx-border-radius:6;-fx-background-radius:6;");
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#000000", 0.7)); sombra.setRadius(25);
        panel.setEffect(sombra);

        Text titulo = new Text("CREAR CUENTA");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        titulo.setFill(Color.web("#D4A017"));

        campoApodo       = crearCampoTexto("Apodo (mín. 3 caracteres)");
        campoCorreo      = crearCampoTexto("Correo (ej: usuario@ejemplo.com)");
        campoContrasena  = crearCampoPassword("Contraseña (mín. 6 caracteres)");

        // Label de error — visible solo cuando hay un problema
        lblError = new Label("");
        lblError.setWrapText(true);
        lblError.setMaxWidth(320);
        lblError.setVisible(false);
        lblError.setStyle(
            "-fx-text-fill:#FF6B6B;" +
            "-fx-font-size:12px;" +
            "-fx-background-color:rgba(200,50,50,0.18);" +
            "-fx-background-radius:4;" +
            "-fx-padding:6 10 6 10;");

        btnRegistrar = crearBotonRelleno("REGISTRAR");
        btnVolver    = crearBotonContorno("VOLVER");

        // Validación en tiempo real al salir de cada campo
        campoApodo.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) validarApodo();
        });
        campoCorreo.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) validarCorreo();
        });
        campoContrasena.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) validarContrasena();
        });

        panel.getChildren().addAll(titulo, campoApodo, campoCorreo, campoContrasena,
                                   lblError, btnRegistrar, btnVolver);
        getChildren().add(panel);
    }

    // ── Validaciones ──────────────────────────────────────────────────────────

    private boolean validarApodo() {
        String apodo = campoApodo.getText().trim();
        if (apodo.isEmpty()) {
            mostrarError("El apodo no puede estar vacío.");
            marcarCampoError(campoApodo); return false;
        }
        if (apodo.length() < 3) {
            mostrarError("El apodo debe tener al menos 3 caracteres.");
            marcarCampoError(campoApodo); return false;
        }
        if (!apodo.matches("[a-zA-Z0-9_]+")) {
            mostrarError("El apodo solo puede contener letras, números y guiones bajos.");
            marcarCampoError(campoApodo); return false;
        }
        limpiarError(campoApodo); return true;
    }

    private boolean validarCorreo() {
        String correo = campoCorreo.getText().trim();
        if (correo.isEmpty()) {
            mostrarError("El correo no puede estar vacío.");
            marcarCampoError(campoCorreo); return false;
        }
        if (!correo.contains("@")) {
            mostrarError("El correo debe contener '@'. Ejemplo: usuario@ejemplo.com");
            marcarCampoError(campoCorreo); return false;
        }
        String[] partes = correo.split("@");
        if (partes.length != 2 || partes[1].isEmpty() || !partes[1].contains(".")) {
            mostrarError("El correo debe tener un dominio válido con '.'. Ejemplo: usuario@ejemplo.com");
            marcarCampoError(campoCorreo); return false;
        }
        limpiarError(campoCorreo); return true;
    }

    private boolean validarContrasena() {
        String pass = campoContrasena.getText();
        if (pass.isEmpty()) {
            mostrarError("La contraseña no puede estar vacía.");
            marcarCampoError(campoContrasena); return false;
        }
        if (pass.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres.");
            marcarCampoError(campoContrasena); return false;
        }
        limpiarError(campoContrasena); return true;
    }

    public boolean validarTodo() {
        boolean a = validarApodo();
        boolean b = validarCorreo();
        boolean c = validarContrasena();
        return a && b && c;
    }

    public void mostrarError(String mensaje) {
        lblError.setText("⚠  " + mensaje);
        lblError.setVisible(true);
    }

    public void limpiarErrorGlobal() {
        lblError.setVisible(false);
    }

    private void marcarCampoError(Control campo) {
        campo.setStyle(campo.getStyle() +
            "-fx-border-color:#FF6B6B;-fx-border-width:2;");
    }

    private void limpiarError(Control campo) {
        aplicarEstiloCampo(campo);
        if (lblError.isVisible() && !lblError.getText().isEmpty()) {
            lblError.setVisible(false);
        }
    }

    private void aplicarEstiloCampo(Control campo) {
        campo.setStyle(
            "-fx-background-color:rgba(15,5,0,0.65);" +
            "-fx-text-fill:#E8C97A;" +
            "-fx-prompt-text-fill:#9E7A3A;" +
            "-fx-border-color:#8B6914;" +
            "-fx-border-width:1;" +
            "-fx-border-radius:3;" +
            "-fx-background-radius:3;" +
            "-fx-font-size:13;");
    }

    // ── Helpers de UI ─────────────────────────────────────────────────────────

    private TextField crearCampoTexto(String placeholder) {
        TextField campo = new TextField();
        campo.setPromptText(placeholder);
        campo.setPrefHeight(38);
        aplicarEstiloCampo(campo);
        return campo;
    }

    private PasswordField crearCampoPassword(String placeholder) {
        PasswordField campo = new PasswordField();
        campo.setPromptText(placeholder);
        campo.setPrefHeight(38);
        aplicarEstiloCampo(campo);
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

    public Button    getBtnRegistrar()   { return btnRegistrar; }
    public Button    getBtnVolver()      { return btnVolver; }
    public TextField getCampoApodo()     { return campoApodo; }
    public TextField getCampoCorreo()    { return campoCorreo; }
    public PasswordField getCampoContrasena() { return campoContrasena; }
}