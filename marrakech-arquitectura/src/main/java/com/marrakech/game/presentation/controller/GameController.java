package com.marrakech.game.presentation.controller;

import java.util.Random;

import com.marrakech.game.repository.IEstadoJuegoRepositorio;
import com.marrakech.game.service.AssamServicio;
import com.marrakech.game.service.ChatServicio;
import com.marrakech.game.service.EstadoJuegoServicio;
import com.marrakech.game.service.EstadoJuegoServicio.EstadoDB;
import com.marrakech.game.service.GestionJuegoServicio;
import com.marrakech.game.service.MusicaServicio;
import com.marrakech.game.service.PartidaServicio;
import com.marrakech.game.presentation.render.GameRenderEngine;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

public class GameController {

    @FXML private VBox      startScreen;
    @FXML private HBox      gameScreen;
    @FXML private StackPane endScreen;
    @FXML private GridPane  boardGrid;
    @FXML private Label     turnLabel, statusLabel, diceResultLabel, diceValueLabel;
    @FXML private Button    rollDiceBtn;
    @FXML private VBox      panelJ1, panelJ2, panelJ3, panelJ4;
    @FXML private Label     rugsJ1, rugsJ2, rugsJ3, rugsJ4;
    @FXML private Label     moneyJ1, moneyJ2, moneyJ3, moneyJ4;
    @FXML private Label     resultadoLabel, winnerLabel, finalScores;
    @FXML private Button    btnVolverSala, btnVolverMenu;
    @FXML private Canvas    diceCanvas;
    @FXML private VBox      chatPanel, chatBox;
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

    public void initialize() {}

    public void setServicios(MusicaServicio musicaSvc, ChatServicio chatSvc) {
        this.musicaSvc = musicaSvc;
        this.chatSvc   = chatSvc;
    }

    public void setEstadoRepositorio(IEstadoJuegoRepositorio estadoRepo) {
        this.estadoRepo = estadoRepo;
    }

    public void iniciarConJugadores(int n, String partidaId, String usuario,
                                    int miIndice, PartidaServicio partidaSvc) {
        this.partidaId        = partidaId;
        this.usuarioActual    = usuario;
        this.miIndice         = miIndice;
        this.modoMultijugador = true;
        this.partidaSvc       = partidaSvc;

        this.estadoSvc = new EstadoJuegoServicio(estadoRepo, partidaId);

        startGame(n);

        if (miIndice == 0) estadoSvc.guardarEstadoSincrono(
            assamSvc.getX(), assamSvc.getY(), assamSvc.getDir(), serializarEstado());
        estadoSvc.iniciarPolling(() -> aplicarEstadoDesdeDB());

        chatSvc.inicializar(partidaId, usuario);
        chatCtrl.cargarHistorial();
        chatSvc.iniciarPolling(() -> chatCtrl.cargarNuevos());
    }

    public void iniciarConJugadores(int n) {
        this.modoMultijugador = false;
        startGame(n);
    }

    @FXML private void startWith2() { iniciarConJugadores(2); }
    @FXML private void startWith3() { iniciarConJugadores(3); }
    @FXML private void startWith4() { iniciarConJugadores(4); }

    private void startGame(int n) {
        juegoSvc = new GestionJuegoServicio();
        juegoSvc.iniciarJuego(n);

        assamSvc = new AssamServicio();

        GameRenderEngine renderEngine = new GameRenderEngine(boardGrid, diceCanvas);

        tableroCtrl = new TableroController(boardGrid, renderEngine, juegoSvc);
        tableroCtrl.inicializar();
        tableroCtrl.limpiarHighlights();

        assamCtrl    = new AssamController(assamSvc, boardGrid);
        chatCtrl     = new ChatController(chatBox, chatInput, chatScroll, chatPanel, usuarioActual);
        finJuegoCtrl = new FinJuegoController(endScreen, gameScreen,
            resultadoLabel, winnerLabel, finalScores, btnVolverSala, btnVolverMenu,
            juegoSvc, chatSvc);

        if (partidaSvc != null || estadoSvc != null)
            finJuegoCtrl.setServicios(partidaSvc, estadoSvc);

        tableroCtrl.setOnCarpetPlaced(() -> pasarTurno());
        tableroCtrl.setOnGameEnded(() -> finJuegoCtrl.mostrar(
            modoMultijugador, miIndice, usuarioActual));
        tableroCtrl.setOnAlfombraCompleta(() ->
            statusLabel.setText("¡No puedes tapar una alfombra completa! Elige otra casilla.")
        );

        finJuegoCtrl.setOnVolverSala(onVolverSala);
        finJuegoCtrl.setOnVolverMenu(onVolverMenu);

        chatCtrl.inicializar(modoMultijugador);
        if (chatSvc != null) chatCtrl.setServicios(chatSvc, partidaId);

        panelJ3.setVisible(n >= 3); panelJ3.setManaged(n >= 3);
        panelJ4.setVisible(n >= 4); panelJ4.setManaged(n >= 4);

        assamCtrl.getView().toFront();

        int firstPlayer = (!modoMultijugador || miIndice == 0)
            ? new Random().nextInt(n) : 0;
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
        guardarEstado();
    }

