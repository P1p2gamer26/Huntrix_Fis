package com.marrakech.game.presentation.controller;

import java.util.Random;

import com.marrakech.game.service.ChatServicio;
import com.marrakech.game.service.EstadoJuegoServicio;
import com.marrakech.game.service.EstadoJuegoServicio.EstadoDB;
import com.marrakech.game.service.JuegoServicio;
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

    private int currentPhase = 0;
    private static final int PHASE_MOVE = 0;

    private int numPlayers = 2;
    private int currentPlayerIdx = 0;
    private int[] money, rugs;

    private String  partidaId;
    private String  usuarioActual;
    private int     miIndice = 0;
    private boolean modoMultijugador = false;

    private EstadoJuegoServicio estadoSvc;
    private ChatServicio        chatSvc;
    private PartidaServicio     partidaSvc;
    private MusicaServicio      musicaSvc;

    private TableroController tableroCtrl;
    private AssamController   assamCtrl;
    private ChatController    chatCtrl;
    private FinJuegoController finJuegoCtrl;

    public void initialize() {}

    public void setServicios(MusicaServicio musicaSvc, ChatServicio chatSvc) {
        this.musicaSvc = musicaSvc;
        this.chatSvc   = chatSvc;
    }

    public void iniciarConJugadores(int n, String partidaId, String usuario,
                                    int miIndice, PartidaServicio partidaSvc) {
        this.partidaId        = partidaId;
        this.usuarioActual    = usuario;
        this.miIndice         = miIndice;
        this.modoMultijugador = true;
        this.partidaSvc       = partidaSvc;

        this.estadoSvc = new EstadoJuegoServicio(partidaId);

        startGame(n);

        if (miIndice == 0) estadoSvc.guardarEstadoSincrono(
            assamCtrl.getX(), assamCtrl.getY(), assamCtrl.getDir(), serializarEstado());
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
        numPlayers = n;
        money = new int[n]; rugs = new int[n];
        for (int i = 0; i < n; i++) { money[i] = 30; rugs[i] = 15; }

        GameRenderEngine renderEngine = new GameRenderEngine(boardGrid, diceCanvas);

        tableroCtrl = new TableroController(boardGrid, renderEngine);
        chatCtrl    = new ChatController(chatBox, chatInput, chatScroll, chatPanel, usuarioActual);
        finJuegoCtrl = new FinJuegoController(endScreen, gameScreen,
            resultadoLabel, winnerLabel, finalScores, btnVolverSala, btnVolverMenu);

        tableroCtrl.setOnCarpetPlaced(() -> pasarTurno());
        tableroCtrl.setOnGameEnded(() -> finJuegoCtrl.mostrar(
            numPlayers, money, tableroCtrl.getTileOwner(),
            modoMultijugador, miIndice, usuarioActual,
            partidaSvc, estadoSvc, chatSvc));

        finJuegoCtrl.setOnVolverSala(onVolverSala);
        finJuegoCtrl.setOnVolverMenu(onVolverMenu);

        chatCtrl.inicializar(modoMultijugador);
        if (chatSvc != null) chatCtrl.setServicios(chatSvc, partidaId);

        panelJ3.setVisible(n >= 3); panelJ3.setManaged(n >= 3);
        panelJ4.setVisible(n >= 4); panelJ4.setManaged(n >= 4);

        tableroCtrl.inicializar();
        assamCtrl = new AssamController(boardGrid);
        assamCtrl.getView().toFront();

        currentPlayerIdx = (!modoMultijugador || miIndice == 0)
            ? new Random().nextInt(numPlayers) : 0;
        currentPhase = PHASE_MOVE;
        tableroCtrl.actualizarContexto(currentPhase, currentPlayerIdx,
            assamCtrl.getX(), assamCtrl.getY(), rugs);

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
        if (!JuegoServicio.esMiTurno(modoMultijugador, currentPlayerIdx, miIndice)
            || currentPhase != PHASE_MOVE) return;
        assamCtrl.rotarIzquierda();
        guardarEstado();
    }

    @FXML protected void rotateRight() {
        if (!JuegoServicio.esMiTurno(modoMultijugador, currentPlayerIdx, miIndice)
            || currentPhase != PHASE_MOVE) return;
        assamCtrl.rotarDerecha();
        guardarEstado();
    }

    @FXML protected void onRollDiceClick() {
        if (!JuegoServicio.esMiTurno(modoMultijugador, currentPlayerIdx, miIndice)
            || currentPhase != PHASE_MOVE) return;
        int pasos = new Random().nextInt(6) + 1;
        rollDiceBtn.setDisable(true);

        diceResultLabel.setText(String.valueOf(pasos));
        diceValueLabel.setText(String.valueOf(pasos));

        GameRenderEngine renderEngine = new GameRenderEngine(boardGrid, diceCanvas);
        renderEngine.animarDado(pasos, () -> {
            assamCtrl.animarMovimiento(pasos, () -> {
                assamCtrl.getView().toFront();
                int ax = assamCtrl.getX(), ay = assamCtrl.getY();
                int pago = JuegoServicio.aplicarPago(ax, ay, currentPlayerIdx,
                                                      tableroCtrl.getTileOwner(), money);
                if (pago > 0) {
                    int dueno = tableroCtrl.getTileOwner()[ax][ay];
                    statusLabel.setText("Dado: " + pasos + " — Pagas " + pago
                        + " Dh a " + playerName(dueno - 1) + ". Coloca tu alfombra.");
                } else {
                    statusLabel.setText("Dado: " + pasos + " — Haz click en una casilla adyacente.");
                }
                currentPhase = JuegoServicio.fasePostDado(rugs[currentPlayerIdx]);
                tableroCtrl.actualizarContexto(currentPhase, currentPlayerIdx,
                    assamCtrl.getX(), assamCtrl.getY(), rugs);
                if (rugs[currentPlayerIdx] == 0) { pasarTurno(); return; }
                actualizarUI(); actualizarControles();
                guardarEstado();
            });
        });
    }

    private void pasarTurno() {
        currentPlayerIdx = JuegoServicio.siguienteTurno(currentPlayerIdx, numPlayers);
        currentPhase = PHASE_MOVE;
        tableroCtrl.redibujar(assamCtrl.getView());
        tableroCtrl.actualizarContexto(currentPhase, currentPlayerIdx,
            assamCtrl.getX(), assamCtrl.getY(), rugs);
        tableroCtrl.limpiarHighlights();
        actualizarUI(); actualizarControles(); actualizarStatus();
        guardarEstado();
    }

    private void aplicarEstadoDesdeDB() {
        String raw = estadoSvc.cargarUltimoEstado();
        if (raw == null) return;
        EstadoDB est = EstadoJuegoServicio.parsearEstado(raw);
        if (est == null || est.turno <= estadoSvc.getUltimoTurnoVisto()) return;

        assamCtrl.setPosition(est.ax, est.ay);
        assamCtrl.setDir(est.adir);
        assamCtrl.actualizarPosicionEnGrid();

        String[] secciones = est.tableroJson.split(";");
        if (secciones.length < 7) return;

        String[] ms = secciones[0].split(",");
        String[] rs = secciones[1].split(",");
        for (int i = 0; i < numPlayers && i < ms.length; i++) {
            money[i] = Integer.parseInt(ms[i]);
            rugs[i]  = Integer.parseInt(rs[i]);
        }

        int[][] owner = new int[7][7];
        String[] filas = secciones[2].split("/");
        for (int row = 0; row < 7 && row < filas.length; row++) {
            String[] celdas = filas[row].split(",");
            for (int col = 0; col < 7 && col < celdas.length; col++)
                owner[col][row] = Integer.parseInt(celdas[col]);
        }
        tableroCtrl.setTileOwner(owner);

        currentPlayerIdx = Integer.parseInt(secciones[3]);
        currentPhase     = Integer.parseInt(secciones[4]);
        tableroCtrl.setFirstCarpetX(Integer.parseInt(secciones[5]));
        tableroCtrl.setFirstCarpetY(Integer.parseInt(secciones[6]));

        if (secciones.length > 7) {
            int[][] orient = new int[7][7];
            String[] ofilas = secciones[7].split("/");
            for (int row = 0; row < 7 && row < ofilas.length; row++) {
                String[] oceldas = ofilas[row].split(",");
                for (int col = 0; col < 7 && col < oceldas.length; col++)
                    orient[col][row] = Integer.parseInt(oceldas[col]);
            }
            tableroCtrl.setCarpetOrientation(orient);
        }

        estadoSvc.setUltimoTurnoVisto(est.turno);
        estadoSvc.setEstadoVersion(est.turno);

        tableroCtrl.actualizarContexto(currentPhase, currentPlayerIdx,
            assamCtrl.getX(), assamCtrl.getY(), rugs);
        tableroCtrl.redibujar(assamCtrl.getView());
        tableroCtrl.limpiarHighlights();

        if (currentPhase == 2 && tableroCtrl.getFirstCarpetX() >= 0
            && JuegoServicio.esMiTurno(modoMultijugador, currentPlayerIdx, miIndice))
            tableroCtrl.highlightTile(tableroCtrl.getFirstCarpetX(), tableroCtrl.getFirstCarpetY());

        assamCtrl.getView().toFront();
        actualizarUI(); actualizarControles(); actualizarStatus();
        if (JuegoServicio.juegoTerminado(rugs) && !endScreen.isVisible())
            finJuegoCtrl.mostrar(numPlayers, money, tableroCtrl.getTileOwner(),
                modoMultijugador, miIndice, usuarioActual, partidaSvc, estadoSvc, chatSvc);
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
            !JuegoServicio.esMiTurno(modoMultijugador, currentPlayerIdx, miIndice)
            || currentPhase != PHASE_MOVE);
    }

    private void actualizarStatus() {
        if (!JuegoServicio.esMiTurno(modoMultijugador, currentPlayerIdx, miIndice)) {
            String sufijo = modoMultijugador ? " (Tú eres " + playerName(miIndice) + ")" : "";
            statusLabel.setText("Turno de " + playerName(currentPlayerIdx) + ". Esperando..." + sufijo);
            return;
        }
        switch (currentPhase) {
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
        for (int i = 0; i < numPlayers; i++) {
            if (rl[i] != null) rl[i].setText(String.valueOf(rugs[i]));
            if (ml[i] != null) ml[i].setText(money[i] + " Dh");
            if (pl[i] != null) pl[i].setStyle(i == currentPlayerIdx
                ? "-fx-border-color:" + colors[i] + ";-fx-border-width:3px;-fx-border-radius:10px;"
                : "-fx-border-color:" + hexToRgba(colors[i], 0.35) + ";-fx-border-width:1.5px;-fx-border-radius:10px;");
        }
        turnLabel.setText(JuegoServicio.esMiTurno(modoMultijugador, currentPlayerIdx, miIndice)
            ? "TU TURNO" : "TURNO: " + playerName(currentPlayerIdx));
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
        return EstadoJuegoServicio.serializarEstado(
            numPlayers, money, rugs, tableroCtrl.getTileOwner(),
            currentPlayerIdx, currentPhase,
            tableroCtrl.getFirstCarpetX(), tableroCtrl.getFirstCarpetY(),
            tableroCtrl.getCarpetOrientation());
    }

    @FXML private void onSendChat() {
        chatCtrl.enviar();
    }

    private void guardarEstado() {
        if (estadoSvc != null)
            estadoSvc.guardarEstado(assamCtrl.getX(), assamCtrl.getY(),
                assamCtrl.getDir(), serializarEstado());
    }
}
