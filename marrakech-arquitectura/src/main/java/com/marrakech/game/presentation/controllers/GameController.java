package com.marrakech.game.presentation.controllers;

import com.marrakech.game.infrastructure.ChatRepository;
import com.marrakech.game.infrastructure.ChatRepository.Mensaje;
import com.marrakech.game.infrastructure.PartidaRepository;
import com.marrakech.game.infrastructure.database.DatabaseConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.*;
import java.util.List;
import java.util.Random;

public class GameController {

    // ── FXML ─────────────────────────────────────────────────────────────────
    @FXML private VBox      startScreen;
    @FXML private HBox      gameScreen;
    @FXML private VBox      endScreen;
    @FXML private GridPane  boardGrid;
    @FXML private Label     turnLabel;
    @FXML private Label     statusLabel;
    @FXML private Button    rollDiceBtn;
    @FXML private VBox      panelJ1, panelJ2, panelJ3, panelJ4;
    @FXML private Label     rugsJ1, rugsJ2, rugsJ3, rugsJ4;
    @FXML private Label     moneyJ1, moneyJ2, moneyJ3, moneyJ4;
    @FXML private Label     winnerLabel;
    @FXML private Label     finalScores;

    // Chat FXML
    @FXML private VBox       chatPanel;
    @FXML private VBox       chatBox;
    @FXML private ScrollPane chatScroll;
    @FXML private TextField  chatInput;
    @FXML private Button     chatSendBtn;

    // ── Estado del juego ──────────────────────────────────────────────────────
    private ImageView assamView;
    private int assamX = 3, assamY = 3, assamDir = 0;

    // Fase como int para poder serializarla
    private int currentPhase = 0;
    private static final int PHASE_MOVE     = 0;
    private static final int PHASE_CARPET_1 = 1;
    private static final int PHASE_CARPET_2 = 2;

    private int numPlayers = 2;
    private int currentPlayerIdx = 0;
    private int[] money, rugs;
    private Image[] carpetImages;
    private int[][] tileOwner          = new int[7][7];
    private int[][] carpetOrientation  = new int[7][7];
    private StackPane[][] tiles        = new StackPane[7][7];
    private int firstCarpetX = -1, firstCarpetY = -1;

    private static final int CELL = 61;
    private final String[] playerColors = {"#e74c3c","#3498db","#2ecc71","#f39c12"};
    private final String[] playerNames  = {"J1 (ROJO)","J2 (AZUL)","J3 (VERDE)","J4 (AMARILLO)"};

    // ── Multijugador ──────────────────────────────────────────────────────────
    private String   partidaId;
    private String   usuarioActual;
    private int      miIndice = 0;
    private int      ultimoTurnoVisto = -1;
    private int      estadoVersion    = 0;
    private Timeline pollingTimeline;
    private boolean  modoMultijugador = false;
    private static boolean tablaEstadoCreada = false;

    // ── Chat ──────────────────────────────────────────────────────────────────
    private int      ultimoMensajeId = 0;
    private Timeline chatTimeline;

    public void initialize() {}

    // ── Inicio ────────────────────────────────────────────────────────────────

    public void iniciarConJugadores(int n, String partidaId, String usuario, int miIndice) {
        this.partidaId        = partidaId;
        this.usuarioActual    = usuario;
        this.miIndice         = miIndice;
        this.modoMultijugador = true;
        startGame(n);
        // Solo el host guarda el estado inicial
        if (miIndice == 0) guardarEstadoSincrono();
        iniciarPolling();
        // Chat
        ChatRepository.inicializarTabla();
        ultimoMensajeId = ChatRepository.obtenerUltimoId(partidaId);
        cargarHistorialChat();
        iniciarChatPolling();
    }

    public void iniciarConJugadores(int n) {
        this.modoMultijugador = false;
        startGame(n);
    }

    @FXML private void startWith2() { iniciarConJugadores(2); }
    @FXML private void startWith3() { iniciarConJugadores(3); }
    @FXML private void startWith4() { iniciarConJugadores(4); }

    // ── startGame ─────────────────────────────────────────────────────────────

