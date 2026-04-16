package com.marrakech.game.presentation.views;

import com.marrakech.game.infrastructure.PartidaRepository;
import com.marrakech.game.infrastructure.PartidaRepository.Partida;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;

public class SalaEsperaView extends StackPane {

    private Button btnIniciar;
    private Button btnSalir;
    private Partida partida;
    private VBox listaJugadores;
    private Label lblEstado;
    private Timeline pollingTimeline;
    private Runnable onJuegoIniciado; // callback cuando la DB dice INICIADA
    private boolean esHost;
    private final String[] colores = {"#e74c3c","#3498db","#2ecc71","#f39c12"};

    public SalaEsperaView(Partida partida, boolean esHost) {
        this.partida = partida;
        this.esHost  = esHost;
        configurarFondo();
        configurarContenido();
        iniciarPolling();
    }

    // Constructor legacy sin host (por si acaso)
    public SalaEsperaView(Partida partida) {
        this(partida, true);
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
            "-fx-border-color: #8B6914;-fx-border-width: 1.5;" +
            "-fx-border-radius: 8;-fx-background-radius: 8;");
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.web("#000000", 0.85)); sombra.setRadius(30);
        panel.setEffect(sombra);

        Text titulo = new Text(partida.nombre.toUpperCase());
        titulo.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        titulo.setFill(Color.web("#D4A017"));

        // Código de sala grande y visible
        VBox codigoBox = new VBox(4);
        codigoBox.setAlignment(Pos.CENTER);
        Label lblCodigoTitulo = new Label("CÓDIGO DE SALA");
        lblCodigoTitulo.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        lblCodigoTitulo.setTextFill(Color.web("#9E7A3A"));
        Label lblCodigo = new Label(partida.id);
        lblCodigo.setFont(Font.font("Georgia", FontWeight.BOLD, 42));
        lblCodigo.setTextFill(Color.web("#F0D060"));
        codigoBox.getChildren().addAll(lblCodigoTitulo, lblCodigo);

        VBox infoPartida = construirInfoPartida();

        // Estado dinámico
        lblEstado = new Label(esHost ? "Esperando jugadores..." : "Esperando que el host inicie...");
        lblEstado.setFont(Font.font("Georgia", 13));
        lblEstado.setTextFill(Color.web("#C9922A"));

        Text tituloJugadores = new Text("Jugadores en sala");
        tituloJugadores.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        tituloJugadores.setFill(Color.web("#C9922A"));

        listaJugadores = new VBox(10);
        construirListaJugadores();

        HBox botonesInferiores = new HBox(14);
        botonesInferiores.setAlignment(Pos.CENTER);

        btnSalir = crearBotonContorno("SALIR DE PARTIDA");

        btnIniciar = crearBotonPrimario("INICIAR PARTIDA");
        // Solo el host puede iniciar, y solo si la sala está llena
        btnIniciar.setDisable(!esHost || partida.jugadores.size() < partida.maxJugadores);
        if (!esHost) btnIniciar.setVisible(false);
        HBox.setHgrow(btnIniciar, Priority.ALWAYS);

        botonesInferiores.getChildren().addAll(btnSalir, btnIniciar);

        panel.getChildren().addAll(titulo, codigoBox, infoPartida, lblEstado, tituloJugadores, listaJugadores, botonesInferiores);
        getChildren().add(panel);
    }

    private void iniciarPolling() {
        pollingTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            Partida actualizada = PartidaRepository.obtenerPartida(partida.id);
            if (actualizada == null) return;
            this.partida = actualizada;
            
            // Actualizar lista e info (para ver el estado cambiar en pantalla)
            construirListaJugadores();
            VBox panelPrincipal = (VBox) getChildren().get(1);
            panelPrincipal.getChildren().set(2, construirInfoPartida());

            boolean salaLlena = actualizada.jugadores.size() >= actualizada.maxJugadores;

            if (esHost) {
                btnIniciar.setDisable(!salaLlena);
                lblEstado.setText(salaLlena
                    ? "¡Sala llena! Puedes iniciar la partida."
                    : "Esperando jugadores... (" + actualizada.jugadores.size() + "/" + actualizada.maxJugadores + ")");
            } else {
                lblEstado.setText("Esperando que el host inicie... (" + actualizada.jugadores.size() + "/" + actualizada.maxJugadores + ")");
                // Guest detecta cuando el host inicia
                if ("INICIADA".equalsIgnoreCase(actualizada.estado.trim())) {
                    detenerPolling();
                    if (onJuegoIniciado != null) onJuegoIniciado.run();
                }
            }
        }));
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
    }

    public void detenerPolling() {
        if (pollingTimeline != null) pollingTimeline.stop();
    }

    public void setOnJuegoIniciado(Runnable callback) {
        this.onJuegoIniciado = callback;
    }

    private VBox construirInfoPartida() {
        VBox info = new VBox(6);
        info.setStyle("-fx-background-color:rgba(255,255,255,0.04);-fx-border-color:#4A3000;-fx-border-width:1;-fx-border-radius:5;-fx-background-radius:5;");
        info.setPadding(new Insets(14, 18, 14, 18));
        GridPane grid = new GridPane();
        grid.setHgap(30); grid.setVgap(6);
        String[][] datos = {
            {"Modo:", "Competitivo", "Poderes:", partida.poderesActivados ? "Activados" : "Desactivados"},
            {"Tiempo/turno:", partida.partidaRapida ? "20s" : "45s", "Dificultad:", partida.dificultad},
            {"Jugadores:", partida.jugadores.size() + "/" + partida.maxJugadores, "Estado:", partida.estado},
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
        info.getChildren().add(grid);
        return info;
    }

    private void construirListaJugadores() {
        listaJugadores.getChildren().clear();
        for (int i = 0; i < partida.jugadores.size(); i++) {
            String nombre = partida.jugadores.get(i);
            VBox tarjeta = new VBox(4);
            tarjeta.setPadding(new Insets(10, 16, 10, 16));
            tarjeta.setStyle(
                "-fx-background-color:rgba(255,255,255,0.05);" +
                "-fx-border-color:" + colores[i % colores.length] + "55;" +
                "-fx-border-width:1;-fx-border-radius:5;-fx-background-radius:5;");
            HBox cab = new HBox();
            cab.setAlignment(Pos.CENTER_LEFT);
            Text nombreText = new Text(nombre + (i == 0 ? "  [HOST]" : ""));
            nombreText.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
            nombreText.setFill(Color.web(colores[i % colores.length]));
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Label listo = new Label("● En sala");
            listo.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
            listo.setTextFill(Color.web("#2ecc71"));
            cab.getChildren().addAll(nombreText, sp, listo);
            tarjeta.getChildren().add(cab);
            listaJugadores.getChildren().add(tarjeta);
        }
    }

    private Button crearBotonPrimario(String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE); btn.setPrefHeight(46);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        String n = "-fx-background-color:#C9922A;-fx-text-fill:#1A0A00;-fx-border-color:#E8C97A;-fx-border-width:1;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        String h = "-fx-background-color:#E8A830;-fx-text-fill:#1A0A00;-fx-border-color:#F0D88A;-fx-border-width:1;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        btn.setStyle(n); btn.setOnMouseEntered(e->btn.setStyle(h)); btn.setOnMouseExited(e->btn.setStyle(n));
        return btn;
    }

    private Button crearBotonContorno(String texto) {
        Button btn = new Button(texto);
        btn.setPrefHeight(46);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        String n = "-fx-background-color:transparent;-fx-text-fill:#e74c3c;-fx-border-color:#e74c3c;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        String h = "-fx-background-color:rgba(231,76,60,0.15);-fx-text-fill:#ff6b6b;-fx-border-color:#ff6b6b;-fx-border-width:1.5;-fx-border-radius:4;-fx-background-radius:4;-fx-cursor:hand;";
        btn.setStyle(n); btn.setOnMouseEntered(e->btn.setStyle(h)); btn.setOnMouseExited(e->btn.setStyle(n));
        return btn;
    }

    public Button getBtnIniciar()              { return btnIniciar; }
    public Button getBtnSalir()                { return btnSalir; }
    public int getNumJugadores()               { return partida.maxJugadores; }
    public String getPartidaId()               { return partida.id; }
}
