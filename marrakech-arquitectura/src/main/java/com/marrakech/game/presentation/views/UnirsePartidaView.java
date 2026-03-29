package com.marrakech.game.presentation.views;

import com.marrakech.game.infrastructure.PartidaRepository;
import com.marrakech.game.infrastructure.PartidaRepository.Partida;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class UnirsePartidaView extends StackPane {

    private TextField campoCodigo;
    private Button btnUnirPorCodigo;
    private Consumer<String> onUnirse;

    public UnirsePartidaView() {
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

        Text titulo = new Text("PARTIDAS DISPONIBLES");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        titulo.setFill(Color.web("#D4A017"));

        VBox seccionCodigo = new VBox(8);
        seccionCodigo.setAlignment(Pos.CENTER);
        seccionCodigo.setPadding(new Insets(14, 20, 14, 20));
        seccionCodigo.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04);" +
            "-fx-border-color: #4A3000;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;"
        );

        Label lblCodigo = new Label("Unirse mediante código único");
        lblCodigo.setFont(Font.font("Georgia", 13));
        lblCodigo.setTextFill(Color.web("#D4B87A"));

        HBox filaCodigo = new HBox(10);
        filaCodigo.setAlignment(Pos.CENTER);
        campoCodigo = new TextField();
        campoCodigo.setPromptText("Ej: MRK-9921");
        campoCodigo.setPrefHeight(36);
        campoCodigo.setPrefWidth(220);
        campoCodigo.setStyle(
            "-fx-background-color: rgba(15,5,0,0.70);" +
            "-fx-text-fill: #E8C97A;" +
            "-fx-prompt-text-fill: #6A4A1A;" +
            "-fx-border-color: #8B6914;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 3;" +
            "-fx-background-radius: 3;" +
            "-fx-font-size: 13;"
        );

        btnUnirPorCodigo = crearBotonRelleno("UNIRSE");
        btnUnirPorCodigo.setOnAction(e -> {
            String codigo = campoCodigo.getText().trim().toUpperCase();
            if (!codigo.isEmpty() && onUnirse != null) onUnirse.accept(codigo);
        });

        filaCodigo.getChildren().addAll(campoCodigo, btnUnirPorCodigo);
        seccionCodigo.getChildren().addAll(lblCodigo, filaCodigo);

        VBox listaPartidas = construirListaPartidas();

        ScrollPane scroll = new ScrollPane(listaPartidas);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(320);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        panel.getChildren().addAll(titulo, seccionCodigo, scroll);
        getChildren().add(panel);
    }

    private VBox construirListaPartidas() {
        VBox lista = new VBox(10);
        lista.setPadding(new Insets(4, 0, 4, 0));

        List<Partida> partidas = PartidaRepository.listarPartidas();

        if (partidas.isEmpty()) {
            Label vacia = new Label("No hay partidas disponibles. ¡Crea una!");
            vacia.setFont(Font.font("Georgia", 14));
            vacia.setTextFill(Color.web("#9E7A3A"));
            lista.setAlignment(Pos.CENTER);
            lista.getChildren().add(vacia);
            return lista;
        }

        for (Partida p : partidas) {
            HBox fila = new HBox();
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(12, 16, 12, 16));
            fila.setStyle(
                "-fx-background-color: rgba(255,255,255,0.04);" +
                "-fx-border-color: #4A3000;" +
                "-fx-border-width: 0 0 1 0;"
            );

            Label info = new Label(p.resumen());
            info.setFont(Font.font("Georgia", 14));
            info.setTextFill(Color.web("#D4B87A"));
            HBox.setHgrow(info, Priority.ALWAYS);

            Button btnUnir = crearBotonContorno("UNIRSE");
            btnUnir.setOnAction(e -> { if (onUnirse != null) onUnirse.accept(p.id); });

            fila.getChildren().addAll(info, btnUnir);
            lista.getChildren().add(fila);
        }
        return lista;
    }

    private Button crearBotonRelleno(String texto) {
        Button btn = new Button(texto);
        btn.setPrefHeight(36);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        String n = "-fx-background-color:#C9922A;-fx-text-fill:#1A0A00;-fx-border-color:#E8C97A;-fx-border-width:1;-fx-border-radius:3;-fx-background-radius:3;-fx-cursor:hand;";
        String h = "-fx-background-color:#E8A830;-fx-text-fill:#1A0A00;-fx-border-color:#F0D88A;-fx-border-width:1;-fx-border-radius:3;-fx-background-radius:3;-fx-cursor:hand;";
        btn.setStyle(n); btn.setOnMouseEntered(e->btn.setStyle(h)); btn.setOnMouseExited(e->btn.setStyle(n));
        return btn;
    }

    private Button crearBotonContorno(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(100); btn.setPrefHeight(34);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        String n = "-fx-background-color:transparent;-fx-text-fill:#D4A017;-fx-border-color:#8B6914;-fx-border-width:1.5;-fx-border-radius:3;-fx-background-radius:3;-fx-cursor:hand;";
        String h = "-fx-background-color:rgba(201,146,42,0.18);-fx-text-fill:#F0D060;-fx-border-color:#D4A017;-fx-border-width:1.5;-fx-border-radius:3;-fx-background-radius:3;-fx-cursor:hand;";
        btn.setStyle(n); btn.setOnMouseEntered(e->btn.setStyle(h)); btn.setOnMouseExited(e->btn.setStyle(n));
        return btn;
    }

    public void setOnUnirse(Consumer<String> handler) { this.onUnirse = handler; }
}