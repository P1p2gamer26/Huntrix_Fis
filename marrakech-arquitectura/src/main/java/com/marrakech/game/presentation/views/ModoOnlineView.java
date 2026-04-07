package com.marrakech.game.presentation.views;

import java.util.List;

import com.marrakech.game.infrastructure.PartidaRepository;
import com.marrakech.game.infrastructure.PartidaRepository.RankingEntry;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class ModoOnlineView extends StackPane {

    private Button btnCrear;
    private Button btnUnirse;

    public ModoOnlineView() {
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
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.70);");
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());
        getChildren().add(overlay);
    }

    private void configurarContenido() {
        VBox contenido = new VBox(24);
        contenido.setAlignment(Pos.TOP_CENTER);
        contenido.setMaxWidth(700);
        contenido.setPadding(new Insets(48, 0, 40, 0));

        Text titulo = new Text("MODO ONLINE");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 52));
        titulo.setFill(Color.web("#D4A017"));
        DropShadow s = new DropShadow();
        s.setColor(Color.web("#7A4500")); s.setRadius(12); s.setOffsetX(3); s.setOffsetY(4);
        titulo.setEffect(s);

        HBox botones = new HBox(20);
        botones.setAlignment(Pos.CENTER);
        btnCrear  = crearBotonRelleno("CREAR");
        btnUnirse = crearBotonContorno("UNIRSE A PARTIDA");
        botones.getChildren().addAll(btnCrear, btnUnirse);

        Text subtituloRanking = new Text("RANKING MENSUAL");
        subtituloRanking.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        subtituloRanking.setFill(Color.web("#C9922A"));

        VBox listaRanking = new VBox(10);
        listaRanking.setMaxWidth(580);
        listaRanking.setPadding(new Insets(0, 20, 0, 20));

        List<RankingEntry> rankingData = PartidaRepository.obtenerRanking();

        if (rankingData.isEmpty()) {
            Text vacio = new Text("Aún no hay victorias registradas.");
            vacio.setFont(Font.font("Georgia", 14));
            vacio.setFill(Color.web("#9E7A3A"));
            listaRanking.getChildren().add(vacio);
        } else {
            for (int i = 0; i < rankingData.size(); i++) {
                RankingEntry entry = rankingData.get(i);
                HBox fila = new HBox();
                fila.setAlignment(Pos.CENTER_LEFT);

                Text posYNombre = new Text((i + 1) + ".  " + entry.usuario);
                posYNombre.setFont(Font.font("Georgia", 15));
                posYNombre.setFill(Color.web("#D4B87A"));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Text victorias = new Text(entry.victorias + " victorias");
                victorias.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
                victorias.setFill(Color.web("#D4A017"));

                fila.getChildren().addAll(posYNombre, spacer, victorias);
                listaRanking.getChildren().add(fila);
            }
        }

        contenido.getChildren().addAll(titulo, botones, subtituloRanking, listaRanking);
        getChildren().add(contenido);
    }

    private Button crearBotonRelleno(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(180); btn.setPrefHeight(44);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String n = "-fx-background-color:#C9922A;-fx-text-fill:#1A0A00;-fx-border-color:#E8C97A;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        String h = "-fx-background-color:#E8A830;-fx-text-fill:#1A0A00;-fx-border-color:#F0D88A;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        btn.setStyle(n); btn.setOnMouseEntered(e->btn.setStyle(h)); btn.setOnMouseExited(e->btn.setStyle(n));
        return btn;
    }

    private Button crearBotonContorno(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(200); btn.setPrefHeight(44);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String n = "-fx-background-color:transparent;-fx-text-fill:#D4A017;-fx-border-color:#C9922A;-fx-border-width:2;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        String h = "-fx-background-color:rgba(201,146,42,0.18);-fx-text-fill:#F0D060;-fx-border-color:#D4A017;-fx-border-width:2;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        btn.setStyle(n); btn.setOnMouseEntered(e->btn.setStyle(h)); btn.setOnMouseExited(e->btn.setStyle(n));
        return btn;
    }

    public Button getBtnCrear()  { return btnCrear; }
    public Button getBtnUnirse() { return btnUnirse; }
}