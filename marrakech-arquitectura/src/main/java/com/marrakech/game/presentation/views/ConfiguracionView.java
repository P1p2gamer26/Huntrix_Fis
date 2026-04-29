package com.marrakech.game.presentation.views;

import com.marrakech.game.presentation.MusicaManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
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
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ConfiguracionView extends StackPane {

    private Button btnVolver;
    private Button btnGuardar;

    // Valores actuales para aplicar al guardar
    private double brilloActual     = 0.0;
    private String resolucionActual = "1920 × 1080";
    private String modoPantallaActual = "Pantalla completa";

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
        overlay.setStyle("-fx-background-color:rgba(0,0,0,0.62);");
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
            "-fx-background-color:rgba(10,4,0,0.88);" +
            "-fx-border-color:#8B6914;-fx-border-width:1.5;" +
            "-fx-border-radius:8;-fx-background-radius:8;");
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#000000", 0.8)); sombra.setRadius(30);
        panel.setEffect(sombra);

        Text titulo = new Text("CONFIGURACIÓN");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        titulo.setFill(Color.web("#D4A017"));

        VBox opciones = new VBox(20);
        opciones.setMaxWidth(560);
        opciones.getChildren().addAll(
            crearSliderVolumen(),
            crearSliderBrillo(),
            crearComboResolucion(),
            crearComboModoPantalla()
        );

        HBox botones = new HBox(16);
        botones.setAlignment(Pos.CENTER);
        btnVolver  = crearBotonContorno("VOLVER");
        btnGuardar = crearBotonRelleno("GUARDAR");

        btnGuardar.setOnAction(e -> aplicarConfiguracion());

        botones.getChildren().addAll(btnVolver, btnGuardar);
        panel.getChildren().addAll(titulo, opciones, botones);
        getChildren().add(panel);
    }

    // ── Slider de volumen ─────────────────────────────────────────────────────

    private VBox crearSliderVolumen() {
        Label lblNombre = new Label("🔊  Volumen general");
        lblNombre.setFont(Font.font("Georgia", 14));
        lblNombre.setTextFill(Color.web("#D4B87A"));

        double volumenActual = MusicaManager.getInstance().getVolumen() * 100;
        Label lblValor = new Label((int) volumenActual + "%");
        lblValor.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        lblValor.setTextFill(Color.web("#D4A017"));
        lblValor.setMinWidth(46);

        HBox cabecera = new HBox();
        cabecera.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(lblNombre, Priority.ALWAYS);
        cabecera.getChildren().addAll(lblNombre, lblValor);

        Slider slider = new Slider(0, 100, volumenActual);
        slider.setMaxWidth(Double.MAX_VALUE);
        estilizarSlider(slider);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblValor.setText(newVal.intValue() + "%");
            MusicaManager.getInstance().setVolumen(newVal.doubleValue() / 100.0);
        });

        return new VBox(6, cabecera, slider);
    }

    // ── Slider de brillo ──────────────────────────────────────────────────────

    private VBox crearSliderBrillo() {
        Label lblNombre = new Label("☀  Brillo");
        lblNombre.setFont(Font.font("Georgia", 14));
        lblNombre.setTextFill(Color.web("#D4B87A"));

        Label lblValor = new Label("50%");
        lblValor.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        lblValor.setTextFill(Color.web("#D4A017"));
        lblValor.setMinWidth(46);

        HBox cabecera = new HBox();
        cabecera.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(lblNombre, Priority.ALWAYS);
        cabecera.getChildren().addAll(lblNombre, lblValor);

        Slider slider = new Slider(0, 100, 50);
        slider.setMaxWidth(Double.MAX_VALUE);
        estilizarSlider(slider);
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int pct = newVal.intValue();
            lblValor.setText(pct + "%");
            // Brillo: 0% = muy oscuro (-0.8), 50% = normal (0.0), 100% = muy brillante (0.5)
            brilloActual = (pct / 100.0) * 1.3 - 0.8;
            aplicarBrillo();
        });

        return new VBox(6, cabecera, slider);
    }

    private void aplicarBrillo() {
        Platform.runLater(() -> {
            Scene scene = getScene();
            if (scene == null) return;
            ColorAdjust ajuste = new ColorAdjust();
            ajuste.setBrightness(Math.max(-1.0, Math.min(1.0, brilloActual)));
            scene.getRoot().setEffect(ajuste);
        });
    }

    // ── Combo resolución ──────────────────────────────────────────────────────

    private VBox crearComboResolucion() {
        Label lbl = new Label("📐  Resolución");
        lbl.setFont(Font.font("Georgia", 14));
        lbl.setTextFill(Color.web("#D4B87A"));

        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("1920 × 1080", "1280 × 720", "1600 × 900", "2560 × 1440");
        combo.setValue(resolucionActual);
        combo.setMaxWidth(Double.MAX_VALUE);
        estilizarCombo(combo);
        combo.setOnAction(e -> resolucionActual = combo.getValue());

        Label hint = new Label("Se aplica al guardar");
        hint.setFont(Font.font("Georgia", 10));
        hint.setTextFill(Color.web("#6A4A1A"));

        return new VBox(6, lbl, combo, hint);
    }

    // ── Combo modo pantalla ───────────────────────────────────────────────────

    private VBox crearComboModoPantalla() {
        Label lbl = new Label("🖥  Modo de pantalla");
        lbl.setFont(Font.font("Georgia", 14));
        lbl.setTextFill(Color.web("#D4B87A"));

        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(
            "Pantalla completa",
            "Ventana",
            "Sin bordes (modo cine)"
        );
        combo.setValue(modoPantallaActual);
        combo.setMaxWidth(Double.MAX_VALUE);
        estilizarCombo(combo);
        combo.setOnAction(e -> modoPantallaActual = combo.getValue());

        Label hint = new Label("Se aplica al guardar");
        hint.setFont(Font.font("Georgia", 10));
        hint.setTextFill(Color.web("#6A4A1A"));

        return new VBox(6, lbl, combo, hint);
    }

    // ── Aplicar configuración al pulsar GUARDAR ───────────────────────────────

    private void aplicarConfiguracion() {
        Scene scene = getScene();
        if (scene == null) return;
        Stage stage = (Stage) scene.getWindow();
        if (stage == null) return;

        switch (modoPantallaActual) {
            case "Pantalla completa":
                stage.setFullScreen(false);
                stage.setMaximized(true);
                break;
            case "Ventana":
                stage.setFullScreen(false);
                stage.setMaximized(false);
                aplicarResolucion(stage);
                stage.centerOnScreen();
                break;
            case "Sin bordes (modo cine)":
                stage.setMaximized(false);
                stage.setFullScreen(true);
                break;
        }
    }

    private void aplicarResolucion(Stage stage) {
        try {
            String[] partes = resolucionActual.replace(" ", "").split("×");
            double w = Double.parseDouble(partes[0]);
            double h = Double.parseDouble(partes[1]);
            // Limitar a la pantalla disponible
            Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
            w = Math.min(w, pantalla.getWidth());
            h = Math.min(h, pantalla.getHeight());
            stage.setWidth(w);
            stage.setHeight(h);
        } catch (Exception e) { /* ignorar resoluciones inválidas */ }
    }

    // ── Helpers de estilo ─────────────────────────────────────────────────────

    private void estilizarSlider(Slider slider) {
        slider.setStyle("-fx-control-inner-background:#2A1800;-fx-accent:#C9922A;");
    }

    private void estilizarCombo(ComboBox<?> combo) {
        combo.setStyle(
            "-fx-background-color:#1A0A00;-fx-border-color:#8B6914;" +
            "-fx-border-radius:3;-fx-background-radius:3;" +
            "-fx-text-fill:#D4B87A;-fx-font-size:13;");
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