package com.marrakech.game.presentation.controller;

import java.util.List;
import java.util.Random;

import com.marrakech.game.repositorio.ChatRepositorio.Mensaje;
import com.marrakech.game.repositorio.PartidaRepositorio.Partida;
import com.marrakech.game.servicios.ChatServicio;
import com.marrakech.game.servicios.EstadoJuegoServicio;
import com.marrakech.game.servicios.EstadoJuegoServicio.EstadoDB;
import com.marrakech.game.servicios.PartidaServicio;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Controlador principal del juego.
 * Delega el renderizado a {@link GameRenderEngine}, la navegación de Assam a
 * {@link AssamNavigator} y la validación de alfombras a {@link CarpetValidador}.
 */
public class GameController {

    // ── FXML ─────────────────────────────────────────────────────────────────
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

    // ── Callbacks de navegación ───────────────────────────────────────────────
    private Runnable onVolverSala;
    private Runnable onVolverMenu;

    public void setOnVolverSala(Runnable r) { this.onVolverSala = r; }
    public void setOnVolverMenu(Runnable r) { this.onVolverMenu = r; }

    // ── Estado visual de Assam ────────────────────────────────────────────────
    private ImageView assamView;
    private final Image[] assamImages = new Image[4];
    private int assamX = 3, assamY = 3, assamDir = 0;

    // ── Fases del turno ───────────────────────────────────────────────────────
    private int currentPhase    = 0;
    private static final int PHASE_MOVE     = 0;
    private static final int PHASE_CARPET_1 = 1;
    private static final int PHASE_CARPET_2 = 2;

    // ── Estado del tablero ────────────────────────────────────────────────────
    private int numPlayers = 2;
    private int currentPlayerIdx = 0;
    private int[] money, rugs;
    private final int[][] tileOwner         = new int[7][7];
    private final int[][] carpetOrientation = new int[7][7];
    private final StackPane[][] tiles       = new StackPane[7][7];
    private int firstCarpetX = -1, firstCarpetY = -1;

    private static final int CELL = 61;
    private final String[] playerColors = {"#e74c3c","#3498db","#2ecc71","#f39c12"};
    private final String[] playerNames  = {"J1 (ROJO)","J2 (AZUL)","J3 (VERDE)","J4 (AMARILLO)"};

    // ── Multijugador y servicios ──────────────────────────────────────────────
    private String  partidaId;
    private String  usuarioActual;
    private int     miIndice = 0;
    private boolean modoMultijugador = false;

    // Servicios inyectados desde AuthController a través de iniciarConJugadores
    private EstadoJuegoServicio estadoSvc;
    private ChatServicio        chatSvc;
    private PartidaServicio     partidaSvc;

    // ── Motor de renderizado ──────────────────────────────────────────────────
    private GameRenderEngine renderEngine;

    public void initialize() {}

    // ── Punto de entrada multijugador ─────────────────────────────────────────

    /**
     * Inicia la partida en modo multijugador con todos los servicios inyectados.
     */
    public void iniciarConJugadores(int n, String partidaId, String usuario,
                                    int miIndice, PartidaServicio partidaSvc,
                                    ChatServicio chatSvc) {
        this.partidaId        = partidaId;
        this.usuarioActual    = usuario;
        this.miIndice         = miIndice;
        this.modoMultijugador = true;
        this.partidaSvc       = partidaSvc;
        this.chatSvc          = chatSvc;

        this.estadoSvc = new EstadoJuegoServicio(partidaId);

        startGame(n);

        if (miIndice == 0) estadoSvc.guardarEstadoSincrono(assamX, assamY, assamDir, serializarEstado());
        estadoSvc.iniciarPolling(() -> aplicarEstadoDesdeDB());

        chatSvc.inicializar(partidaId, usuario);
        cargarHistorialChat();
        chatSvc.iniciarPolling(() -> cargarNuevosMensajes());
    }

    /** Modo local (sin base de datos). */
    public void iniciarConJugadores(int n) {
        this.modoMultijugador = false;
        startGame(n);
    }

