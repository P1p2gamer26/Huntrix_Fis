package com.marrakech.game.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class PerfilView extends StackPane {

    private Button btnVolver;

    private final String nombreUsuario;
    private final String correo;
    private final String estado;
    private final String fechaRegistro;
    private final int    victorias;

    public PerfilView(String nombreUsuario, String correo, String estado,
                      String fechaRegistro, int victorias) {
        this.nombreUsuario = nombreUsuario;
        this.correo        = correo;
        this.estado        = estado;
        this.fechaRegistro = fechaRegistro;
        this.victorias     = victorias;
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
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.68);");
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());
        getChildren().add(overlay);
    }

    private void configurarContenido() {
        VBox panel = new VBox(22);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(40, 52, 40, 52));
        panel.setMaxWidth(520);
        panel.setStyle(
            "-fx-background-color: rgba(10,4,0,0.92);" +
            "-fx-border-color: #8B6914;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#000000", 0.85)); sombra.setRadius(30);
        panel.setEffect(sombra);

        panel.getChildren().addAll(
            construirAvatar(),
            crearSeparador(),
            construirDatos(),
            btnVolver = crearBotonContorno("VOLVER AL MENÚ")
        );
        getChildren().add(panel);
    }

    private VBox construirAvatar() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);

        StackPane avatarContainer = new StackPane();
        Circle circulo = new Circle(48);
        circulo.setFill(Color.web("#1A0800"));
        circulo.setStroke(Color.web("#C9922A"));
        circulo.setStrokeWidth(2.5);
        Text icono = new Text("👤");
        icono.setFont(Font.font(42));
        avatarContainer.getChildren().addAll(circulo, icono);

        Text nombre = new Text(nombreUsuario);
        nombre.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        nombre.setFill(Color.web("#D4A017"));

        boolean activo = estado != null && estado.equalsIgnoreCase("Activo");
        Text etEstado = new Text("● " + (estado == null || estado.isEmpty() ? "Activo" : estado));
        etEstado.setFont(Font.font("Georgia", 13));
        etEstado.setFill(activo ? Color.web("#2ecc71") : Color.web("#e74c3c"));

        box.getChildren().addAll(avatarContainer, nombre, etEstado);
        return box;
    }

    private Separator crearSeparador() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #4A3000;");
        return sep;
    }

    private VBox construirDatos() {
        VBox datos = new VBox(14);
        datos.setMaxWidth(Double.MAX_VALUE);
        datos.getChildren().addAll(
            crearCampo("Nombre de usuario", nombreUsuario),
            crearCampo("Correo",            correo == null || correo.isEmpty() ? "—" : correo),
            crearCampo("Fecha de registro", fechaRegistro == null || fechaRegistro.isEmpty() ? "—" : fechaRegistro),
            crearCampo("Victorias",         String.valueOf(victorias))
        );
        return datos;
    }

    private VBox crearCampo(String etiqueta, String valor) {
        Label lbl = new Label(etiqueta);
        lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web("#9E7A3A"));

        Label val = new Label(valor);
        val.setFont(Font.font("Georgia", 15));
        val.setTextFill(Color.web("#E8D090"));
        val.setPadding(new Insets(7, 14, 7, 14));
        val.setMaxWidth(Double.MAX_VALUE);
        val.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04);" +
            "-fx-border-color: #4A3000;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;"
        );
        return new VBox(4, lbl, val);
    }

    private Button crearBotonContorno(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(200); btn.setPrefHeight(42);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String n = "-fx-background-color:transparent;-fx-text-fill:#D4A017;-fx-border-color:#8B6914;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        String h = "-fx-background-color:rgba(201,146,42,0.18);-fx-text-fill:#F0D060;-fx-border-color:#D4A017;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        btn.setStyle(n); btn.setOnMouseEntered(e->btn.setStyle(h)); btn.setOnMouseExited(e->btn.setStyle(n));
        return btn;
    }

    public Button getBtnVolver() { return btnVolver; }
}