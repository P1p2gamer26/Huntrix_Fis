package com.marrakech.game.presentation.controller;

import java.util.Random;

import com.marrakech.game.presentation.render.GameRenderEngine;
import com.marrakech.game.repository.IEstadoJuegoRepositorio;
import com.marrakech.game.service.AssamServicio;
import com.marrakech.game.service.ChatServicio;
import com.marrakech.game.service.EstadoJuegoServicio;
import com.marrakech.game.service.EstadoJuegoServicio.EstadoDB;
import com.marrakech.game.service.GestionJuegoServicio;
import com.marrakech.game.service.GestionJuegoServicio.Reliquia;
import com.marrakech.game.service.MusicaServicio;
import com.marrakech.game.service.PartidaServicio;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GameController {

    @FXML private VBox      startScreen;
    @FXML private HBox      gameScreen;
    @FXML private StackPane endScreen;
    @FXML private CheckBox  checkPoderesLocal;
    @FXML private GridPane  boardGrid;
    @FXML private Label     turnLabel, statusLabel, diceResultLabel, diceValueLabel;
    @FXML private Button    rollDiceBtn;
    @FXML private VBox      panelJ1, panelJ2, panelJ3, panelJ4;
    @FXML private Label     rugsJ1, rugsJ2, rugsJ3, rugsJ4;
    @FXML private Label     moneyJ1, moneyJ2, moneyJ3, moneyJ4;
    @FXML private Label     resultadoLabel, winnerLabel, finalScores;
    @FXML private Button    btnVolverSala, btnVolverMenu;
    @FXML private Canvas    diceCanvas;
    @FXML private VBox      chatPanel, chatBox, poderesLegendPanel;
    @FXML private VBox      leftPanel, notificacionesPanel;
    @FXML private ScrollPane chatScroll;
    @FXML private TextField  chatInput;
    @FXML private Button     chatSendBtn;

    private Runnable onVolverSala;
    private Runnable onVolverMenu;

    public void setOnVolverSala(Runnable r) { this.onVolverSala = r; }
    public void setOnVolverMenu(Runnable r) { this.onVolverMenu = r; }

    private static final int PHASE_MOVE = 0;

    private String  partidaId;
    private String  usuarioActual;
    private int     miIndice = 0;
    private boolean modoMultijugador = false;
    private boolean poderesActivados = false;
    private boolean partidaRapida    = false;
    private boolean sultanPendiente  = false;

    private GestionJuegoServicio juegoSvc;
    private AssamServicio        assamSvc;
    private EstadoJuegoServicio  estadoSvc;
    private ChatServicio         chatSvc;
    private PartidaServicio      partidaSvc;
    private MusicaServicio       musicaSvc;
    private IEstadoJuegoRepositorio estadoRepo;

    private TableroController tableroCtrl;
    private AssamController   assamCtrl;
    private ChatController    chatCtrl;
    private FinJuegoController finJuegoCtrl;
    private PoderesController  poderesCtrl;

    public void initialize() {}

    public void setServicios(MusicaServicio musicaSvc, ChatServicio chatSvc) {
        this.musicaSvc = musicaSvc;
        this.chatSvc   = chatSvc;
    }

    public void setEstadoRepositorio(IEstadoJuegoRepositorio estadoRepo) {
        this.estadoRepo = estadoRepo;
    }

    public void iniciarConJugadores(int n, String partidaId, String usuario,
                                    int miIndice, PartidaServicio partidaSvc, boolean poderes, boolean rapida) {
        this.partidaId        = partidaId;
        this.usuarioActual    = usuario;
        this.miIndice         = miIndice;
        this.modoMultijugador = true;
        this.partidaSvc       = partidaSvc;
        this.poderesActivados = poderes;
        this.partidaRapida    = rapida;

        this.estadoSvc = new EstadoJuegoServicio(estadoRepo, partidaId);

        startGame(n, rapida);

        if (miIndice == 0) {
            estadoSvc.guardarEstadoSincrono(
                assamSvc.getX(), assamSvc.getY(), assamSvc.getDir(),
                serializarEstado());
        }
        estadoSvc.iniciarPolling(raw -> aplicarEstadoDesdeDB(raw));

        chatSvc.inicializar(partidaId, usuario);
        chatCtrl.cargarHistorial();
        chatSvc.iniciarPolling(() -> chatCtrl.cargarNuevos());
    }

    public void iniciarConJugadores(int n) {
        this.modoMultijugador = false;
        startGame(n);
    }

    @FXML private void startWith2() { iniciarConJugadoresLocal(2); }
    @FXML private void startWith3() { iniciarConJugadoresLocal(3); }
    @FXML private void startWith4() { iniciarConJugadoresLocal(4); }

    private void iniciarConJugadoresLocal(int n) {
        this.poderesActivados = checkPoderesLocal != null && checkPoderesLocal.isSelected();
        iniciarConJugadores(n);
    }

    private void startGame(int n) { startGame(n, false); }

    private void startGame(int n, boolean rapida) {
        juegoSvc = new GestionJuegoServicio();
        juegoSvc.iniciarJuego(n, rapida);

        assamSvc = new AssamServicio();

        GameRenderEngine renderEngine = new GameRenderEngine(boardGrid, diceCanvas);

        tableroCtrl = new TableroController(boardGrid, renderEngine, juegoSvc);
        tableroCtrl.inicializar();
        tableroCtrl.limpiarHighlights();

        assamCtrl    = new AssamController(assamSvc, boardGrid);
        chatCtrl     = new ChatController(chatBox, chatInput, chatScroll, chatPanel, usuarioActual);
        poderesCtrl  = new PoderesController(juegoSvc, poderesActivados);
        finJuegoCtrl = new FinJuegoController(endScreen, gameScreen,
            resultadoLabel, winnerLabel, finalScores, btnVolverSala, btnVolverMenu,
            juegoSvc, chatSvc);

        if (partidaSvc != null || estadoSvc != null)
            finJuegoCtrl.setServicios(partidaSvc, estadoSvc);

        tableroCtrl.setOnCarpetPlaced(() -> {
            if (sultanPendiente) {
                sultanPendiente = false;
                statusLabel.setText("[A] Alfombra del Sultán — coloca tu segunda alfombra.");
                juegoSvc.setCurrentPhase(1);
                tableroCtrl.actualizarContexto(1, juegoSvc.getCurrentPlayerIdx(),
                    assamSvc.getX(), assamSvc.getY(), juegoSvc.getRugs());
                tableroCtrl.limpiarHighlights();
                actualizarUI(); actualizarControles();
            } else {
                pasarTurno();
            }
        });
        tableroCtrl.setOnGameEnded(() -> finJuegoCtrl.mostrar(
            modoMultijugador, miIndice, usuarioActual));
        tableroCtrl.setOnAlfombraCompleta(() ->
            statusLabel.setText("¡No puedes tapar una alfombra completa! Elige otra casilla.")
        );

        finJuegoCtrl.setOnVolverSala(onVolverSala);
        finJuegoCtrl.setOnVolverMenu(onVolverMenu);

        chatCtrl.inicializar(modoMultijugador);
        if (chatSvc != null) chatCtrl.setServicios(chatSvc, partidaId);

        if (poderesLegendPanel != null) {
            if (poderesActivados) {
                construirLeyendaPoderes();
                poderesLegendPanel.setVisible(true);
                poderesLegendPanel.setManaged(true);
            } else {
                poderesLegendPanel.setVisible(false);
                poderesLegendPanel.setManaged(false);
            }
        }

        panelJ3.setVisible(n >= 3); panelJ3.setManaged(n >= 3);
        panelJ4.setVisible(n >= 4); panelJ4.setManaged(n >= 4);

        assamCtrl.getView().toFront();

        int firstPlayer = 0;
        if (!modoMultijugador) {
            firstPlayer = new Random().nextInt(n);
        }

        juegoSvc.setCurrentPlayerIdx(firstPlayer);
        juegoSvc.setCurrentPhase(0);
        tableroCtrl.actualizarContexto(0, firstPlayer,
            assamSvc.getX(), assamSvc.getY(), juegoSvc.getRugs());

        startScreen.setVisible(false);
        endScreen.setVisible(false);
        gameScreen.setVisible(true);

        renderEngine.dibujarDadoInicial();
        actualizarUI();
        actualizarControles();

        if (chatInput != null)
            chatInput.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER) chatCtrl.enviar();
            });

        statusLabel.setText(modoMultijugador
            ? "Conectando... Eres " + playerName(miIndice) + ". Espera el inicio."
            : "Rota a Assam y lanza el dado.");
    }

    @FXML protected void rotateLeft() {
        if (!juegoSvc.esMiTurno(modoMultijugador, miIndice)
            || juegoSvc.getCurrentPhase() != PHASE_MOVE) return;
        assamCtrl.rotarIzquierda();
    }

    @FXML protected void rotateRight() {
        if (!juegoSvc.esMiTurno(modoMultijugador, miIndice)
            || juegoSvc.getCurrentPhase() != PHASE_MOVE) return;
        assamCtrl.rotarDerecha();
    }

    @FXML protected void onRollDiceClick() {
        if (!juegoSvc.esMiTurno(modoMultijugador, miIndice)
            || juegoSvc.getCurrentPhase() != PHASE_MOVE) return;

        int pasos = poderesCtrl.resolverDado();
        ejecutarMovimientoAssam(pasos);
    }

    private void ejecutarMovimientoAssam(int pasos) {
        rollDiceBtn.setDisable(true);
        diceResultLabel.setText(String.valueOf(pasos));
        diceValueLabel.setText(String.valueOf(pasos));

        GameRenderEngine renderEngine = new GameRenderEngine(boardGrid, diceCanvas);
        renderEngine.animarDado(pasos, () -> {
            int[][] path = assamSvc.computePath(pasos);

            assamCtrl.animarMovimiento(pasos, () -> Platform.runLater(() -> {
                assamCtrl.getView().toFront();
                int ax = assamSvc.getX(), ay = assamSvc.getY();

                Reliquia recogida = poderesCtrl.recogerReliquiasEnRecorrido(path, pasos);
                if (recogida != null) {
                    statusLabel.setText(recogida.icono + " \u00a1Recogiste: " + recogida.nombre + "!");
                    redibujarTablero();
                    actualizarUI();
                    agregarNotificacion("[+] Recogiste " + recogida.icono + " "
                        + recogida.nombre + " — " + recogida.descripcion);
                }

                PoderesController.ResultadoPost resultado =
                    poderesCtrl.resolverPostMovimiento(ax, ay, pasos);
                statusLabel.setText(resultado.mensaje);

                int jugadorActual = juegoSvc.getCurrentPlayerIdx();
                boolean eliminado = juegoSvc.verificarEliminacion(jugadorActual);

                if (juegoSvc.juegoTerminado()) {
                    actualizarUI(); notificarTurnoListo();
                    finJuegoCtrl.mostrar(modoMultijugador, miIndice, usuarioActual);
                    return;
                }

                if (eliminado) {
                    statusLabel.setText("[X] " + playerName(jugadorActual) + " se quedo sin monedas y fue eliminado.");
                    actualizarUI(); guardarEstado();
                    pasarTurno();
                    return;
                }

                int newPhase = juegoSvc.fasePostDado();
                juegoSvc.setCurrentPhase(newPhase);

                boolean sultanActivado = poderesCtrl.resolverSultan();
                if (sultanActivado) {
                    sultanPendiente = true;
                    statusLabel.setText("[A] Alfombra del Sultán — coloca tu primera alfombra!");
                    tableroCtrl.actualizarContexto(newPhase, juegoSvc.getCurrentPlayerIdx(),
                        assamSvc.getX(), assamSvc.getY(), juegoSvc.getRugs());
                    actualizarUI(); actualizarControles();
                    return;
                }
                tableroCtrl.actualizarContexto(newPhase, juegoSvc.getCurrentPlayerIdx(),
                    assamSvc.getX(), assamSvc.getY(), juegoSvc.getRugs());
                if (juegoSvc.getRugs()[juegoSvc.getCurrentPlayerIdx()] == 0) { pasarTurno(); return; }
                actualizarUI(); actualizarControles();
            }));
        });
    }

    private void construirLeyendaPoderes() {
        poderesLegendPanel.getChildren().clear();
        poderesLegendPanel.setStyle("-fx-background-color:rgba(10,4,0,0.92);"
            + "-fx-border-color:#C9922A;-fx-border-width:1.5px;-fx-border-radius:8px;-fx-background-radius:8px;"
            + "-fx-padding:10 10 10 10;");

        Label titulo = new Label("[*]  L E Y E N D A   D E   P O D E R E S");
        titulo.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#F0D060;"
            + "-fx-padding:0 0 6 0;-fx-effect:dropshadow(gaussian,rgba(240,208,96,0.3),6,0.3,0,0);");
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setAlignment(javafx.geometry.Pos.CENTER);
        poderesLegendPanel.getChildren().add(titulo);

        Label sub = new Label("Reliquias m\u00E1gicas del zoco");
        sub.setStyle("-fx-font-size:9px;-fx-text-fill:#9E7A3A;-fx-padding:0 0 6 0;");
        sub.setMaxWidth(Double.MAX_VALUE);
        sub.setAlignment(javafx.geometry.Pos.CENTER);
        poderesLegendPanel.getChildren().add(sub);

        for (Reliquia r : Reliquia.values()) {
            Label encabezado = new Label(r.icono + "  " + r.nombre);
            encabezado.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#E8D4A0;-fx-padding:6 0 1 0;");
            encabezado.setMaxWidth(Double.MAX_VALUE);
            poderesLegendPanel.getChildren().add(encabezado);

            Label desc = new Label(r.descripcion);
            desc.setStyle("-fx-font-size:10px;-fx-text-fill:#B89860;-fx-padding:0 0 0 12;");
            desc.setWrapText(true);
            desc.setMaxWidth(Double.MAX_VALUE);
            poderesLegendPanel.getChildren().add(desc);

            Label flavor = new Label("\u201C" + r.leyenda + "\u201D");
            flavor.setStyle("-fx-font-size:9px;-fx-font-style:italic;-fx-text-fill:#7A5A30;"
                + "-fx-padding:1 0 3 12;");
            flavor.setWrapText(true);
            flavor.setMaxWidth(Double.MAX_VALUE);
            poderesLegendPanel.getChildren().add(flavor);
        }

        Label sep = new Label(" \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500 ");
        sep.setStyle("-fx-font-size:9px;-fx-text-fill:#5C3A10;-fx-padding:6 0 4 0;");
        sep.setMaxWidth(Double.MAX_VALUE);
        poderesLegendPanel.getChildren().add(sep);

        Label invLabel = new Label("Tus reliquias:");
        invLabel.setStyle("-fx-font-size:10px;-fx-font-weight:bold;-fx-text-fill:#C9922A;");
        invLabel.setMaxWidth(Double.MAX_VALUE);
        invLabel.setUserData("inventario-label");
        poderesLegendPanel.getChildren().add(invLabel);
    }

    private void agregarNotificacion(String texto) {
        Label notif = new Label(texto);
        notif.setWrapText(true);
        notif.setAlignment(Pos.CENTER_LEFT);
        notif.setMaxWidth(Double.MAX_VALUE);
        notif.setStyle("-fx-background-color:rgba(201,146,42,0.18);"
            + "-fx-background-radius:6px;-fx-border-color:rgba(201,146,42,0.4);"
            + "-fx-border-width:1px;-fx-border-radius:6px;"
            + "-fx-padding:6 6;-fx-text-fill:#F0D060;"
            + "-fx-font-size:11px;-fx-font-weight:bold;");
        notificacionesPanel.getChildren().add(notif);

        PauseTransition pt = new PauseTransition(javafx.util.Duration.seconds(8));
        pt.setOnFinished(e -> Platform.runLater(() ->
            notificacionesPanel.getChildren().remove(notif)));
        pt.play();
    }

    private void notificarAparicionReliquia(Reliquia r) {
        agregarNotificacion("[*] " + r.icono + "  " + r.nombre
            + " — " + r.descripcion);
    }

    private void pasarTurno() {
        juegoSvc.pasarTurno();

        Reliquia reliquiaNueva = null;
        if (poderesActivados) {
            reliquiaNueva = juegoSvc.intentarAparecerReliquia(assamSvc.getX(), assamSvc.getY());
        }

        redibujarTablero();
        tableroCtrl.actualizarContexto(juegoSvc.getCurrentPhase(), juegoSvc.getCurrentPlayerIdx(),
            assamSvc.getX(), assamSvc.getY(), juegoSvc.getRugs());
        tableroCtrl.limpiarHighlights();
        actualizarUI(); actualizarControles();

        if (reliquiaNueva != null) {
            notificarAparicionReliquia(reliquiaNueva);
            statusLabel.setText("\u2192 Turno de " + playerName(juegoSvc.getCurrentPlayerIdx())
                + " [*] Aparecio " + reliquiaNueva.icono + " " + reliquiaNueva.nombre + " en el tablero!");
        } else {
            actualizarStatus();
        }
        notificarTurnoListo();
    }

    private void aplicarEstadoDesdeDB(String raw) {
        if (raw == null) return;
        EstadoDB est = EstadoJuegoServicio.parsearEstado(raw);
        if (est == null) return;

        estadoSvc.setUltimoTurnoVisto(est.turno);
        estadoSvc.setEstadoVersion(est.turno);

        assamSvc.setPosition(est.ax, est.ay);
        assamSvc.setDir(est.adir);
        assamCtrl.actualizarPosicionEnGrid();

        juegoSvc.aplicarEstado(raw);

        tableroCtrl.actualizarContexto(juegoSvc.getCurrentPhase(), juegoSvc.getCurrentPlayerIdx(),
            assamSvc.getX(), assamSvc.getY(), juegoSvc.getRugs());
        redibujarTablero();
        tableroCtrl.limpiarHighlights();

        if (juegoSvc.getCurrentPhase() == 2 && juegoSvc.getFirstCarpetX() >= 0
                && juegoSvc.esMiTurno(modoMultijugador, miIndice))
            tableroCtrl.highlightTile(juegoSvc.getFirstCarpetX(), juegoSvc.getFirstCarpetY());

        assamCtrl.getView().toFront();
        actualizarUI(); actualizarControles(); actualizarStatus();

        // Solo el jugador cuyo turno acaba de comenzar desmarca listo
        if (estadoSvc != null && juegoSvc.esMiTurno(modoMultijugador, miIndice))
            estadoSvc.desmarcarListo();

        if (juegoSvc.juegoTerminado() && !endScreen.isVisible())
            finJuegoCtrl.mostrar(modoMultijugador, miIndice, usuarioActual);
    }

    @FXML private void volverSala() {
        if (!endScreen.isVisible() && !confirmarSalida()) return;
        if (estadoSvc != null) estadoSvc.detenerPolling();
        if (chatSvc   != null) chatSvc.detenerPolling();
        if (onVolverSala != null) onVolverSala.run();
    }

    @FXML private void volverMenu() {
        if (!endScreen.isVisible() && !confirmarSalida()) return;
        if (estadoSvc != null) estadoSvc.detenerPolling();
        if (chatSvc   != null) chatSvc.detenerPolling();
        if (onVolverMenu != null) onVolverMenu.run();
    }

    private boolean confirmarSalida() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Salir de la partida");
        alert.setHeaderText("¿Seguro que quieres salir?");
        alert.setContentText("Se perderá el progreso de esta partida.");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void actualizarControles() {
        rollDiceBtn.setDisable(
            !juegoSvc.esMiTurno(modoMultijugador, miIndice)
            || juegoSvc.getCurrentPhase() != PHASE_MOVE);
    }

    private void actualizarStatus() {
        if (!juegoSvc.esMiTurno(modoMultijugador, miIndice)) {
            String sufijo = modoMultijugador ? " (Tú eres " + playerName(miIndice) + ")" : "";
            statusLabel.setText("Turno de " + playerName(juegoSvc.getCurrentPlayerIdx()) + ". Esperando..." + sufijo);
            return;
        }
        switch (juegoSvc.getCurrentPhase()) {
            case 0: statusLabel.setText("Tu turno. Rota a Assam y lanza el dado."); break;
            case 1: statusLabel.setText("Haz click en una casilla adyacente a Assam."); break;
            case 2: statusLabel.setText("1ra mitad lista. Haz click en la casilla contigua."); break;
        }
    }

    private void actualizarUI() {
        Label[] rl = {rugsJ1, rugsJ2, rugsJ3, rugsJ4};
        Label[] ml = {moneyJ1, moneyJ2, moneyJ3, moneyJ4};
        VBox[]  pl = {panelJ1, panelJ2, panelJ3, panelJ4};
        String[] colors = {"#e74c3c","#3498db","#2ecc71","#f39c12"};
        int[] moneyArr = juegoSvc.getMoney();
        int[] rugsArr  = juegoSvc.getRugs();
        int cpIdx = juegoSvc.getCurrentPlayerIdx();
        int np    = juegoSvc.getNumPlayers();
        for (int i = 0; i < np; i++) {
            if (rl[i] != null) rl[i].setText(String.valueOf(rugsArr[i]));
            if (ml[i] != null) ml[i].setText(moneyArr[i] + " Dh");

            boolean estaEliminado = juegoSvc.esEliminado(i);
            if (pl[i] != null) {
                if (estaEliminado) {
                    pl[i].setStyle("-fx-border-color:#555;-fx-border-width:1.5px;"
                        + "-fx-border-radius:10px;-fx-opacity:0.4;");
                } else {
                    pl[i].setStyle(i == cpIdx
                        ? "-fx-border-color:" + colors[i] + ";-fx-border-width:3px;-fx-border-radius:10px;"
                        : "-fx-border-color:" + hexToRgba(colors[i], 0.35) + ";-fx-border-width:1.5px;-fx-border-radius:10px;");
                }
            }

            if (poderesActivados && pl[i] != null) {
                pl[i].getChildren().removeIf(n ->
                    n instanceof Label && "reliquias-label".equals(n.getUserData()));

                boolean[] inv = juegoSvc.getInventarioJugador(i);
                StringBuilder sb = new StringBuilder();
                for (int r = 0; r < Reliquia.values().length; r++) {
                    if (inv[r]) sb.append(Reliquia.values()[r].icono).append(" ");
                }

                if (sb.length() > 0) {
                    Label lblRel = new Label(sb.toString().trim());
                    lblRel.setUserData("reliquias-label");
                    lblRel.setStyle("-fx-font-size:14px;-fx-text-fill:#D4A017;");
                    Tooltip tip = new Tooltip(armarTooltipReliquias(inv));
                    tip.setStyle("-fx-font-size:10px;-fx-background-color:rgba(10,4,0,0.95);-fx-text-fill:#F0D060;-fx-border-color:#C9922A;-fx-padding:6 8;");
                    Tooltip.install(lblRel, tip);
                    pl[i].getChildren().add(lblRel);
                }
            }
        }
        turnLabel.setText(juegoSvc.esMiTurno(modoMultijugador, miIndice)
            ? "TU TURNO" : "TURNO: " + playerName(cpIdx));

        if (poderesActivados && poderesLegendPanel != null) {
            boolean[] inv = juegoSvc.getInventarioJugador(cpIdx);
            for (var n : poderesLegendPanel.getChildren()) {
                if (n instanceof Label && "inventario-label".equals(n.getUserData())) {
                    StringBuilder txt = new StringBuilder("Tus reliquias:");
                    for (int r = 0; r < Reliquia.values().length; r++) {
                        if (inv[r]) txt.append("\n  ").append(Reliquia.values()[r].icono)
                            .append(" ").append(Reliquia.values()[r].nombre);
                    }
                    if (txt.length() == 13) txt.append("\n  (ninguna)");
                    ((Label) n).setText(txt.toString());
                    break;
                }
            }
        }
    }

    private String playerName(int idx) {
        String[] names = {"J1 (ROJO)","J2 (AZUL)","J3 (VERDE)","J4 (AMARILLO)"};
        return idx >= 0 && idx < names.length ? names[idx] : "J" + (idx + 1);
    }

    private String hexToRgba(String hex, double alpha) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return "rgba(" + r + "," + g + "," + b + "," + alpha + ")";
    }

    private String serializarEstado() {
        return juegoSvc.serializarEstado(assamSvc.getX(), assamSvc.getY(), assamSvc.getDir());
    }

    @FXML private void onSendChat() {
        chatCtrl.enviar();
    }

    private String armarTooltipReliquias(boolean[] inv) {
        StringBuilder sb = new StringBuilder("Reliquias:\n");
        for (int r = 0; r < Reliquia.values().length; r++) {
            if (inv[r]) {
                Reliquia rel = Reliquia.values()[r];
                sb.append(rel.icono).append(" ").append(rel.nombre).append("\n  ")
                    .append(rel.descripcion).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private void redibujarTablero() {
        if (poderesActivados)
            tableroCtrl.redibujarConReliquias(assamCtrl.getView());
        else
            tableroCtrl.redibujar(assamCtrl.getView());
    }

    private void notificarTurnoListo() {
        if (estadoSvc != null)
            estadoSvc.notificarTurnoListo(assamSvc.getX(), assamSvc.getY(),
                assamSvc.getDir(), serializarEstado());
    }

    private void guardarEstado() {
        if (estadoSvc != null && juegoSvc.esMiTurno(modoMultijugador, miIndice))
            estadoSvc.guardarEstado(assamSvc.getX(), assamSvc.getY(),
                assamSvc.getDir(), serializarEstado());
    }
}