    @FXML private void startWith2() { iniciarConJugadores(2); }
    @FXML private void startWith3() { iniciarConJugadores(3); }
    @FXML private void startWith4() { iniciarConJugadores(4); }

    // ── Inicialización del tablero ────────────────────────────────────────────

    private void startGame(int n) {
        numPlayers = n;
        money = new int[n]; rugs = new int[n];
        for (int i = 0; i < n; i++) { money[i] = 30; rugs[i] = 15; }

        renderEngine = new GameRenderEngine(boardGrid, diceCanvas);

        panelJ3.setVisible(n >= 3); panelJ3.setManaged(n >= 3);
        panelJ4.setVisible(n >= 4); panelJ4.setManaged(n >= 4);
        if (chatPanel != null) {
            chatPanel.setVisible(modoMultijugador);
            chatPanel.setManaged(modoMultijugador);
        }

        boardGrid.getChildren().clear();
        for (int row = 0; row < 7; row++)
            for (int col = 0; col < 7; col++) {
                tileOwner[col][row] = 0; carpetOrientation[col][row] = 0;
                StackPane tile = new StackPane();
                tile.setPrefSize(CELL, CELL);
                tile.getStyleClass().add("tile");
                final int r = row, c = col;
                tile.setOnMouseClicked(e -> handleTileClick(c, r));
                tiles[col][row] = tile;
                boardGrid.add(tile, col, row);
            }

        assamX = 3; assamY = 3; assamDir = 0;
        cargarImagenesAssam();
        assamView.toFront();

        currentPlayerIdx = (!modoMultijugador || miIndice == 0) ? new Random().nextInt(numPlayers) : 0;
        currentPhase = PHASE_MOVE; firstCarpetX = -1; firstCarpetY = -1;

        startScreen.setVisible(false);
        endScreen.setVisible(false);
        gameScreen.setVisible(true);

        renderEngine.dibujarDadoInicial();
        actualizarUI();
        actualizarControles();

        if (chatInput != null)
            chatInput.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) onSendChat(); });

        statusLabel.setText(modoMultijugador
            ? "Conectando... Eres " + playerNames[miIndice] + ". Espera el inicio."
            : "Rota a Assam y lanza el dado.");
    }

    private void cargarImagenesAssam() {
        String[] paths = {
            "/images/marrakesh de atras.png",
            "/images/marrakesh perfil derecho.png",
            "/images/marrakesh de frente.png",
            "/images/marrakesh perfil izquierdo.png"
        };
        for (int i = 0; i < 4; i++) {
            try { assamImages[i] = new Image(getClass().getResourceAsStream(paths[i])); }
            catch (Exception e) { assamImages[i] = null; }
        }
        Image imgInicial;
        try { imgInicial = assamImages[0] != null ? assamImages[0]
                : new Image(getClass().getResourceAsStream("/images/assam.png")); }
        catch (Exception e) { imgInicial = null; }
        assamView = new ImageView(imgInicial);
        assamView.setFitWidth(50); assamView.setFitHeight(58);
        assamView.setMouseTransparent(true);
        boardGrid.add(assamView, assamX, assamY);
    }

    // ── Acciones del jugador ──────────────────────────────────────────────────

    @FXML protected void rotateLeft() {
        if (!esMiTurno() || currentPhase != PHASE_MOVE) return;
        assamDir = (assamDir + 3) % 4;
        setAssamImage(assamDir);
        if (estadoSvc != null) estadoSvc.guardarEstado(assamX, assamY, assamDir, serializarEstado());
    }

    @FXML protected void rotateRight() {
        if (!esMiTurno() || currentPhase != PHASE_MOVE) return;
        assamDir = (assamDir + 1) % 4;
        setAssamImage(assamDir);
        if (estadoSvc != null) estadoSvc.guardarEstado(assamX, assamY, assamDir, serializarEstado());
    }

    @FXML protected void onRollDiceClick() {
        if (!esMiTurno() || currentPhase != PHASE_MOVE) return;
        int pasos = new Random().nextInt(6) + 1;
        rollDiceBtn.setDisable(true);

        diceResultLabel.setText(String.valueOf(pasos));
        diceValueLabel.setText(String.valueOf(pasos));
        renderEngine.animarDado(pasos, () -> animarMovimientoAssam(pasos, () -> {
            assamView.toFront();
            int dueno = tileOwner[assamX][assamY];
            if (dueno != 0 && dueno != currentPlayerIdx + 1) {
                int pago = CarpetValidador.contarContiguas(assamX, assamY, dueno, tileOwner);
                money[currentPlayerIdx] = Math.max(0, money[currentPlayerIdx] - pago);
                money[dueno - 1] += pago;
                statusLabel.setText("Dado: " + pasos + " — Pagas " + pago
                    + " Dh a " + playerNames[dueno - 1] + ". Coloca tu alfombra.");
            } else {
                statusLabel.setText("Dado: " + pasos + " — Haz click en una casilla adyacente.");
            }
            currentPhase = rugs[currentPlayerIdx] > 0 ? PHASE_CARPET_1 : PHASE_MOVE;
            if (rugs[currentPlayerIdx] == 0) { pasarTurno(); return; }
            actualizarUI(); actualizarControles();
            if (estadoSvc != null) estadoSvc.guardarEstado(assamX, assamY, assamDir, serializarEstado());
        }));
    }

    // ── Clics en el tablero ───────────────────────────────────────────────────

    private void handleTileClick(int x, int y) {
        if (!esMiTurno()) return;

        if (currentPhase == PHASE_CARPET_1) {
            int dx = Math.abs(assamX - x), dy = Math.abs(assamY - y);
            boolean adj     = dx <= 1 && dy <= 1 && !(dx == 0 && dy == 0);
            boolean noAssam = !(x == assamX && y == assamY);
            if (adj && noAssam && CarpetValidador.tiene2daOpcionValida(x, y, assamX, assamY)) {
                firstCarpetX = x; firstCarpetY = y;
                tiles[x][y].setStyle("-fx-background-color: rgba(255,255,255,0.25);");
                currentPhase = PHASE_CARPET_2;
                actualizarStatus();
                if (estadoSvc != null) estadoSvc.guardarEstado(assamX, assamY, assamDir, serializarEstado());
            } else if (adj && noAssam) {
                statusLabel.setText("Sin espacio para la 2da mitad. Elige otra casilla.");
            }

        } else if (currentPhase == PHASE_CARPET_2) {
            boolean adj    = Math.abs(firstCarpetX - x) + Math.abs(firstCarpetY - y) == 1;
            boolean noAssam= !(x == assamX && y == assamY);
            boolean dentro = CarpetValidador.esCarpetValida(firstCarpetX, firstCarpetY, x, y);
            boolean horiz  = (y == firstCarpetY);

            if (adj && noAssam && dentro) {
                tiles[firstCarpetX][firstCarpetY].setStyle("");
                int player = currentPlayerIdx + 1;
                CarpetValidador.repararAlfombraAfectada(firstCarpetX, firstCarpetY, carpetOrientation);
                CarpetValidador.repararAlfombraAfectada(x, y, carpetOrientation);
                tileOwner[firstCarpetX][firstCarpetY] = player;
                tileOwner[x][y]                       = player;
                int topCol = Math.min(firstCarpetX, x), topRow = Math.min(firstCarpetY, y);
                int botCol = Math.max(firstCarpetX, x), botRow = Math.max(firstCarpetY, y);
                carpetOrientation[firstCarpetX][firstCarpetY] = 0;
                carpetOrientation[x][y]                       = 0;
                carpetOrientation[topCol][topRow]             = horiz ? 2 : 1;
                carpetOrientation[botCol][botRow]             = -1;
                renderEngine.redibujarTableroCompleto(tileOwner, carpetOrientation, assamView);
                rugs[currentPlayerIdx]--;
                actualizarUI();
                if (juegoTerminado()) {
                    if (estadoSvc != null) estadoSvc.guardarEstado(assamX, assamY, assamDir, serializarEstado());
                    mostrarFinDeJuego();
                } else {
                    pasarTurno();
                }
            } else if (!dentro) {
                statusLabel.setText("Fuera del tablero. Elige otra dirección.");
            }
        }
    }

    // ── Turno ─────────────────────────────────────────────────────────────────

    private void pasarTurno() {
        firstCarpetX = -1; firstCarpetY = -1;
        currentPlayerIdx = (currentPlayerIdx + 1) % numPlayers;
        currentPhase = PHASE_MOVE;
        for (int row = 0; row < 7; row++)
            for (int col = 0; col < 7; col++)
                if (tiles[col][row] != null) tiles[col][row].setStyle("");
        actualizarUI(); actualizarControles(); actualizarStatus();
        if (estadoSvc != null) estadoSvc.guardarEstado(assamX, assamY, assamDir, serializarEstado());
    }

    // ── Animación de Assam ────────────────────────────────────────────────────

    private void animarMovimientoAssam(int pasos, Runnable onFinished) {
        int[][] path = AssamNavigator.computePath(pasos, assamX, assamY, assamDir);
        Timeline anim = new Timeline();
        for (int p = 1; p <= pasos; p++) {
            final int px = path[p][0], py = path[p][1], pd = path[p][2];
            anim.getKeyFrames().add(new KeyFrame(Duration.millis(p * 280L), e -> {
                assamX = px; assamY = py; assamDir = pd;
                GridPane.setColumnIndex(assamView, assamX);
                GridPane.setRowIndex(assamView, assamY);
                setAssamImage(assamDir);
                assamView.toFront();
            }));
        }
        anim.getKeyFrames().add(new KeyFrame(Duration.millis(pasos * 280L + 60), e -> onFinished.run()));
        anim.play();
    }

    private void setAssamImage(int dir) {
        if (assamImages[dir] != null) {
            assamView.setImage(assamImages[dir]);
            assamView.setRotate(0);
        } else {
            assamView.setRotate(dir * 90);
        }
    }

    // ── Sincronización multijugador ───────────────────────────────────────────

    private void aplicarEstadoDesdeDB() {
        String raw = estadoSvc.cargarUltimoEstado();
        if (raw == null) return;
        EstadoDB est = EstadoJuegoServicio.parsearEstado(raw);
        if (est == null || est.turno <= estadoSvc.getUltimoTurnoVisto()) return;

        assamX = est.ax; assamY = est.ay; assamDir = est.adir;
        GridPane.setColumnIndex(assamView, assamX);
        GridPane.setRowIndex(assamView, assamY);
        setAssamImage(assamDir);

        String[] secciones = est.tableroJson.split(";");
        if (secciones.length < 7) return;

        String[] ms = secciones[0].split(",");
        String[] rs = secciones[1].split(",");
        for (int i = 0; i < numPlayers && i < ms.length; i++) {
            money[i] = Integer.parseInt(ms[i]);
            rugs[i]  = Integer.parseInt(rs[i]);
        }
        String[] filas = secciones[2].split("/");
        for (int row = 0; row < 7 && row < filas.length; row++) {
            String[] celdas = filas[row].split(",");
            for (int col = 0; col < 7 && col < celdas.length; col++)
                tileOwner[col][row] = Integer.parseInt(celdas[col]);
        }
        currentPlayerIdx = Integer.parseInt(secciones[3]);
        currentPhase     = Integer.parseInt(secciones[4]);
        firstCarpetX     = Integer.parseInt(secciones[5]);
        firstCarpetY     = Integer.parseInt(secciones[6]);
        if (secciones.length > 7) {
            String[] ofilas = secciones[7].split("/");
            for (int row = 0; row < 7 && row < ofilas.length; row++) {
                String[] oceldas = ofilas[row].split(",");
                for (int col = 0; col < 7 && col < oceldas.length; col++)
                    carpetOrientation[col][row] = Integer.parseInt(oceldas[col]);
            }
        }

        estadoSvc.setUltimoTurnoVisto(est.turno);
        estadoSvc.setEstadoVersion(est.turno);

        renderEngine.redibujarTableroCompleto(tileOwner, carpetOrientation, assamView);
        limpiarHighlights();
        if (currentPhase == PHASE_CARPET_2 && firstCarpetX >= 0 && esMiTurno())
            tiles[firstCarpetX][firstCarpetY].setStyle("-fx-background-color: rgba(255,255,255,0.25);");

        assamView.toFront();
        actualizarUI(); actualizarControles(); actualizarStatus();
        if (juegoTerminado() && !endScreen.isVisible()) mostrarFinDeJuego();
    }

    private void limpiarHighlights() {
        for (int row = 0; row < 7; row++)
            for (int col = 0; col < 7; col++)
                if (tiles[col][row] != null) tiles[col][row].setStyle("");
    }

    // ── Chat ──────────────────────────────────────────────────────────────────

    private void cargarHistorialChat() {
        List<Mensaje> todos = chatSvc.cargarHistorial();
        for (Mensaje m : todos) agregarBurbuja(m);
        scrollAlFinal();
    }

    private void cargarNuevosMensajes() {
        List<Mensaje> nuevos = chatSvc.getChatRepo()
            .obtenerMensajes(partidaId, chatSvc.getUltimoMensajeId());
        for (Mensaje m : nuevos) {
            if (!m.usuario.equals(usuarioActual)) agregarBurbuja(m);
            chatSvc.setUltimoMensajeId(m.id);
        }
        scrollAlFinal();
    }

    @FXML private void onSendChat() {
        if (!modoMultijugador || chatInput == null) return;
        String texto = chatInput.getText().trim();
        if (texto.isEmpty()) return;
        chatInput.clear();
        chatSvc.enviar(texto);
        // Mostrar el propio mensaje inmediatamente
        new Thread(() -> {
            int nuevoId = chatSvc.getChatRepo().obtenerUltimoId(partidaId);
            String hora = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date());
            Mensaje m = new Mensaje(nuevoId, usuarioActual, texto, hora);
            Platform.runLater(() -> {
                chatSvc.setUltimoMensajeId(nuevoId);
                agregarBurbuja(m);
                scrollAlFinal();
            });
        }, "chat-send-local").start();
    }

    private void agregarBurbuja(Mensaje m) {
        boolean esPropio = m.usuario.equals(usuarioActual);
        Label lblTexto = new Label(m.texto);
        lblTexto.setWrapText(true); lblTexto.setMaxWidth(160);
        lblTexto.setStyle("-fx-font-size:12px;-fx-text-fill:" + (esPropio ? "#1A0A00" : "#F0E0B0") + ";");
        Label lblMeta = new Label(m.usuario + "  " + m.hora);
        lblMeta.setStyle("-fx-font-size:10px;-fx-text-fill:" + (esPropio ? "#5A3010" : "#9E7A3A") + ";");
        VBox burbuja = new VBox(2, lblTexto, lblMeta);
        burbuja.getStyleClass().add(esPropio ? "burbuja-propia" : "burbuja-ajena");
        burbuja.setMaxWidth(170);
        HBox fila = new HBox(burbuja);
        fila.setAlignment(esPropio ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        fila.setPadding(new Insets(1, 0, 1, 0));
        chatBox.getChildren().add(fila);
    }

    private void scrollAlFinal() {
        if (chatScroll != null) { chatScroll.layout(); chatScroll.setVvalue(1.0); }
    }

    // ── Fin del juego ─────────────────────────────────────────────────────────

    private boolean juegoTerminado() {
        for (int i = 0; i < numPlayers; i++) if (rugs[i] > 0) return false;
        return true;
    }

    private void mostrarFinDeJuego() {
        if (estadoSvc != null) estadoSvc.detenerPolling();
        if (chatSvc   != null) chatSvc.detenerPolling();

        int[] enTablero = new int[numPlayers];
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                if (tileOwner[c][r] > 0) enTablero[tileOwner[c][r] - 1]++;

        int win = 0;
        for (int i = 1; i < numPlayers; i++)
            if (money[i] > money[win] || (money[i] == money[win] && enTablero[i] > enTablero[win]))
                win = i;

        if (modoMultijugador && partidaSvc != null) partidaSvc.registrarVictoria(usuarioActual);

        boolean yoGane = !modoMultijugador || (win == miIndice);
        if (resultadoLabel != null) {
            resultadoLabel.setText(yoGane ? "✦  ¡HAS GANADO!  ✦" : "HAS PERDIDO");
            resultadoLabel.setStyle("-fx-font-size:54px;-fx-font-weight:bold;-fx-text-fill:"
                + (yoGane ? playerColors[win] : "#707070") + ";");
        }
        if (winnerLabel != null) {
            winnerLabel.setText("Ganador: " + playerNames[win]);
            winnerLabel.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:"
                + playerColors[win] + ";");
        }
        if (finalScores != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < numPlayers; i++) {
                sb.append(playerNames[i]).append(":  ").append(money[i]).append(" Dh")
                  .append("   |   Alfombras en tablero: ").append(enTablero[i]);
                if (i < numPlayers - 1) sb.append("\n");
            }
            finalScores.setText(sb.toString());
        }
        gameScreen.setVisible(false);
        endScreen.setVisible(true);
    }

    @FXML private void volverSala() {
        if (estadoSvc != null) estadoSvc.detenerPolling();
        if (chatSvc   != null) chatSvc.detenerPolling();
        if (onVolverSala != null) onVolverSala.run();
    }

    @FXML private void volverMenu() {
        if (estadoSvc != null) estadoSvc.detenerPolling();
        if (chatSvc   != null) chatSvc.detenerPolling();
        if (onVolverMenu != null) onVolverMenu.run();
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private boolean esMiTurno() {
        return !modoMultijugador || currentPlayerIdx == miIndice;
    }

    private void actualizarControles() {
        rollDiceBtn.setDisable(!esMiTurno() || currentPhase != PHASE_MOVE);
    }

    private void actualizarStatus() {
        if (!esMiTurno()) {
            String sufijo = modoMultijugador ? " (Tú eres " + playerNames[miIndice] + ")" : "";
            statusLabel.setText("Turno de " + playerNames[currentPlayerIdx] + ". Esperando..." + sufijo);
            return;
        }
        switch (currentPhase) {
            case PHASE_MOVE:     statusLabel.setText("Tu turno. Rota a Assam y lanza el dado."); break;
            case PHASE_CARPET_1: statusLabel.setText("Haz click en una casilla adyacente a Assam."); break;
            case PHASE_CARPET_2: statusLabel.setText("1ra mitad lista. Haz click en la casilla contigua."); break;
        }
    }

    private void actualizarUI() {
        Label[] rl = {rugsJ1, rugsJ2, rugsJ3, rugsJ4};
        Label[] ml = {moneyJ1, moneyJ2, moneyJ3, moneyJ4};
        VBox[]  pl = {panelJ1, panelJ2, panelJ3, panelJ4};
        for (int i = 0; i < numPlayers; i++) {
            if (rl[i] != null) rl[i].setText(String.valueOf(rugs[i]));
            if (ml[i] != null) ml[i].setText(money[i] + " Dh");
            if (pl[i] != null) pl[i].setStyle(i == currentPlayerIdx
                ? "-fx-border-color:" + playerColors[i] + ";-fx-border-width:3px;-fx-border-radius:10px;"
                : "-fx-border-color:" + hexToRgba(playerColors[i], 0.35) + ";-fx-border-width:1.5px;-fx-border-radius:10px;");
        }
        turnLabel.setText(esMiTurno() ? "TU TURNO" : "TURNO: " + playerNames[currentPlayerIdx]);
        turnLabel.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:"
            + (modoMultijugador ? playerColors[miIndice] : playerColors[currentPlayerIdx]) + ";");
    }

    private String hexToRgba(String hex, double alpha) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return "rgba(" + r + "," + g + "," + b + "," + alpha + ")";
    }

    // ── Serialización del estado ──────────────────────────────────────────────

    private String serializarEstado() {
        return EstadoJuegoServicio.serializarEstado(
            numPlayers, money, rugs, tileOwner, currentPlayerIdx,
            currentPhase, firstCarpetX, firstCarpetY, carpetOrientation);
    }
}
