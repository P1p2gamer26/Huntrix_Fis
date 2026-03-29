package com.marrakech.game.presentation.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class CrearPartidaView extends StackPane {

    private Button btnVolver;
    private Button btnCrear;
    private ComboBox<String> comboCantidad;
    private CheckBox checkPoderes;
    private CheckBox checkRapida;
    private ComboBox<String> comboDificultad;

    public CrearPartidaView() {
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
        VBox panel = new VBox(18);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(38, 52, 38, 52));
        panel.setMaxWidth(580);
        panel.setStyle(
            "-fx-background-color: rgba(10,4,0,0.90);" +
            "-fx-border-color: #8B6914;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#000000", 0.85)); sombra.setRadius(30);
        panel.setEffect(sombra);

        Text titulo = new Text("CREAR PARTIDA");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        titulo.setFill(Color.web("#D4A017"));

        VBox seccionCantidad = crearSeccion("Cantidad de jugadores");
        comboCantidad = new ComboBox<>();
        comboCantidad.getItems().addAll("2 jugadores", "3 jugadores", "4 jugadores");
        comboCantidad.setValue("2 jugadores");
        comboCantidad.setMaxWidth(Double.MAX_VALUE);
        aplicarEstiloCombo(comboCantidad);
        seccionCantidad.getChildren().add(comboCantidad);

        checkPoderes = crearCheckbox("Activar poderes especiales");
        checkRapida  = crearCheckbox("Partida rápida");

        VBox seccionDificultad = crearSeccion("Dificultad");
        comboDificultad = new ComboBox<>();
        comboDificultad.getItems().addAll("Normal", "Difícil", "Experto");
        comboDificultad.setValue("Normal");
        comboDificultad.setMaxWidth(Double.MAX_VALUE);
        aplicarEstiloCombo(comboDificultad);
        seccionDificultad.getChildren().add(comboDificultad);

        HBox botones = new HBox(16);
        botones.setAlignment(Pos.CENTER);
        btnVolver = crearBotonContorno("VOLVER");
        btnCrear  = crearBotonRelleno("CREAR");
        botones.getChildren().addAll(btnVolver, btnCrear);

        panel.getChildren().addAll(titulo, seccionCantidad, checkPoderes, checkRapida, seccionDificultad, botones);
        getChildren().add(panel);
    }

    private VBox crearSeccion(String etiqueta) {
        Label lbl = new Label(etiqueta);
        lbl.setFont(Font.font("Georgia", 14));
        lbl.setTextFill(Color.web("#D4B87A"));
        return new VBox(6, lbl);
    }

    private CheckBox crearCheckbox(String texto) {
        CheckBox cb = new CheckBox(texto);
        cb.setFont(Font.font("Georgia", 14));
        cb.setTextFill(Color.web("#D4B87A"));
        cb.setStyle("-fx-mark-color: #C9922A;");
        return cb;
    }

    private void aplicarEstiloCombo(ComboBox<?> combo) {
        combo.setStyle(
            "-fx-background-color: rgba(15,5,0,0.70);" +
            "-fx-border-color: #8B6914;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-text-fill: #D4B87A;" +
            "-fx-font-size: 13;"
        );
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

    private Button crearBotonContorno(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(150); btn.setPrefHeight(42);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String n = "-fx-background-color:transparent;-fx-text-fill:#D4A017;-fx-border-color:#8B6914;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        String h = "-fx-background-color:rgba(201,146,42,0.18);-fx-text-fill:#F0D060;-fx-border-color:#D4A017;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        btn.setStyle(n); btn.setOnMouseEntered(e->btn.setStyle(h)); btn.setOnMouseExited(e->btn.setStyle(n));
        return btn;
    }

    public Button getBtnVolver()        { return btnVolver; }
    public Button getBtnCrear()         { return btnCrear; }
    public boolean isPoderesActivados() { return checkPoderes.isSelected(); }
    public boolean isPartidaRapida()    { return checkRapida.isSelected(); }
    public String getDificultad()       { return comboDificultad.getValue(); }
    public int getCantidadJugadores()   { return Integer.parseInt(comboCantidad.getValue().charAt(0) + ""); }
}