package com.marrakech.game.service;

import com.marrakech.game.repository.IEstadoJuegoRepositorio;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class EstadoJuegoServicio {

    public static class EstadoDB {
        public int    turno;
        public int    ax, ay, adir;
        public String tableroJson;
    }

    private final IEstadoJuegoRepositorio estadoRepo;
    private final String partidaId;

    // -1 garantiza que cualquier turno en DB sea detectado como nuevo
    private volatile int ultimoTurnoVisto = -1;
    private volatile int estadoVersion    = -1;
    private Timeline pollingTimeline;

    public EstadoJuegoServicio(IEstadoJuegoRepositorio estadoRepo, String partidaId) {
        this.estadoRepo = estadoRepo;
        this.partidaId  = partidaId;
    }

    public void guardarEstado(int assamX, int assamY, int assamDir, String tableroJson) {
        if (partidaId == null) return;
        final int ax = assamX, ay = assamY, adir = assamDir;
        new Thread(() -> {
            String raw = estadoRepo.cargarUltimo(partidaId);
            int ultimoTurno = 0;
            if (raw != null) {
                try { ultimoTurno = Integer.parseInt(raw.split("\\|")[0]); } catch (Exception ignored) {}
            }
            int turno = ultimoTurno + 1;
            estadoRepo.guardar(partidaId, turno, ax, ay, adir, tableroJson);
            // Actualizar en el mismo hilo para evitar race condition
            ultimoTurnoVisto = turno;
            estadoVersion    = turno;
        }, "db-writer").start();
    }

    public void guardarEstadoSincrono(int assamX, int assamY, int assamDir, String tableroJson) {
        if (partidaId == null) return;
        final int ax = assamX, ay = assamY, adir = assamDir;
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            // Limpiar estado anterior y empezar siempre desde turno 1
            estadoRepo.limpiar(partidaId);
            estadoRepo.guardar(partidaId, 1, ax, ay, adir, tableroJson);
            ultimoTurnoVisto = 1;
            estadoVersion    = 1;
            latch.countDown();
        }, "db-init-writer").start();
        try { latch.await(3, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
    }

    public String cargarUltimoEstado() {
        if (partidaId == null) return null;
        return estadoRepo.cargarUltimo(partidaId);
    }

    /**
     * Notifica que el turno actual terminó completamente.
     * Guarda el estado y lo marca como "listo" para que los otros jugadores lo lean.
     */
    public void notificarTurnoListo(int assamX, int assamY, int assamDir, String tableroJson) {
        if (partidaId == null) return;
        final int ax = assamX, ay = assamY, adir = assamDir;
        new Thread(() -> {
            String raw = estadoRepo.cargarUltimo(partidaId);
            int ultimoTurno = 0;
            if (raw != null) {
                try { ultimoTurno = Integer.parseInt(raw.split("\\|")[0]); } catch (Exception ignored) {}
            }
            int turno = ultimoTurno + 1;
            estadoRepo.guardar(partidaId, turno, ax, ay, adir, tableroJson);
            // Ahora marcamos como listo — J2 solo leerá a partir de aquí
            estadoRepo.marcarListo(partidaId);
            ultimoTurnoVisto = turno;
            estadoVersion    = turno;
        }, "db-writer-listo").start();
    }

    /** Desmarca el listo al empezar el turno propio (para no re-leer el estado anterior). */
    public void desmarcarListo() {
        if (partidaId == null) return;
        new Thread(() -> estadoRepo.desmarcarListo(partidaId), "db-desmarcar").start();
    }

    public void iniciarPolling(java.util.function.Consumer<String> onEstadoCambiado) {
        pollingTimeline = new Timeline(new KeyFrame(Duration.millis(500), e ->
            new Thread(() -> {
                // Solo disparar cuando el otro jugador marcó su turno como listo
                if (!estadoRepo.estaListo(partidaId)) return;
                String raw = cargarUltimoEstado();
                if (raw == null) return;
                EstadoDB est = parsearEstado(raw);
                if (est != null && est.turno > ultimoTurnoVisto) {
                    ultimoTurnoVisto = est.turno;
                    estadoVersion    = est.turno;
                    final String rawFinal = raw;
                    Platform.runLater(() -> onEstadoCambiado.accept(rawFinal));
                }
            }, "db-poller").start()
        ));
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
    }

    public void detenerPolling() {
        if (pollingTimeline != null) { pollingTimeline.stop(); pollingTimeline = null; }
    }

    public static String serializarEstado(int numPlayers, int[] money, int[] rugs,
                                          int[][] tileOwner, int currentPlayerIdx,
                                          int currentPhase, int firstCarpetX, int firstCarpetY,
                                          int[][] carpetOrientation,
                                          int[][] posicionReliquia, boolean[][] inventarioReliquias) {
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
        sb.append(";");
        for (int i = 0; i < posicionReliquia.length; i++)
            sb.append(posicionReliquia[i][0]).append(",").append(posicionReliquia[i][1])
              .append(i < posicionReliquia.length - 1 ? "/" : "");
        sb.append(";");
        for (int j = 0; j < numPlayers; j++) {
            for (int r = 0; r < inventarioReliquias[j].length; r++)
                sb.append(inventarioReliquias[j][r] ? "1" : "0")
                  .append(r < inventarioReliquias[j].length - 1 ? "," : "");
            if (j < numPlayers - 1) sb.append("/");
        }
        return sb.toString();
    }

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

    public void setUltimoTurnoVisto(int turno)  { this.ultimoTurnoVisto = turno; }
    public int  getUltimoTurnoVisto()            { return ultimoTurnoVisto; }
    public int  getEstadoVersion()               { return estadoVersion; }
    public void setEstadoVersion(int version)    { this.estadoVersion = version; }
}
