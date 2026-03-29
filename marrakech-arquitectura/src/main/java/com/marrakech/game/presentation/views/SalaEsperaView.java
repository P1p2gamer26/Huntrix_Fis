package com.marrakech.game.presentation.views;

import com.marrakech.game.infrastructure.PartidaRepository.Partida;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class SalaEsperaView extends StackPane {

    private Button btnIniciar;
    private final Partida partida;

    public SalaEsperaView(Partida partida) {
        this.partida = partida;
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
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.72);");
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());
        getChildren().add(overlay);
    }

    private void configurarContenido() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(36, 48, 36, 48));
        panel.setMaxWidth(720);
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

        Text titulo = new Text(partida.nombre.toUpperCase());
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        titulo.setFill(Color.web("#D4A017"));

        VBox infoPartida = new VBox(6);
        infoPartida.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04);" +
            "-fx-border-color: #4A3000;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;"
        );
        infoPartida.setPadding(new Insets(14, 18, 14, 18));

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(6);

        String[][] datos = {
            {"Código:", partida.id,
             "Modo:", "Competitivo"},
            {"Complejidad:", partida.dificultad,
             "Poderes:", partida.poderesActivados ? "Activados" : "Desactivados"},
            {"Tiempo por turno:", partida.partidaRapida ? "20s" : "45s",
             "Rondas máximas:", "20"},
            {"Jugadores:", partida.jugadores.size() + "/" + partida.maxJugadores,
             "Estado:", partida.jugadores.size() < partida.maxJugadores ? "Esperando jugadores" : "Sala llena"},
        };

        for (int row = 0; row < datos.length; row++) {
            for (int col = 0; col < datos[row].length; col++) {
                Label lbl = new Label(datos[row][col]);
                boolean esEtiqueta = col % 2 == 0;
                lbl.setFont(Font.font("Georgia", esEtiqueta ? FontWeight.BOLD : FontWeight.NORMAL, 13));
                lbl.setTextFill(esEtiqueta ? Color.web("#D4B87A") : Color.web("#E8D090"));
                grid.add(lbl, col, row);
            }
        }
        infoPartida.getChildren().add(grid);

        Text tituloJugadores = new Text("Jugadores en sala");
        tituloJugadores.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        tituloJugadores.setFill(Color.web("#C9922A"));

        VBox listaJugadores = new VBox(10);
        String[] colores = {"#e74c3c", "#3498db", "#2ecc71", "#f39c12"};

        for (int i = 0; i < partida.jugadores.size(); i++) {
            String nombre = partida.jugadores.get(i);
            VBox tarjeta = new VBox(6);
            tarjeta.setPadding(new Insets(12, 16, 12, 16));
            tarjeta.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05);" +
                "-fx-border-color: " + colores[i % colores.length] + "55;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;"
            );

            HBox cabecera = new HBox();
            cabecera.setAlignment(Pos.CENTER_LEFT);

            Text nombreText = new Text(nombre + (i == 0 ? " [HOST]" : ""));
            nombreText.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
            nombreText.setFill(Color.web(i == 0 ? "#D4A017" : "#D4B87A"));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label estadoLbl = new Label("● Listo");
            estadoLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
            estadoLbl.setTextFill(Color.web("#2ecc71"));

            cabecera.getChildren().addAll(nombreText, spacer, estadoLbl);

            Label detalles = new Label(
                "Poder especial: " + (partida.poderesActivados ? "Sí" : "No")
            );
            detalles.setFont(Font.font("Georgia", 12));
            detalles.setTextFill(Color.web("#9E7A3A"));

            tarjeta.getChildren().addAll(cabecera, detalles);
            listaJugadores.getChildren().add(tarjeta);
        }

        btnIniciar = new Button("INICIAR PARTIDA");
        btnIniciar.setMaxWidth(Double.MAX_VALUE);
        btnIniciar.setPrefHeight(46);
        btnIniciar.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        String n = "-fx-background-color:transparent;-fx-text-fill:#D4A017;-fx-border-color:#8B6914;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        String h = "-fx-background-color:rgba(201,146,42,0.18);-fx-text-fill:#F0D060;-fx-border-color:#D4A017;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        btnIniciar.setStyle(n);
        btnIniciar.setOnMouseEntered(e -> btnIniciar.setStyle(h));
        btnIniciar.setOnMouseExited(e -> btnIniciar.setStyle(n));

        panel.getChildren().addAll(titulo, infoPartida, tituloJugadores, listaJugadores, btnIniciar);
        getChildren().add(panel);
    }

    public Button getBtnIniciar() { return btnIniciar; }
}