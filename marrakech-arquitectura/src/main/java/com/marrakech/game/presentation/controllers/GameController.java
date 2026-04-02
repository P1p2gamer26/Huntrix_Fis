package com.marrakech.game.presentation.controllers;

import com.marrakech.game.infrastructure.PartidaRepository;
import com.marrakech.game.infrastructure.database.DatabaseConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.sql.*;
import java.util.Random;

public class GameController {

    @FXML private VBox startScreen;
    @FXML private HBox gameScreen;
    @FXML private VBox endScreen;
    @FXML private GridPane boardGrid;
    @FXML private Label turnLabel;
    @FXML private Label statusLabel;
    @FXML private Button rollDiceBtn;
    @FXML private VBox panelJ1, panelJ2, panelJ3, panelJ4;
    @FXML private Label rugsJ1, rugsJ2, rugsJ3, rugsJ4;
    @FXML private Label moneyJ1, moneyJ2, moneyJ3, moneyJ4;
    @FXML private Label winnerLabel;
    @FXML private Label finalScores;

    private ImageView assamView;
    private int assamX = 3, assamY = 3, assamDir = 0;

    private enum Phase { MOVE, CARPET_1, CARPET_2 }
    private Phase currentPhase = Phase.MOVE;

    private int numPlayers = 2;
    private int currentPlayerIdx = 0;
    private int[] money, rugs;
    private Image[] carpetImages;
    private int[][] tileOwner = new int[7][7];
    private StackPane[][] tiles = new StackPane[7][7];
    private int firstCarpetX = -1, firstCarpetY = -1;

    private static final int CELL = 61;
    private String[] playerColors = {"#e74c3c","#3498db","#2ecc71","#f39c12"};
    private String[] playerNames  = {"J1 (ROJO)","J2 (AZUL)","J3 (VERDE)","J4 (AMARILLO)"};

    // Multijugador
    private String partidaId;
    private String miUsuario;
    private int miIndice = 0; // qué jugador soy yo (0-based)
    private int ultimoTurnoVisto = -1;
    private Timeline pollingTimeline;
    private boolean modoMultijugador = false;

    public void initialize() {}

    // Llamado desde AuthController — modo multijugador
    public void iniciarConJugadores(int n, String partidaId, String miUsuario, int miIndice) {
        this.partidaId       = partidaId;
        this.miUsuario       = miUsuario;
        this.miIndice        = miIndice;
        this.modoMultijugador = true;
        startGame(n);
        guardarEstado(); // turno 0 inicial
        iniciarPolling();
    }

    // Llamado sin multijugador (fallback local)
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

        String[] imgPaths = {
            "/images/alfombra tablero roja.png", "/images/alfombra tablero azul.png",
            "/images/alfombra tablero morada.png", "/images/alfombra tablero amarillo.png"
        };
        carpetImages = new Image[n];
        for (int i = 0; i < n; i++) {
            try { carpetImages[i] = new Image(getClass().getResourceAsStream(imgPaths[i])); }
            catch (Exception e) { System.out.println("Sin imagen J"+(i+1)); }
        }

        panelJ3.setVisible(n >= 3); panelJ3.setManaged(n >= 3);
        panelJ4.setVisible(n >= 4); panelJ4.setManaged(n >= 4);