    private void startGame(int n) {
        numPlayers = n;
        money = new int[n]; rugs = new int[n];
        for (int i = 0; i < n; i++) { money[i] = 30; rugs[i] = 15; }

        String[] imgPaths = {
            "/images/alfombra tablero roja.png", "/images/alfombra tablero azul.png",
            "/images/alfombra tablero morada.png", "/images/alfombra tablero amarillo.png"
        };
        carpetImages = new Image[n];
        for (int i = 0; i < n; i++) {
            try { carpetImages[i] = new Image(getClass().getResourceAsStream(imgPaths[i])); }
            catch (Exception e) { System.out.println("Sin imagen J" + (i + 1)); }
        }

        panelJ3.setVisible(n >= 3); panelJ3.setManaged(n >= 3);
        panelJ4.setVisible(n >= 4); panelJ4.setManaged(n >= 4);

        if (chatPanel != null) {
            chatPanel.setVisible(modoMultijugador);
            chatPanel.setManaged(modoMultijugador);
        }

        boardGrid.getChildren().clear();
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                tileOwner[col][row]        = 0;
                carpetOrientation[col][row] = 0;
                StackPane tile = new StackPane();
                tile.setPrefSize(CELL, CELL);
                tile.getStyleClass().add("tile");
                final int r = row, c = col;
                tile.setOnMouseClicked(e -> handleTileClick(c, r));
                tiles[col][row] = tile;
                boardGrid.add(tile, col, row);
            }
        }

        assamX = 3; assamY = 3; assamDir = 0;
        try { assamView = new ImageView(new Image(getClass().getResourceAsStream("/images/assam.png"))); }
        catch (Exception e) { assamView = new ImageView(); }
        assamView.setFitWidth(50); assamView.setFitHeight(58);
        assamView.setMouseTransparent(true);
        boardGrid.add(assamView, assamX, assamY);

        currentPlayerIdx = (!modoMultijugador || miIndice == 0) ? new Random().nextInt(numPlayers) : 0;
        currentPhase     = PHASE_MOVE;
        firstCarpetX     = -1; firstCarpetY = -1;

        startScreen.setVisible(false);
        endScreen.setVisible(false);
        gameScreen.setVisible(true);
        actualizarUI();
        actualizarControles();

        if (chatInput != null) {
            chatInput.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) onSendChat(); });
        }

        if (modoMultijugador) {
            statusLabel.setText("Conectando... Eres " + playerNames[miIndice] + ". Espera el inicio.");
        } else {
            statusLabel.setText("Rota a Assam y lanza el dado.");
        }
    }

    // ── CHAT ──────────────────────────────────────────────────────────────────

    private void cargarHistorialChat() {
        List<Mensaje> todos = ChatRepository.obtenerMensajes(partidaId, 0);
        for (Mensaje m : todos) agregarBurbuja(m);
        if (!todos.isEmpty()) ultimoMensajeId = todos.get(todos.size() - 1).id;
        scrollAlFinal();
    }

    private void iniciarChatPolling() {
        chatTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            new Thread(() -> {
                List<Mensaje> nuevos = ChatRepository.obtenerMensajes(partidaId, ultimoMensajeId);
                if (!nuevos.isEmpty()) {
                    Platform.runLater(() -> {
                        for (Mensaje m : nuevos) {
                            if (!m.usuario.equals(usuarioActual)) agregarBurbuja(m);
                            ultimoMensajeId = m.id;
                        }
                        scrollAlFinal();
                    });
                }
            }, "chat-poller").start();
        }));
        chatTimeline.setCycleCount(Timeline.INDEFINITE);
        chatTimeline.play();
    }

    @FXML private void onSendChat() {
        if (!modoMultijugador || chatInput == null) return;
        String texto = chatInput.getText().trim();
        if (texto.isEmpty()) return;
        chatInput.clear();
        new Thread(() -> {
            ChatRepository.enviarMensaje(partidaId, usuarioActual, texto);
            int nuevoId = ChatRepository.obtenerUltimoId(partidaId);
            String hora = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date());
            Mensaje m = new Mensaje(nuevoId, usuarioActual, texto, hora);
            Platform.runLater(() -> {
                ultimoMensajeId = nuevoId;
                agregarBurbuja(m);
                scrollAlFinal();
            });
        }, "chat-send").start();
    }

    private void agregarBurbuja(Mensaje m) {
        boolean esPropio = m.usuario.equals(usuarioActual);
        Label lblTexto = new Label(m.texto);
        lblTexto.setWrapText(true);
        lblTexto.setMaxWidth(160);
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

    // ── Polling estado juego (hilo de fondo para no bloquear UI) ─────────────

    private void iniciarPolling() {
        pollingTimeline = new Timeline(new KeyFrame(Duration.millis(1200), e -> {
            new Thread(() -> {
                String raw = cargarUltimoEstadoDB();
                if (raw == null) return;
                EstadoDB est = parsearEstado(raw);
                if (est != null && est.turno > ultimoTurnoVisto) {
                    Platform.runLater(() -> aplicarEstado(est));
                }
            }, "db-poller").start();
        }));
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
    }

    // ── Persistencia ──────────────────────────────────────────────────────────

    private void ensureTable(Connection conn) throws Exception {
        if (!tablaEstadoCreada) {
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS estado_juego (" +
                "partida_id VARCHAR(20), turno_numero INT, assam_x INT, assam_y INT, " +
                "assam_dir INT, tablero TEXT, ts TIMESTAMP, " +
                "PRIMARY KEY (partida_id, turno_numero))");
            tablaEstadoCreada = true;
        }
    }

    private void guardarEstadoSincrono() {
        if (!modoMultijugador || partidaId == null) return;
        estadoVersion++;
        final String json = serializarEstado();
        final int turno   = estadoVersion;
        final int ax = assamX, ay = assamY, adir = assamDir;
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                ensureTable(conn);
                try (PreparedStatement ps = conn.prepareStatement(
                    "MERGE INTO estado_juego " +
                    "(partida_id,turno_numero,assam_x,assam_y,assam_dir,tablero,ts) " +
                    "KEY(partida_id,turno_numero) VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)")) {
                    ps.setString(1, partidaId); ps.setInt(2, turno);
                    ps.setInt(3, ax); ps.setInt(4, ay);
                    ps.setInt(5, adir); ps.setString(6, json);
                    ps.executeUpdate();
                    Platform.runLater(() -> ultimoTurnoVisto = turno);
                }
            } catch (Exception ex) {
                System.err.println("Error guardando estado inicial: " + ex.getMessage());
            } finally { latch.countDown(); }
        }, "db-init-writer").start();
        try { latch.await(3, java.util.concurrent.TimeUnit.SECONDS); }
        catch (InterruptedException ignored) {}
    }

    private void guardarEstado() {
        if (!modoMultijugador || partidaId == null) return;
        estadoVersion++;
        final String json = serializarEstado();
        final int turno   = estadoVersion;
        final int ax = assamX, ay = assamY, adir = assamDir;
        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                ensureTable(conn);
                try (PreparedStatement ps = conn.prepareStatement(
                    "MERGE INTO estado_juego " +
                    "(partida_id,turno_numero,assam_x,assam_y,assam_dir,tablero,ts) " +
                    "KEY(partida_id,turno_numero) VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)")) {
                    ps.setString(1, partidaId); ps.setInt(2, turno);
                    ps.setInt(3, ax); ps.setInt(4, ay);
                    ps.setInt(5, adir); ps.setString(6, json);
                    ps.executeUpdate();
                    Platform.runLater(() -> ultimoTurnoVisto = turno);
                }
            } catch (Exception ex) {
                System.err.println("Error guardando estado (no crítico): " + ex.getMessage());
            }
        }, "db-writer").start();
    }

    private String cargarUltimoEstadoDB() {
        if (!modoMultijugador || partidaId == null) return null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT turno_numero,assam_x,assam_y,assam_dir,tablero " +
                "FROM estado_juego WHERE partida_id=? ORDER BY turno_numero DESC LIMIT 1")) {
            ps.setString(1, partidaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("turno_numero") + "|" + rs.getInt("assam_x") + "|" +
                       rs.getInt("assam_y") + "|" + rs.getInt("assam_dir") + "|" +
                       rs.getString("tablero");
        } catch (Exception e) { System.err.println("Error leyendo estado: " + e.getMessage()); }
        return null;
    }

    // ── Serialización ─────────────────────────────────────────────────────────
    // Formato: money;rugs;tablero/;playerIdx;phase;firstCarpetX;firstCarpetY;orientaciones/

    private String serializarEstado() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numPlayers; i++) sb.append(money[i]).append(i<numPlayers-1?",":"");
        sb.append(";");
        for (int i = 0; i < numPlayers; i++) sb.append(rugs[i]).append(i<numPlayers-1?",":"");
        sb.append(";");
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) sb.append(tileOwner[col][row]).append(",");
            sb.append("/");
        }
        sb.append(";").append(currentPlayerIdx);
        sb.append(";").append(currentPhase);
        sb.append(";").append(firstCarpetX);
        sb.append(";").append(firstCarpetY);
        sb.append(";");
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) sb.append(carpetOrientation[col][row]).append(",");
            sb.append("/");
        }
        return sb.toString();
    }

    private static class EstadoDB { int turno, ax, ay, adir; String tableroJson; }

    private EstadoDB parsearEstado(String raw) {
        try {
            String[] p = raw.split("\\|", 5);
            EstadoDB e = new EstadoDB();
            e.turno = Integer.parseInt(p[0]); e.ax = Integer.parseInt(p[1]);
            e.ay    = Integer.parseInt(p[2]); e.adir = Integer.parseInt(p[3]);
            e.tableroJson = p[4]; return e;
        } catch (Exception ex) { return null; }
    }

    private void aplicarEstado(EstadoDB est) {
        assamX = est.ax; assamY = est.ay; assamDir = est.adir;
        GridPane.setColumnIndex(assamView, assamX);
        GridPane.setRowIndex(assamView, assamY);
        assamView.setRotate(assamDir * 90);

        String[] secciones = est.tableroJson.split(";");
        if (secciones.length < 7) return;

        String[] ms = secciones[0].split(",");
        String[] rs = secciones[1].split(",");
        for (int i = 0; i < numPlayers && i < ms.length; i++) {
            money[i] = Integer.parseInt(ms[i]);
            rugs[i]  = Integer.parseInt(rs[i]);
        }

        // Cargar tileOwner
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

        // Cargar orientaciones
        if (secciones.length > 7) {
            String[] ofilas = secciones[7].split("/");
            for (int row = 0; row < 7 && row < ofilas.length; row++) {
                String[] oceldas = ofilas[row].split(",");
                for (int col = 0; col < 7 && col < oceldas.length; col++)
                    carpetOrientation[col][row] = Integer.parseInt(oceldas[col]);
            }
        }

        estadoVersion    = est.turno;
        ultimoTurnoVisto = est.turno;

        // Redibujar tablero completo desde orientaciones
        redibujarTableroCompleto();

        // Limpiar resaltados y aplicar si corresponde
        for (int row = 0; row < 7; row++)
            for (int col = 0; col < 7; col++)
                if (tiles[col][row] != null) tiles[col][row].setStyle("");

        if (currentPhase == PHASE_CARPET_2 && firstCarpetX >= 0 && esMiTurno())
            tiles[firstCarpetX][firstCarpetY].setStyle("-fx-background-color: rgba(255,255,255,0.25);");

        assamView.toFront();
        actualizarUI();
        actualizarControles();
        actualizarStatus();
    }

    // ── Redibujado completo sin duplicados ────────────────────────────────────

    private void redibujarTableroCompleto() {
        java.util.List<javafx.scene.Node> eliminar = new java.util.ArrayList<>();
        for (javafx.scene.Node n : boardGrid.getChildren())
            if (n instanceof ImageView && n != assamView) eliminar.add(n);
        boardGrid.getChildren().removeAll(eliminar);

        for (int row = 0; row < 7; row++)
            for (int col = 0; col < 7; col++) {
                int ori = carpetOrientation[col][row];
                if ((ori == 1 || ori == 2 || ori == 3) && tileOwner[col][row] > 0)
                    redibujarCelda(col, row, tileOwner[col][row]);
            }
        assamView.toFront();
    }

    private void redibujarCelda(int col, int row, int player) {
        if (carpetImages == null || carpetImages[player - 1] == null) return;
        int ori = carpetOrientation[col][row];
        if (ori == 0 || ori == -1) return;
        if (ori == 3) {
            colocarImagenAlfombraSpan(col, row, false, player, false); return;
        }
        colocarImagenAlfombra(col, row, ori == 2, player);
    }

    // ── Lógica del juego ──────────────────────────────────────────────────────

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
            case PHASE_MOVE:
                statusLabel.setText("Tu turno. Rota a Assam y lanza el dado."); break;
            case PHASE_CARPET_1:
                statusLabel.setText("Haz click en una casilla adyacente a Assam para la 1ra mitad."); break;
            case PHASE_CARPET_2:
                statusLabel.setText("1ra mitad lista. Haz click en la casilla contigua para la 2da mitad."); break;
        }
    }

    @FXML protected void rotateLeft() {
        if (!esMiTurno() || currentPhase != PHASE_MOVE) return;
        assamDir = (assamDir + 3) % 4; assamView.setRotate(assamDir * 90); guardarEstado();
    }

    @FXML protected void rotateRight() {
        if (!esMiTurno() || currentPhase != PHASE_MOVE) return;
        assamDir = (assamDir + 1) % 4; assamView.setRotate(assamDir * 90); guardarEstado();
    }

    @FXML protected void onRollDiceClick() {
        if (!esMiTurno() || currentPhase != PHASE_MOVE) return;
        int pasos = new Random().nextInt(4) + 1;
        moverAssam(pasos);
        GridPane.setColumnIndex(assamView, assamX);
        GridPane.setRowIndex(assamView, assamY);
        assamView.toFront();

        int dueno = tileOwner[assamX][assamY];
        if (dueno != 0 && dueno != currentPlayerIdx + 1) {
            int pago = contarContiguas(assamX, assamY, dueno);
            money[currentPlayerIdx] = Math.max(0, money[currentPlayerIdx] - pago);
            money[dueno - 1] += pago;
            statusLabel.setText("Dado: " + pasos + " — Pagas " + pago + " Dh a " + playerNames[dueno - 1] + ". Coloca tu alfombra.");
        } else {
            statusLabel.setText("Dado: " + pasos + " — Haz click en una casilla adyacente a Assam.");
        }

        if (rugs[currentPlayerIdx] > 0) {
            currentPhase = PHASE_CARPET_1;
        } else {
            pasarTurno(); return;
        }
        actualizarUI(); actualizarControles(); guardarEstado();
    }

    private void handleTileClick(int x, int y) {
        if (!esMiTurno()) return;

        if (currentPhase == PHASE_CARPET_1) {
            int dx = Math.abs(assamX - x), dy = Math.abs(assamY - y);
            boolean adj     = dx <= 1 && dy <= 1 && !(dx == 0 && dy == 0);
            boolean noAssam = !(x == assamX && y == assamY);
            if (adj && noAssam && tiene2daOpcionValida(x, y)) {
                firstCarpetX = x; firstCarpetY = y;
                tiles[x][y].setStyle("-fx-background-color: rgba(255,255,255,0.25);");
                currentPhase = PHASE_CARPET_2;
                actualizarStatus(); guardarEstado();
            } else if (adj && noAssam) {
                statusLabel.setText("Sin espacio para la 2da mitad. Elige otra casilla.");
            }

        } else if (currentPhase == PHASE_CARPET_2) {
            boolean adj     = Math.abs(firstCarpetX - x) + Math.abs(firstCarpetY - y) == 1;
            boolean noAssam = !(x == assamX && y == assamY);
            boolean dentro  = esCarpetValida(firstCarpetX, firstCarpetY, x, y);
            boolean horiz   = (y == firstCarpetY);

            if (adj && noAssam && dentro) {
                tiles[firstCarpetX][firstCarpetY].setStyle("");
                int player = currentPlayerIdx + 1;

                repararAlfombraAfectada(firstCarpetX, firstCarpetY);
                repararAlfombraAfectada(x, y);

                tileOwner[firstCarpetX][firstCarpetY] = player;
                tileOwner[x][y]                       = player;
                int topCol  = Math.min(firstCarpetX, x);
                int topRow  = Math.min(firstCarpetY, y);
                int bot2Col = Math.max(firstCarpetX, x);
                int bot2Row = Math.max(firstCarpetY, y);
                carpetOrientation[firstCarpetX][firstCarpetY] = 0;
                carpetOrientation[x][y]                       = 0;
                carpetOrientation[topCol][topRow]             = horiz ? 2 : 1;
                carpetOrientation[bot2Col][bot2Row]           = -1;

                redibujarTableroCompleto();
                rugs[currentPlayerIdx]--;
                actualizarUI();
                if (juegoTerminado()) { guardarEstado(); mostrarFinDeJuego(); }
                else pasarTurno();
            } else if (!dentro) {
                statusLabel.setText("Fuera del tablero. Elige otra dirección.");
            }
        }
    }

    private void repararAlfombraAfectada(int col, int row) {
        int ori = carpetOrientation[col][row];
        if (ori == 0 || ori == 3) return;
        if (ori == 1) {
            if (row + 1 < 7 && carpetOrientation[col][row + 1] == -1)
                carpetOrientation[col][row + 1] = 3;
        } else if (ori == 2) {
            if (col + 1 < 7 && carpetOrientation[col + 1][row] == -1)
                carpetOrientation[col + 1][row] = 3;
        } else if (ori == -1) {
            if (row > 0 && carpetOrientation[col][row - 1] == 1)
                carpetOrientation[col][row - 1] = 3;
            else if (col > 0 && carpetOrientation[col - 1][row] == 2)
                carpetOrientation[col - 1][row] = 3;
        }
        carpetOrientation[col][row] = 0;
    }

    private void pasarTurno() {
        firstCarpetX = -1; firstCarpetY = -1;
        currentPlayerIdx = (currentPlayerIdx + 1) % numPlayers;
        currentPhase = PHASE_MOVE;
        for (int row = 0; row < 7; row++)
            for (int col = 0; col < 7; col++)
                if (tiles[col][row] != null) tiles[col][row].setStyle("");
        actualizarUI(); actualizarControles(); actualizarStatus(); guardarEstado();
    }

    private boolean esCarpetValida(int x1, int y1, int x2, int y2) {
        return x1>=0&&x1<=6&&y1>=0&&y1<=6&&x2>=0&&x2<=6&&y2>=0&&y2<=6;
    }

    private boolean tiene2daOpcionValida(int x1, int y1) {
        for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
            int x2 = x1+d[0], y2 = y1+d[1];
            if (x2==assamX && y2==assamY) continue;
            if (esCarpetValida(x1,y1,x2,y2)) return true;
        }
        return false;
    }

    private void colocarImagenAlfombra(int col, int row, boolean horizontal, int player) {
        colocarImagenAlfombraSpan(col, row, horizontal, player, true);
    }

    private void colocarImagenAlfombraSpan(int col, int row, boolean horizontal, int player, boolean dosCeldas) {
        if (carpetImages[player - 1] == null) return;
        ImageView iv = new ImageView(carpetImages[player - 1]);
        iv.setMouseTransparent(true); iv.setPreserveRatio(false);
        if (!dosCeldas) {
            iv.setFitWidth(CELL); iv.setFitHeight(CELL);
            GridPane.setColumnSpan(iv, 1); GridPane.setRowSpan(iv, 1);
        } else if (horizontal) {
            iv.setFitWidth(CELL * 2); iv.setFitHeight(CELL);
            GridPane.setColumnSpan(iv, 2); GridPane.setRowSpan(iv, 1);
        } else {
            iv.setFitWidth(CELL); iv.setFitHeight(CELL * 2);
            GridPane.setColumnSpan(iv, 1); GridPane.setRowSpan(iv, 2);
        }
        boardGrid.add(iv, col, row);
        assamView.toFront();
    }

    // ── Movimiento Assam ──────────────────────────────────────────────────────

    private int borderIndex(int x, int y) {
        if (y==0&&x<6) return x; if (x==6&&y<6) return 6+y;
        if (y==6&&x>0) return 12+(6-x); if (x==0&&y>0) return 18+(6-y); return -1;
    }
    private int[] borderPos(int idx) {
        idx=((idx%24)+24)%24;
        if(idx<6) return new int[]{idx,0}; if(idx<12) return new int[]{6,idx-6};
        if(idx<18) return new int[]{6-(idx-12),6}; return new int[]{0,6-(idx-18)};
    }
    private int borderDir(int idx) {
        idx=((idx%24)+24)%24;
        if(idx<6) return 1; if(idx<12) return 2; if(idx<18) return 3; return 0;
    }
    private void moverAssam(int pasos) {
        for (int p=0; p<pasos; p++) {
            int nx=assamX, ny=assamY;
            switch(assamDir){case 0:ny--;break;case 1:nx++;break;case 2:ny++;break;case 3:nx--;break;}
            if(nx<0||nx>6||ny<0||ny>6){
                int bi=borderIndex(assamX,assamY);
                if(bi==-1){nx=Math.max(0,Math.min(6,nx));ny=Math.max(0,Math.min(6,ny));}
                else{bi++;int[]pos=borderPos(bi);nx=pos[0];ny=pos[1];assamDir=borderDir(bi);}
            }
            assamX=nx; assamY=ny;
        }
        int bi=borderIndex(assamX,assamY);
        if(bi!=-1) assamDir=borderDir(bi);
        assamView.setRotate(assamDir*90);
    }

    // ── Fin del juego ─────────────────────────────────────────────────────────

    private boolean juegoTerminado() {
        for(int i=0;i<numPlayers;i++) if(rugs[i]>0) return false; return true;
    }

    private void mostrarFinDeJuego() {
        if(pollingTimeline!=null) pollingTimeline.stop();
        if(chatTimeline!=null)    chatTimeline.stop();
        int[] enTablero=new int[numPlayers];
        for(int r=0;r<7;r++) for(int c=0;c<7;c++)
            if(tileOwner[c][r]>0) enTablero[tileOwner[c][r]-1]++;
        int win=0;
        for(int i=1;i<numPlayers;i++)
            if(money[i]>money[win]||(money[i]==money[win]&&enTablero[i]>enTablero[win])) win=i;
        if(modoMultijugador) PartidaRepository.registrarVictoria(usuarioActual);
        winnerLabel.setText("GANA: "+playerNames[win]+"!");
        winnerLabel.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:"+playerColors[win]+";");
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<numPlayers;i++)
            sb.append(playerNames[i]).append(": ").append(money[i])
              .append(" Dh | Alfombras en tablero: ").append(enTablero[i]).append("\n");
        finalScores.setText(sb.toString());
        gameScreen.setVisible(false); endScreen.setVisible(true);
    }

    @FXML private void restartGame() {
        if(pollingTimeline!=null) pollingTimeline.stop();
        if(chatTimeline!=null)    chatTimeline.stop();
        endScreen.setVisible(false); gameScreen.setVisible(false);
        boardGrid.getChildren().clear(); startScreen.setVisible(true);
    }

    private int contarContiguas(int x,int y,int owner){return dfs(x,y,owner,new boolean[7][7]);}
    private int dfs(int x,int y,int owner,boolean[][]vis){
        if(x<0||x>6||y<0||y>6||vis[x][y]||tileOwner[x][y]!=owner) return 0;
        vis[x][y]=true;
        return 1+dfs(x+1,y,owner,vis)+dfs(x-1,y,owner,vis)+dfs(x,y+1,owner,vis)+dfs(x,y-1,owner,vis);
    }

    private void actualizarUI() {
        Label[] rl={rugsJ1,rugsJ2,rugsJ3,rugsJ4};
        Label[] ml={moneyJ1,moneyJ2,moneyJ3,moneyJ4};
        VBox[]  pl={panelJ1,panelJ2,panelJ3,panelJ4};
        for(int i=0;i<numPlayers;i++){
            if(rl[i]!=null) rl[i].setText(String.valueOf(rugs[i]));
            if(ml[i]!=null) ml[i].setText(money[i]+" Dh");
            if(pl[i]!=null) pl[i].setStyle(i==currentPlayerIdx
                ? "-fx-border-color:"+playerColors[i]+";-fx-border-width:3px;-fx-border-radius:10px;"
                : "-fx-border-color:"+hexToRgba(playerColors[i],0.35)+";-fx-border-width:1.5px;-fx-border-radius:10px;");
        }
        String miColor = modoMultijugador ? playerColors[miIndice] : playerColors[currentPlayerIdx];
        turnLabel.setText(esMiTurno() ? "TU TURNO" : "TURNO: "+playerNames[currentPlayerIdx]);
        turnLabel.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:"+miColor+";");
    }

    private String hexToRgba(String hex, double alpha) {
        int r=Integer.parseInt(hex.substring(1,3),16);
        int g=Integer.parseInt(hex.substring(3,5),16);
        int b=Integer.parseInt(hex.substring(5,7),16);
        return "rgba("+r+","+g+","+b+","+alpha+")";
    }
}