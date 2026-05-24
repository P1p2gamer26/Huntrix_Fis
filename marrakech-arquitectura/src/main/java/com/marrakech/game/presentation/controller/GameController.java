package com.marrakech.game.presentation.controller;

import java.util.Random;

import com.marrakech.game.repository.IEstadoJuegoRepositorio;
import com.marrakech.game.service.AssamServicio;
import com.marrakech.game.service.ChatServicio;
import com.marrakech.game.service.EstadoJuegoServicio;
import com.marrakech.game.service.EstadoJuegoServicio.EstadoDB;
import com.marrakech.game.service.GestionJuegoServicio;
import com.marrakech.game.service.GestionJuegoServicio.Reliquia;
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
import javafx.scene.control.CheckBox;

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

        // Host guarda estado inicial real — guest lo recibe via polling
        if (miIndice == 0) {
            estadoSvc.guardarEstadoSincrono(
                assamSvc.getX(), assamSvc.getY(), assamSvc.getDir(),
                serializarEstado());
        }
        // Ambos arrancan polling
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
                // Primera alfombra del Sultán colocada — permitir la segunda
                sultanPendiente = false;
                statusLabel.setText("✨ Alfombra del Sultán — coloca tu segunda alfombra.");
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

        panelJ3.setVisible(n >= 3); panelJ3.setManaged(n >= 3);
        panelJ4.setVisible(n >= 4); panelJ4.setManaged(n >= 4);

        assamCtrl.getView().toFront();

        int firstPlayer = 0;
        if (!modoMultijugador) {
            firstPlayer = new Random().nextInt(n);
        }
        // En multijugador siempre empieza el host (índice 0)
        // El guest recibe el estado via polling

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

        // ══════════════════════════════════════════════════════════════════════
        // AQUÍ VAN LOS SUPERPODERES
        // (poderesActivados == true cuando el jugador activó el toggle)
        // ══════════════════════════════════════════════════════════════════════
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
            // Guardar el path antes de que animarMovimiento actualice la posición de Assam
            int[][] path = assamSvc.computePath(pasos);

            assamCtrl.animarMovimiento(pasos, () -> Platform.runLater(() -> {
                assamCtrl.getView().toFront();
                int ax = assamSvc.getX(), ay = assamSvc.getY();

                // Recoger reliquias en cada casilla del recorrido (solo si poderes activos)
                Reliquia recogida = poderesCtrl.recogerReliquiasEnRecorrido(path, pasos);
                if (recogida != null) {
                    statusLabel.setText(recogida.emoji + " ¡Recogiste: " + recogida.nombre + "!");
                    redibujarTablero();
                    actualizarUI();
                }

                // Pago + Cáliz en jerarquía correcta (sin Sultán todavía)
                PoderesController.ResultadoPost resultado =
                    poderesCtrl.resolverPostMovimiento(ax, ay, pasos);
                statusLabel.setText(resultado.mensaje);

                // Verificar eliminación ANTES de preguntar por el Sultán
                int jugadorActual = juegoSvc.getCurrentPlayerIdx();
                boolean eliminado = juegoSvc.verificarEliminacion(jugadorActual);

                // Comprobar fin de partida (por monedas o por rugs)
                if (juegoSvc.juegoTerminado()) {
                    actualizarUI(); notificarTurnoListo();
                    finJuegoCtrl.mostrar(modoMultijugador, miIndice, usuarioActual);
                    return;
                }

                if (eliminado) {
                    statusLabel.setText("💀 " + playerName(jugadorActual) + " se quedó sin monedas y fue eliminado.");
                    actualizarUI(); guardarEstado();
                    pasarTurno();
                    return;
                }

                int newPhase = juegoSvc.fasePostDado();
                juegoSvc.setCurrentPhase(newPhase);

                // Preguntar por el Sultán solo si el jugador no fue eliminado
                boolean sultanActivado = poderesCtrl.resolverSultan();
                if (sultanActivado) {
                    sultanPendiente = true;
                    statusLabel.setText("✨ Alfombra del Sultán — ¡coloca tu primera alfombra!");
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

    private void pasarTurno() {
        juegoSvc.pasarTurno();

        if (poderesActivados) juegoSvc.intentarAparecerReliquia(assamSvc.getX(), assamSvc.getY());

        redibujarTablero();
        tableroCtrl.actualizarContexto(juegoSvc.getCurrentPhase(), juegoSvc.getCurrentPlayerIdx(),
            assamSvc.getX(), assamSvc.getY(), juegoSvc.getRugs());
        tableroCtrl.limpiarHighlights();
        actualizarUI(); actualizarControles(); actualizarStatus();
        // Guarda Y notifica en un solo paso: los demás jugadores solo leen cuando esto esté listo
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

        // Es mi turno ahora — desmarcar listo para que no re-disparemos el mismo estado
        if (estadoSvc != null && juegoSvc.esMiTurno(modoMultijugador, miIndice))
            estadoSvc.desmarcarListo();

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

            // ── Inventario de reliquias ───────────────────────────────────────
            if (poderesActivados && pl[i] != null) {
                // Eliminar label de reliquias anterior si existe
                pl[i].getChildren().removeIf(n ->
                    n instanceof Label && "reliquias-label".equals(n.getUserData()));

                boolean[] inv = juegoSvc.getInventarioJugador(i);
                StringBuilder sb = new StringBuilder();
                for (int r = 0; r < Reliquia.values().length; r++)
                    if (inv[r]) sb.append(Reliquia.values()[r].emoji).append(" ");

                if (sb.length() > 0) {
                    Label lblRel = new Label(sb.toString().trim());
                    lblRel.setUserData("reliquias-label");
                    lblRel.setStyle("-fx-font-size:14px;-fx-text-fill:#D4A017;");
                    pl[i].getChildren().add(lblRel);
                }
            }
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

    private void redibujarTablero() {
        if (poderesActivados)
            tableroCtrl.redibujarConReliquias(assamCtrl.getView());
        else
            tableroCtrl.redibujar(assamCtrl.getView());
    }

    /** Guarda el estado Y lo marca como listo — los otros jugadores leen solo cuando se llama esto. */
    private void notificarTurnoListo() {
        if (estadoSvc != null)
            estadoSvc.notificarTurnoListo(assamSvc.getX(), assamSvc.getY(),
                assamSvc.getDir(), serializarEstado());
    }

    /** Guardado simple sin marcar listo (casos terminales: fin de juego, eliminación). */
    private void guardarEstado() {
        if (estadoSvc != null && juegoSvc.esMiTurno(modoMultijugador, miIndice))
            estadoSvc.guardarEstado(assamSvc.getX(), assamSvc.getY(),
                assamSvc.getDir(), serializarEstado());
    }
}