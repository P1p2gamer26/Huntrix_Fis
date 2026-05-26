package com.marrakech.game.presentation.views;

import java.util.List;

import com.marrakech.game.repository.PartidaRepositorio.RankingEntry;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
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
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class MenuView extends StackPane {

    private Button btnJugar;
    private Button btnJugarLocal;
    private Button btnReglas;
    private Button btnConfiguracion;
    private Button btnCerrarSesion;
    private HBox   tarjetaUsuario;
    private final String usuario;

    public MenuView(String usuario, List<RankingEntry> ranking) {
        this.usuario = usuario == null || usuario.isEmpty() ? "Jugador" : usuario;
        configurarFondo();
        configurarContenido(ranking);
        configurarTarjetaUsuario();
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

    private void configurarContenido(List<RankingEntry> ranking) {
        HBox filaPrincipal = new HBox(40);
        filaPrincipal.setAlignment(Pos.CENTER);

        VBox columnaCentral = new VBox(18);
        columnaCentral.setAlignment(Pos.CENTER);
        columnaCentral.setMaxWidth(320);

        Text titulo = new Text("MARRAKESH");
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 72));
        titulo.setFill(Color.web("#D4A017"));
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#7A4500")); sombra.setRadius(14); sombra.setOffsetX(4); sombra.setOffsetY(4);
        Glow glow = new Glow(0.3);
        glow.setInput(sombra);
        titulo.setEffect(glow);

        Text subtitulo = new Text("JUEGO DE MESA DIGITAL  ·  VERSIÓN ALFA");
        subtitulo.setFont(Font.font("Georgia", 12));
        subtitulo.setFill(Color.web("#9E7A3A"));

        btnJugar         = crearBoton("JUGAR EN LÍNEA");
        btnJugarLocal    = crearBoton("JUGAR LOCAL");
        btnReglas        = crearBoton("REGLAS");
        btnConfiguracion = crearBoton("CONFIGURACIÓN");

        VBox botones = new VBox(14);
        botones.setAlignment(Pos.CENTER);
        botones.getChildren().addAll(btnJugar, btnJugarLocal, btnReglas, btnConfiguracion);

        columnaCentral.getChildren().addAll(titulo, botones, subtitulo);

        VBox rankingPanel = new VBox(10);
        rankingPanel.setAlignment(Pos.TOP_CENTER);
        rankingPanel.setStyle("-fx-background-color:rgba(10,4,0,0.82);-fx-border-color:#8B6914;-fx-border-width:1.5px;-fx-border-radius:8px;-fx-background-radius:8px;-fx-padding:16 20;");
        rankingPanel.setMaxWidth(280);
        rankingPanel.setMinWidth(240);

        Text rankTitulo = new Text("TOP GANADORES");
        rankTitulo.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        rankTitulo.setFill(Color.web("#F0D060"));

        rankingPanel.getChildren().add(rankTitulo);

        if (ranking == null || ranking.isEmpty()) {
            Text vacio = new Text("Aun no hay victorias registradas.");
            vacio.setFont(Font.font("Georgia", 12));
            vacio.setFill(Color.web("#7A5A30"));
            rankingPanel.getChildren().add(vacio);
        } else {
            int top = Math.min(ranking.size(), 5);
            for (int i = 0; i < top; i++) {
                RankingEntry entry = ranking.get(i);
                HBox fila = new HBox();
                fila.setAlignment(Pos.CENTER_LEFT);

                Text posYNombre = new Text((i + 1) + ".  " + entry.usuario);
                posYNombre.setFont(Font.font("Georgia", 13));
                posYNombre.setFill(Color.web("#D4B87A"));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Text victorias = new Text(entry.victorias + " victorias");
                victorias.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
                victorias.setFill(Color.web("#D4A017"));

                fila.getChildren().addAll(posYNombre, spacer, victorias);
                rankingPanel.getChildren().add(fila);
            }
        }

        filaPrincipal.getChildren().addAll(columnaCentral, rankingPanel);

        VBox contenedorTotal = new VBox();
        contenedorTotal.setAlignment(Pos.CENTER);
        contenedorTotal.setPadding(new Insets(0, 0, 60, 0));
        contenedorTotal.getChildren().add(filaPrincipal);
        getChildren().add(contenedorTotal);

        btnCerrarSesion = crearBotonCerrarSesion();
        StackPane.setAlignment(btnCerrarSesion, Pos.BOTTOM_LEFT);
        StackPane.setMargin(btnCerrarSesion, new Insets(0, 0, 22, 22));
        getChildren().add(btnCerrarSesion);
    }

    private Button crearBotonCerrarSesion() {
        Button btn = new Button("CERRAR SESIÓN");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(36);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        String n = "-fx-background-color:transparent;-fx-text-fill:#885533;-fx-border-color:#5C3A10;-fx-border-width:1.5;-fx-border-radius:3;-fx-background-radius:3;-fx-cursor:hand;";
        String h = "-fx-background-color:rgba(100,40,20,0.2);-fx-text-fill:#AA6655;-fx-border-color:#885533;-fx-border-width:1.5;-fx-border-radius:3;-fx-background-radius:3;-fx-cursor:hand;";
        btn.setStyle(n); btn.setOnMouseEntered(e->btn.setStyle(h)); btn.setOnMouseExited(e->btn.setStyle(n));
        return btn;
    }

    private void configurarTarjetaUsuario() {
        tarjetaUsuario = new HBox(8);
        tarjetaUsuario.setAlignment(Pos.CENTER_LEFT);
        tarjetaUsuario.setPadding(new Insets(8, 14, 8, 10));
        tarjetaUsuario.setCursor(Cursor.HAND);
        tarjetaUsuario.setMaxWidth(Region.USE_PREF_SIZE);
        tarjetaUsuario.setMaxHeight(Region.USE_PREF_SIZE);

        String estiloNormal = "-fx-background-color:rgba(10,4,0,0.82);-fx-border-color:#8B6914;-fx-border-width:1;-fx-border-radius:20;-fx-background-radius:20;";
        String estiloHover  = "-fx-background-color:rgba(201,146,42,0.22);-fx-border-color:#D4A017;-fx-border-width:1;-fx-border-radius:20;-fx-background-radius:20;";
        tarjetaUsuario.setStyle(estiloNormal);
        tarjetaUsuario.setOnMouseEntered(e -> tarjetaUsuario.setStyle(estiloHover));
        tarjetaUsuario.setOnMouseExited(e  -> tarjetaUsuario.setStyle(estiloNormal));

        StackPane avatar = new StackPane();
        avatar.setMaxWidth(28); avatar.setMaxHeight(28);
        avatar.setMinWidth(28); avatar.setMinHeight(28);
        Circle circulo = new Circle(14);
        circulo.setFill(Color.web("#2A1400"));
        circulo.setStroke(Color.web("#C9922A"));
        circulo.setStrokeWidth(1.2);
        Text icono = new Text("👤");
        icono.setFont(Font.font(12));
        avatar.getChildren().addAll(circulo, icono);

        VBox info = new VBox(1);
        info.setAlignment(Pos.CENTER_LEFT);
        Text nombreText = new Text(usuario);
        nombreText.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        nombreText.setFill(Color.web("#D4A017"));
        Text subText = new Text("Ver perfil →");
        subText.setFont(Font.font("Georgia", 9));
        subText.setFill(Color.web("#9E7A3A"));
        info.getChildren().addAll(nombreText, subText);

        tarjetaUsuario.getChildren().addAll(avatar, info);

        StackPane.setAlignment(tarjetaUsuario, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(tarjetaUsuario, new Insets(0, 20, 20, 0));
        getChildren().add(tarjetaUsuario);
    }

    private Button crearBoton(String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(46);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String normal = "-fx-background-color:transparent;-fx-text-fill:#D4A017;-fx-border-color:#8B6914;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        String hover  = "-fx-background-color:rgba(201,146,42,0.18);-fx-text-fill:#F0D060;-fx-border-color:#D4A017;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    public Button getBtnJugar()         { return btnJugar; }
    public Button getBtnJugarLocal()    { return btnJugarLocal; }
    public Button getBtnReglas()        { return btnReglas; }
    public Button getBtnConfiguracion() { return btnConfiguracion; }
    public Button getBtnCerrarSesion()  { return btnCerrarSesion; }
    public HBox   getTarjetaUsuario()   { return tarjetaUsuario; }
}