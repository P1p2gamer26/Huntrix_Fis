package com.marrakech.game.servicios;

import com.marrakech.game.infrastructure.database.DatabaseConnection;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.function.Consumer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Gestiona la persistencia y el polling del estado de juego para el modo multijugador.
 * Cada versión de estado representa un turno; el polling detecta turnos nuevos.
 */
public class EstadoJuegoServicio {

    /** Estado de un turno recuperado de la base de datos. */
    public static class EstadoDB {
        public int    turno;
        public int    ax, ay, adir;
        public String tableroJson;
    }

    private final String partidaId;

    private int     estadoVersion;
    private int     ultimoTurnoVisto;
    private boolean tablaCreada;
    private Timeline pollingTimeline;

    public EstadoJuegoServicio(String partidaId) {
        this.partidaId = partidaId;
    }

    // ── Persistencia ──────────────────────────────────────────────────────────

    /** Guarda el estado en un hilo de fondo (no bloquea la UI). */
    public void guardarEstado(int assamX, int assamY, int assamDir, String tableroJson) {
        if (partidaId == null) return;
        estadoVersion++;
        final int turno = estadoVersion;
        final int ax = assamX, ay = assamY, adir = assamDir;
        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                asegurarTabla(conn);
                escribirEstado(conn, turno, ax, ay, adir, tableroJson);
                Platform.runLater(() -> ultimoTurnoVisto = turno);
            } catch (Exception ex) {
                System.err.println("Error guardando estado: " + ex.getMessage());
            }
        }, "db-writer").start();
    }

    /** Guarda el estado de forma síncrona (para el estado inicial antes de habilitar la UI). */
    public void guardarEstadoSincrono(int assamX, int assamY, int assamDir, String tableroJson) {
        if (partidaId == null) return;
        estadoVersion++;
        final int turno = estadoVersion;
        final int ax = assamX, ay = assamY, adir = assamDir;
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                asegurarTabla(conn);
                escribirEstado(conn, turno, ax, ay, adir, tableroJson);
                Platform.runLater(() -> ultimoTurnoVisto = turno);
            } catch (Exception ex) {
                System.err.println("Error guardando estado inicial: " + ex.getMessage());
            } finally { latch.countDown(); }
        }, "db-init-writer").start();
        try { latch.await(3, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
    }

    /** Recupera el estado más reciente de la BD como string crudo. */
    public String cargarUltimoEstado() {
        if (partidaId == null) return null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT turno_numero, assam_x, assam_y, assam_dir, tablero " +
                "FROM estado_juego WHERE partida_id = ? " +
                "ORDER BY turno_numero DESC LIMIT 1")) {
            ps.setString(1, partidaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("turno_numero") + "|" + rs.getInt("assam_x") + "|" +
                       rs.getInt("assam_y")      + "|" + rs.getInt("assam_dir") + "|" +
                       rs.getString("tablero");
        } catch (Exception e) { System.err.println("Error leyendo estado: " + e.getMessage()); }
        return null;
    }

    // ── Polling ───────────────────────────────────────────────────────────────

    /** Arranca un polling cada 1200 ms; llama onEstadoCambiado cuando hay un turno nuevo. */
    public void iniciarPolling(Runnable onEstadoCambiado) {
        pollingTimeline = new Timeline(new KeyFrame(Duration.millis(1200), e ->
            new Thread(() -> {
                String raw = cargarUltimoEstado();
                if (raw == null) return;
                EstadoDB est = parsearEstado(raw);
                if (est != null && est.turno > ultimoTurnoVisto) {
                    ultimoTurnoVisto = est.turno;
                    Platform.runLater(onEstadoCambiado);
                }
            }, "db-poller").start()
        ));
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
    }

    public void detenerPolling() {
        if (pollingTimeline != null) { pollingTimeline.stop(); pollingTimeline = null; }
    }

    // ── Serialización ─────────────────────────────────────────────────────────

    /**
     * Serializa el estado del tablero a un String compacto de texto plano.
     * Formato: dinero;alfombras;propietarios(fila/);currentIdx;fase;cx;cy;orientaciones(fila/)
     */
    public static String serializarEstado(int numPlayers, int[] money, int[] rugs,
                                          int[][] tileOwner, int currentPlayerIdx,
                                          int currentPhase, int firstCarpetX, int firstCarpetY,
                                          int[][] carpetOrientation) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numPlayers; i++)
            sb.append(money[i]).append(i < numPlayers - 1 ? "," : "");
        sb.append(";");
        for (int i = 0; i < numPlayers; i++)
            sb.append(rugs[i]).append(i < numPlayers - 1 ? "," : "");
        sb.append(";");
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) sb.append(tileOwner[col][row]).append(",");
            sb.append("/");
        }
        sb.append(";").append(currentPlayerIdx)
          .append(";").append(currentPhase)
          .append(";").append(firstCarpetX)
          .append(";").append(firstCarpetY)
          .append(";");
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 7; col++) sb.append(carpetOrientation[col][row]).append(",");
            sb.append("/");
        }
        return sb.toString();
    }

    /** Parsea el string crudo almacenado en la BD. Retorna null si el formato es inválido. */
    public static EstadoDB parsearEstado(String raw) {
        try {
            String[] p = raw.split("\\|", 5);
            EstadoDB e = new EstadoDB();
            e.turno       = Integer.parseInt(p[0]);
            e.ax          = Integer.parseInt(p[1]);
            e.ay          = Integer.parseInt(p[2]);
            e.adir        = Integer.parseInt(p[3]);
            e.tableroJson = p[4];
            return e;
        } catch (Exception ex) { return null; }
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private void asegurarTabla(Connection conn) throws Exception {
        if (!tablaCreada) {
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS estado_juego (" +
                "partida_id VARCHAR(20), turno_numero INT, " +
                "assam_x INT, assam_y INT, assam_dir INT, " +
                "tablero TEXT, ts TIMESTAMP, " +
                "PRIMARY KEY (partida_id, turno_numero))");
            tablaCreada = true;
        }
    }

    private void escribirEstado(Connection conn, int turno,
                                 int ax, int ay, int adir, String tableroJson) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(
            "MERGE INTO estado_juego " +
            "(partida_id, turno_numero, assam_x, assam_y, assam_dir, tablero, ts) " +
            "KEY(partida_id, turno_numero) VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)")) {
            ps.setString(1, partidaId); ps.setInt(2, turno);
            ps.setInt(3, ax);           ps.setInt(4, ay);
            ps.setInt(5, adir);         ps.setString(6, tableroJson);
            ps.executeUpdate();
        }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public void setUltimoTurnoVisto(int turno)  { this.ultimoTurnoVisto = turno; }
    public int  getUltimoTurnoVisto()            { return ultimoTurnoVisto; }
    public int  getEstadoVersion()               { return estadoVersion; }
    public void setEstadoVersion(int version)    { this.estadoVersion = version; }
}
