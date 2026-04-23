package com.marrakech.game.presentation;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class MusicaManager {

    public enum Track { MENU, LOBBY, JUEGO }

    private static MusicaManager instancia;

    private MediaPlayer playerActual;
    private Track trackActual;
    private double volumen = 0.5;

    private MusicaManager() {}

    public static MusicaManager getInstance() {
        if (instancia == null) instancia = new MusicaManager();
        return instancia;
    }

    public void reproducir(Track track) {
        if (track == trackActual && playerActual != null
                && playerActual.getStatus() == MediaPlayer.Status.PLAYING) return;

        detener();
        trackActual = track;

        String url = obtenerUrl(track);
        if (url == null) return;

        try {
            Media media = new Media(url);
            playerActual = new MediaPlayer(media);
            playerActual.setVolume(volumen);
            playerActual.setCycleCount(MediaPlayer.INDEFINITE);
            playerActual.setOnError(() ->
                System.err.println("Error reproduciendo música: " + playerActual.getError()));
            playerActual.play();
        } catch (Exception e) {
            System.err.println("No se pudo cargar la música [" + track + "]: " + e.getMessage());
        }
    }

    private String obtenerUrl(Track track) {
        String recurso = switch (track) {
            case MENU  -> "/audio/musica_menu.mp3";   // esta linea cambiala para poner el archivo de la musica de pantalla principal hasta unirse partida
            case LOBBY -> "/audio/musica_lobby.mp3";  // esta linea cambiala para poner el archivo de la musica del lobby de espera
            case JUEGO -> "/audio/musica_juego.mp3";  // esta linea cambiala para poner el archivo de la musica durante la partida en el tablero
        };
        try {
            var resourceUrl = getClass().getResource(recurso);
            if (resourceUrl == null) {
                System.err.println("Archivo de música no encontrado: " + recurso);
                return null;
            }
            return resourceUrl.toExternalForm();
        } catch (Exception e) {
            return null;
        }
    }

    public void detener() {
        if (playerActual != null) {
            playerActual.stop();
            playerActual.dispose();
            playerActual = null;
        }
        trackActual = null;
    }

    public void pausar() {
        if (playerActual != null) playerActual.pause();
    }

    public void reanudar() {
        if (playerActual != null) playerActual.play();
    }

    public void setVolumen(double v) {
        volumen = Math.max(0.0, Math.min(1.0, v));
        if (playerActual != null) playerActual.setVolume(volumen);
    }

    public double getVolumen() { return volumen; }
    public Track getTrackActual() { return trackActual; }
}