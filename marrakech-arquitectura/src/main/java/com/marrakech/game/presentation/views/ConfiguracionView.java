package com.marrakech.game.presentation.views;

import com.marrakech.game.presentation.MusicaManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class ConfiguracionView extends StackPane {

    private Button btnVolver;
    private Button btnGuardar;

    public ConfiguracionView() {
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
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.62);");
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());
        getChildren().add(overlay);
    }

    private void configurarContenido() {
        VBox panel = new VBox(18);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(42, 52, 42, 52));
        panel.setMaxWidth(620);
        panel.setStyle(
            "-fx-background-color: rgba(10,4,0,0.88);" +
            "-fx-border-color: #8B6914;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#000000", 0.8)); sombra.setRadius(30);
        panel.setEffect(sombra);

        Text titulo = new Text("CONFIGURACIÓN");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        titulo.setFill(Color.web("#D4A017"));

        VBox opciones = new VBox(16);
        opciones.setMaxWidth(560);
        opciones.getChildren().addAll(
            crearSliderVolumen(),
            crearSlider("Brillo", 0, 100, 45),
            crearSlider("Tamaño de interfaz", 0, 100, 89),
            crearCombo("Resolución", new String[]{"1920 × 1080", "1280 × 720", "2560 × 1440"}),
            crearCombo("Modo de pantalla", new String[]{"Pantalla completa", "Ventana", "Sin bordes"})
        );

        HBox botones = new HBox(16);
        botones.setAlignment(Pos.CENTER);
        btnVolver  = crearBotonContorno("VOLVER");
        btnGuardar = crearBotonRelleno("GUARDAR");
        botones.getChildren().addAll(btnVolver, btnGuardar);

        panel.getChildren().addAll(titulo, opciones, botones);
        getChildren().add(panel);
    }

    private VBox crearSliderVolumen() {
        Label lblNombre = new Label("🔊  Volumen general");
        lblNombre.setFont(Font.font("Georgia", 14));
        lblNombre.setTextFill(Color.web("#D4B87A"));

        double volumenActual = MusicaManager.getInstance().getVolumen() * 100;

        Label lblValor = new Label((int) volumenActual + "%");
        lblValor.setFont(Font.font("Georgia", 14));
        lblValor.setTextFill(Color.web("#D4B87A"));
        lblValor.setMinWidth(42);

        HBox cabecera = new HBox();
        cabecera.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(lblNombre, Priority.ALWAYS);
        cabecera.getChildren().addAll(lblNombre, lblValor);

        Slider slider = new Slider(0, 100, volumenActual);
        slider.setMaxWidth(Double.MAX_VALUE);
        slider.setStyle("-fx-control-inner-background: #2A1800; -fx-accent: #C9922A;");
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int pct = newVal.intValue();
            lblValor.setText(pct + "%");
            MusicaManager.getInstance().setVolumen(pct / 100.0);
        });

        return new VBox(4, cabecera, slider);
    }

    private VBox crearSlider(String etiqueta, double min, double max, double valor) {
        Label lblNombre = new Label(etiqueta);
        lblNombre.setFont(Font.font("Georgia", 14));
        lblNombre.setTextFill(Color.web("#D4B87A"));

        Label lblValor = new Label((int) valor + "%");
        lblValor.setFont(Font.font("Georgia", 14));
        lblValor.setTextFill(Color.web("#D4B87A"));
        lblValor.setMinWidth(42);

        HBox cabecera = new HBox();
        cabecera.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(lblNombre, Priority.ALWAYS);
        cabecera.getChildren().addAll(lblNombre, lblValor);

        Slider slider = new Slider(min, max, valor);
        slider.setMaxWidth(Double.MAX_VALUE);
        slider.setStyle("-fx-control-inner-background: #2A1800; -fx-accent: #C9922A;");
        slider.valueProperty().addListener((obs, oldVal, newVal) ->
            lblValor.setText((int) newVal.doubleValue() + "%"));

        return new VBox(4, cabecera, slider);
    }

    private VBox crearCombo(String etiqueta, String[] opciones) {
        Label lbl = new Label(etiqueta);
        lbl.setFont(Font.font("Georgia", 14));
        lbl.setTextFill(Color.web("#D4B87A"));

        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(opciones);
        combo.setValue(opciones[0]);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle(
            "-fx-background-color: #1A0A00;" +
            "-fx-border-color: #8B6914;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-text-fill: #D4B87A;" +
            "-fx-font-size: 13;"
        );
        return new VBox(4, lbl, combo);
    }

    private Button crearBotonContorno(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(150); btn.setPrefHeight(42);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String n = "-fx-background-color:transparent;-fx-text-fill:#D4A017;-fx-border-color:#8B6914;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        String h = "-fx-background-color:rgba(201,146,42,0.18);-fx-text-fill:#F0D060;-fx-border-color:#D4A017;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        btn.setStyle(n); btn.setOnMouseEntered(e->btn.setStyle(h)); btn.setOnMouseExited(e->btn.setStyle(n));
        return btn;
    }

    private Button crearBotonRelleno(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(150); btn.setPrefHeight(42);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String n = "-fx-background-color:#C9922A;-fx-text-fill:#1A0A00;-fx-border-color:#E8C97A;-fx-border-width:1;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        String h = "-fx-background-color:#E8A830;-fx-text-fill:#1A0A00;-fx-border-color:#F0D88A;-fx-border-width:1;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        btn.setStyle(n); btn.setOnMouseEntered(e->btn.setStyle(h)); btn.setOnMouseExited(e->btn.setStyle(n));
        return btn;
    }

    public Button getBtnVolver()  { return btnVolver; }
    public Button getBtnGuardar() { return btnGuardar; }
}