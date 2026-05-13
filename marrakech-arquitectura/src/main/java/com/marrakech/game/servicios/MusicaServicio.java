package com.marrakech.game.servicios;

import com.marrakech.game.presentation.MusicaManager;

public class MusicaServicio {

    public enum Track { MENU, LOBBY, JUEGO }

    private final MusicaManager manager;

    public MusicaServicio() {
        this.manager = MusicaManager.getInstance();
    }

    public void reproducir(Track track) {
        MusicaManager.Track t = switch (track) {
            case MENU  -> MusicaManager.Track.MENU;
            case LOBBY -> MusicaManager.Track.LOBBY;
            case JUEGO -> MusicaManager.Track.JUEGO;
        };
        manager.reproducir(t);
    }

    public void detener() { manager.detener(); }

    public void setVolumen(double v) { manager.setVolumen(v); }

    public double getVolumen() { return manager.getVolumen(); }
}