    @FXML protected void rotateRight() {
        if (!juegoSvc.esMiTurno(modoMultijugador, miIndice)
            || juegoSvc.getCurrentPhase() != PHASE_MOVE) return;
        assamCtrl.rotarDerecha();
        guardarEstado();
    }

    @FXML protected void onRollDiceClick() {
        if (!juegoSvc.esMiTurno(modoMultijugador, miIndice)
            || juegoSvc.getCurrentPhase() != PHASE_MOVE) return;
        int pasos = new Random().nextInt(6) + 1;
        rollDiceBtn.setDisable(true);

        diceResultLabel.setText(String.valueOf(pasos));
        diceValueLabel.setText(String.valueOf(pasos));

        GameRenderEngine renderEngine = new GameRenderEngine(boardGrid, diceCanvas);
        renderEngine.animarDado(pasos, () -> {
            assamCtrl.animarMovimiento(pasos, () -> {
                assamCtrl.getView().toFront();
                int ax = assamSvc.getX(), ay = assamSvc.getY();
                int pago = juegoSvc.aplicarPago(ax, ay);
                if (pago > 0) {
                    int dueno = juegoSvc.getTileOwner()[ax][ay];
                    statusLabel.setText("Dado: " + pasos + " — Pagas " + pago
                        + " Dh a " + playerName(dueno - 1) + ". Coloca tu alfombra.");
                } else {
                    statusLabel.setText("Dado: " + pasos + " — Haz click en una casilla adyacente.");
                }
                int newPhase = juegoSvc.fasePostDado();
                juegoSvc.setCurrentPhase(newPhase);
                tableroCtrl.actualizarContexto(newPhase, juegoSvc.getCurrentPlayerIdx(),
                    assamSvc.getX(), assamSvc.getY(), juegoSvc.getRugs());
                if (juegoSvc.getRugs()[juegoSvc.getCurrentPlayerIdx()] == 0) { pasarTurno(); return; }
                actualizarUI(); actualizarControles();
                guardarEstado();
            });
        });
    }

    private void pasarTurno() {
        juegoSvc.pasarTurno();
        tableroCtrl.redibujar(assamCtrl.getView());
        tableroCtrl.actualizarContexto(juegoSvc.getCurrentPhase(), juegoSvc.getCurrentPlayerIdx(),
            assamSvc.getX(), assamSvc.getY(), juegoSvc.getRugs());
        tableroCtrl.limpiarHighlights();
        actualizarUI(); actualizarControles(); actualizarStatus();
        guardarEstado();
    }

    private void aplicarEstadoDesdeDB() {
        String raw = estadoSvc.cargarUltimoEstado();
        if (raw == null) return;
        EstadoDB est = EstadoJuegoServicio.parsearEstado(raw);
        if (est == null || est.turno <= estadoSvc.getUltimoTurnoVisto()) return;

        assamSvc.setPosition(est.ax, est.ay);
        assamSvc.setDir(est.adir);
        assamCtrl.actualizarPosicionEnGrid();

        juegoSvc.aplicarEstado(raw);

        estadoSvc.setUltimoTurnoVisto(est.turno);
        estadoSvc.setEstadoVersion(est.turno);

        tableroCtrl.actualizarContexto(juegoSvc.getCurrentPhase(), juegoSvc.getCurrentPlayerIdx(),
            assamSvc.getX(), assamSvc.getY(), juegoSvc.getRugs());
        tableroCtrl.redibujar(assamCtrl.getView());
        tableroCtrl.limpiarHighlights();

        if (juegoSvc.getCurrentPhase() == 2 && juegoSvc.getFirstCarpetX() >= 0
            && juegoSvc.esMiTurno(modoMultijugador, miIndice))
            tableroCtrl.highlightTile(juegoSvc.getFirstCarpetX(), juegoSvc.getFirstCarpetY());

        assamCtrl.getView().toFront();
        actualizarUI(); actualizarControles(); actualizarStatus();
        if (juegoSvc.juegoTerminado() && !endScreen.isVisible())
            finJuegoCtrl.mostrar(modoMultijugador, miIndice, usuarioActual);
    }

    @FXML private void volverSala() {
        if (!confirmarSalida()) return;
        if (estadoSvc != null) estadoSvc.detenerPolling();
        if (chatSvc   != null) chatSvc.detenerPolling();
        if (onVolverSala != null) onVolverSala.run();
    }

    @FXML private void volverMenu() {
        if (!confirmarSalida()) return;
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
            if (pl[i] != null) pl[i].setStyle(i == cpIdx
                ? "-fx-border-color:" + colors[i] + ";-fx-border-width:3px;-fx-border-radius:10px;"
                : "-fx-border-color:" + hexToRgba(colors[i], 0.35) + ";-fx-border-width:1.5px;-fx-border-radius:10px;");
        }
        turnLabel.setText(juegoSvc.esMiTurno(modoMultijugador, miIndice)
            ? "TU TURNO" : "TURNO: " + playerName(cpIdx));
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

    private void guardarEstado() {
        if (estadoSvc != null)
            estadoSvc.guardarEstado(assamSvc.getX(), assamSvc.getY(),
                assamSvc.getDir(), serializarEstado());
    }
}