        boardGrid.getChildren().clear();
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) {
                tileOwner[col][row] = 0;
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

        currentPlayerIdx = 0;
        currentPhase = Phase.MOVE;
        startScreen.setVisible(false);
        endScreen.setVisible(false);
        gameScreen.setVisible(true);
        actualizarUI();
        actualizarControles();
        statusLabel.setText("Rota a Assam y lanza el dado.");
    }

    // ── Polling ──────────────────────────────────────────────────────────────

    private void iniciarPolling() {
        pollingTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            String estadoJson = cargarUltimoEstadoDB();
            if (estadoJson == null) return;
            EstadoDB est = parsearEstado(estadoJson);
            if (est == null || est.turno == ultimoTurnoVisto) return;
            // Solo aplicar si NO es mi turno (el mío ya lo apliqué localmente)
            if (est.turno != ultimoTurnoVisto && currentPlayerIdx != miIndice) {
                Platform.runLater(() -> aplicarEstado(est));
            } else if (est.turno != ultimoTurnoVisto) {
                ultimoTurnoVisto = est.turno;
            }
        }));
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
    }

    // ── Serialización del estado ─────────────────────────────────────────────

    private void guardarEstado() {
        if (!modoMultijugador || partidaId == null) return;
        String json = serializarEstado();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO EstadoPartida (turno_numero, assam_fila, assam_col, assam_direccion, tablero_estado, fecha_guardado, id_partida) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, (SELECT id_partida FROM Partida WHERE id_sala = " +
                "(SELECT id_sala FROM partidas WHERE id = ?) LIMIT 1))")) {
            ps.setInt(1, currentPlayerIdx);
            ps.setInt(2, assamY); ps.setInt(3, assamX);
            ps.setString(4, String.valueOf(assamDir));
            ps.setString(5, json);
            ps.setString(6, partidaId);
            ps.executeUpdate();
            ultimoTurnoVisto = currentPlayerIdx;
        } catch (Exception e) {
            // Si no hay partida formal en DB todavía, guardar en tabla auxiliar
            guardarEstadoAuxiliar(json);
        }
    }

    private void guardarEstadoAuxiliar(String json) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS estado_juego (" +
                "partida_id VARCHAR(20), turno INT, assam_x INT, assam_y INT, " +
                "assam_dir INT, tablero TEXT, ts TIMESTAMP, " +
                "PRIMARY KEY (partida_id, turno))");
            try (PreparedStatement ps = conn.prepareStatement(
                "MERGE INTO estado_juego (partida_id, turno, assam_x, assam_y, assam_dir, tablero, ts) " +
                "KEY(partida_id, turno) VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)")) {
                ps.setString(1, partidaId);
                ps.setInt(2, currentPlayerIdx);
                ps.setInt(3, assamX); ps.setInt(4, assamY);
                ps.setInt(5, assamDir); ps.setString(6, json);
                ps.executeUpdate();
                ultimoTurnoVisto = currentPlayerIdx;
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private String cargarUltimoEstadoDB() {
        if (!modoMultijugador || partidaId == null) return null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT turno, assam_x, assam_y, assam_dir, tablero FROM estado_juego " +
                "WHERE partida_id = ? ORDER BY ts DESC LIMIT 1")) {
            ps.setString(1, partidaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("turno") + "|" + rs.getInt("assam_x") + "|" +
                       rs.getInt("assam_y") + "|" + rs.getInt("assam_dir") + "|" +
                       rs.getString("tablero");
            }
        } catch (Exception e) { /* tabla aún no existe */ }
        return null;
    }

    private String serializarEstado() {
        // formato: money0,money1,...;rugs0,rugs1,...;tileOwner(fila por fila separada por /)
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
        return sb.toString();
    }

    private static class EstadoDB {
        int turno, ax, ay, adir;
        String tableroJson;
    }

    private EstadoDB parsearEstado(String raw) {
        try {
            String[] partes = raw.split("\\|", 5);
            EstadoDB e = new EstadoDB();
            e.turno = Integer.parseInt(partes[0]);
            e.ax    = Integer.parseInt(partes[1]);
            e.ay    = Integer.parseInt(partes[2]);
            e.adir  = Integer.parseInt(partes[3]);
            e.tableroJson = partes[4];
            return e;
        } catch (Exception ex) { return null; }
    }

    private void aplicarEstado(EstadoDB est) {
        // Assam
        assamX = est.ax; assamY = est.ay; assamDir = est.adir;
        GridPane.setColumnIndex(assamView, assamX);
        GridPane.setRowIndex(assamView, assamY);
        assamView.setRotate(assamDir * 90);
        assamView.toFront();

        // Tablero desde JSON
        String[] secciones = est.tableroJson.split(";");
        if (secciones.length >= 4) {
            String[] ms = secciones[0].split(",");
            String[] rs = secciones[1].split(",");
            for (int i = 0; i < numPlayers && i < ms.length; i++) {
                money[i] = Integer.parseInt(ms[i]);
                rugs[i]  = Integer.parseInt(rs[i]);
            }
            String[] filas = secciones[2].split("/");
            // Limpiar alfombras visuales (excepto tiles base y assam)
            boardGrid.getChildren().removeIf(n ->
                n instanceof ImageView && n != assamView);
            for (int row = 0; row < 7 && row < filas.length; row++) {
                String[] celdas = filas[row].split(",");
                for (int col = 0; col < 7 && col < celdas.length; col++) {
                    int prev = tileOwner[col][row];
                    tileOwner[col][row] = Integer.parseInt(celdas[col]);
                    // Si cambió, redibujar alfombra
                    if (tileOwner[col][row] != prev && tileOwner[col][row] > 0) {
                        // La redibujamos como 1x1 (la sincronización completa de spans
                        // requeriría más info; esto muestra el color correctamente)
                        redibujarCelda(col, row, tileOwner[col][row]);
                    }
                }
            }
            currentPlayerIdx = Integer.parseInt(secciones[3]);
            ultimoTurnoVisto = est.turno;
        }
        actualizarUI();
        actualizarControles();
        statusLabel.setText(esMiTurno()
            ? "Tu turno. Rota a Assam y lanza el dado."
            : "Turno de " + playerNames[currentPlayerIdx] + ". Esperando...");
    }

    private void redibujarCelda(int col, int row, int player) {
        ImageView iv = new ImageView(carpetImages[player - 1]);
        iv.setFitWidth(CELL); iv.setFitHeight(CELL);
        iv.setMouseTransparent(true);
        iv.setPreserveRatio(false);
        boardGrid.add(iv, col, row);
        assamView.toFront();
    }

    // ── Lógica del juego ─────────────────────────────────────────────────────

    private boolean esMiTurno() {
        return !modoMultijugador || currentPlayerIdx == miIndice;
    }

    private void actualizarControles() {
        boolean activo = esMiTurno() && currentPhase == Phase.MOVE;
        rollDiceBtn.setDisable(!activo);
    }

    @FXML protected void rotateLeft() {
        if (!esMiTurno() || currentPhase != Phase.MOVE) return;
        assamDir = (assamDir + 3) % 4;
        assamView.setRotate(assamDir * 90);
    }

    @FXML protected void rotateRight() {
        if (!esMiTurno() || currentPhase != Phase.MOVE) return;
        assamDir = (assamDir + 1) % 4;
        assamView.setRotate(assamDir * 90);
    }

    @FXML protected void onRollDiceClick() {
        if (!esMiTurno() || currentPhase != Phase.MOVE) return;
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
            statusLabel.setText("Dado: " + pasos + " — Pagas " + pago + " Dh a J" + dueno + ". Coloca alfombra.");
        } else {
            statusLabel.setText("Dado: " + pasos + " — Click en casilla adyacente a Assam.");
        }
        actualizarUI();
        if (rugs[currentPlayerIdx] > 0) {
            currentPhase = Phase.CARPET_1;
            rollDiceBtn.setDisable(true);
        } else {
            pasarTurno();
        }
    }

    private void handleTileClick(int x, int y) {
        if (!esMiTurno()) return;
        if (currentPhase == Phase.CARPET_1) {
            boolean adj = Math.abs(assamX-x) + Math.abs(assamY-y) == 1;
            if (adj && tiene2daOpcionValida(x, y)) {
                firstCarpetX = x; firstCarpetY = y;
                tiles[x][y].setStyle("-fx-background-color: rgba(255,255,255,0.25);");
                currentPhase = Phase.CARPET_2;
                statusLabel.setText("1ra mitad lista. Click en casilla contigua para 2da mitad.");
            } else if (adj) {
                statusLabel.setText("Sin espacio para la 2da mitad. Elige otra.");
            }
        } else if (currentPhase == Phase.CARPET_2) {
            boolean adj    = Math.abs(firstCarpetX-x) + Math.abs(firstCarpetY-y) == 1;
            boolean noAssam = !(x==assamX && y==assamY);
            boolean dentro  = esCarpetValida(firstCarpetX, firstCarpetY, x, y);
            boolean horiz   = (y == firstCarpetY);
            if (adj && noAssam && dentro) {
                tiles[firstCarpetX][firstCarpetY].setStyle("");
                int player = currentPlayerIdx + 1;
                tileOwner[firstCarpetX][firstCarpetY] = player;
                tileOwner[x][y] = player;
                colocarImagenAlfombra(Math.min(firstCarpetX,x), Math.min(firstCarpetY,y), horiz, player);
                rugs[currentPlayerIdx]--;
                actualizarUI();
                if (juegoTerminado()) mostrarFinDeJuego();
                else pasarTurno();
            } else if (adj && !dentro) {
                statusLabel.setText("Fuera del tablero. Elige otra dirección.");
            }
        }
    }

    private void pasarTurno() {
        firstCarpetX = -1; firstCarpetY = -1;
        currentPlayerIdx = (currentPlayerIdx + 1) % numPlayers;
        currentPhase = Phase.MOVE;
        actualizarUI();
        actualizarControles();
        guardarEstado(); // ← sincronizar con DB
        statusLabel.setText(esMiTurno()
            ? "Tu turno. Rota a Assam y lanza el dado."
            : "Turno de " + playerNames[currentPlayerIdx] + ". Esperando...");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private boolean esCarpetValida(int x1, int y1, int x2, int y2) {
        return x1>=0&&x1<=6&&y1>=0&&y1<=6&&x2>=0&&x2<=6&&y2>=0&&y2<=6;
    }

    private boolean tiene2daOpcionValida(int x1, int y1) {
        for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
            int x2=x1+d[0], y2=y1+d[1];
            if (x2==assamX && y2==assamY) continue;
            if (esCarpetValida(x1,y1,x2,y2)) return true;
        }
        return false;
    }

    private void colocarImagenAlfombra(int col, int row, boolean horizontal, int player) {
        if (carpetImages[player-1] == null) return;
        ImageView iv = new ImageView(carpetImages[player-1]);
        iv.setMouseTransparent(true);
        iv.setPreserveRatio(false);
        if (horizontal) {
            iv.setFitWidth(CELL*2); iv.setFitHeight(CELL);
            GridPane.setColumnSpan(iv, 2); GridPane.setRowSpan(iv, 1);
            iv.setRotate(90);
        } else {
            iv.setFitWidth(CELL); iv.setFitHeight(CELL*2);
            GridPane.setColumnSpan(iv, 1); GridPane.setRowSpan(iv, 2);
        }
        boardGrid.add(iv, col, row);
        assamView.toFront();
    }

    private int borderIndex(int x, int y) {
        if (y==0&&x<6) return x;
        if (x==6&&y<6) return 6+y;
        if (y==6&&x>0) return 12+(6-x);
        if (x==0&&y>0) return 18+(6-y);
        return -1;
    }
    private int[] borderPos(int idx) {
        idx=((idx%24)+24)%24;
        if(idx<6)  return new int[]{idx,0};
        if(idx<12) return new int[]{6,idx-6};
        if(idx<18) return new int[]{6-(idx-12),6};
        return new int[]{0,6-(idx-18)};
    }
    private int borderDir(int idx) {
        idx=((idx%24)+24)%24;
        if(idx<6)  return 1;
        if(idx<12) return 2;
        if(idx<18) return 3;
        return 0;
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
    private boolean juegoTerminado() {
        for(int i=0;i<numPlayers;i++) if(rugs[i]>0) return false;
        return true;
    }
    private void mostrarFinDeJuego() {
        if (pollingTimeline != null) pollingTimeline.stop();
        int[] enTablero=new int[numPlayers];
        for(int r=0;r<7;r++) for(int c=0;c<7;c++) if(tileOwner[c][r]>0) enTablero[tileOwner[c][r]-1]++;
        int win=0;
        for(int i=1;i<numPlayers;i++)
            if(money[i]>money[win]||(money[i]==money[win]&&enTablero[i]>enTablero[win])) win=i;
        if (modoMultijugador) PartidaRepository.registrarVictoria(miUsuario);
        winnerLabel.setText("GANA: "+playerNames[win]+"!");
        winnerLabel.setStyle("-fx-font-size:36px;-fx-font-weight:bold;-fx-text-fill:"+playerColors[win]+";");
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<numPlayers;i++)
            sb.append(playerNames[i]).append(": ").append(money[i]).append(" Dh | Tiles: ").append(enTablero[i]).append("\n");
        finalScores.setText(sb.toString());
        gameScreen.setVisible(false); endScreen.setVisible(true);
    }
    @FXML private void restartGame() {
        if(pollingTimeline!=null) pollingTimeline.stop();
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
        Label[]rl={rugsJ1,rugsJ2,rugsJ3,rugsJ4};
        Label[]ml={moneyJ1,moneyJ2,moneyJ3,moneyJ4};
        VBox[]pl={panelJ1,panelJ2,panelJ3,panelJ4};
        for(int i=0;i<numPlayers;i++){
            if(rl[i]!=null) rl[i].setText(String.valueOf(rugs[i]));
            if(ml[i]!=null) ml[i].setText(money[i]+" Dh");
            if(pl[i]!=null) pl[i].setStyle(i==currentPlayerIdx
                ?"-fx-border-color:"+playerColors[i]+";-fx-border-width:3px;-fx-border-radius:10px;":"");
        }
        turnLabel.setText(esMiTurno()?"TU TURNO":"TURNO: "+playerNames[currentPlayerIdx]);
    }
}
