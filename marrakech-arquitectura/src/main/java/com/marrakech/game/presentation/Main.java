package com.marrakech.game.presentation;

import com.marrakech.game.infrastructure.database.DatabaseConnection;
import com.marrakech.game.repository.*;
import com.marrakech.game.service.*;
import com.marrakech.game.infrastructure.audio.MusicaManager;
import com.marrakech.game.presentation.controller.AuthController;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Marrakech Game");
        stage.setMaximized(true);

        // ── 1. Repositorios ───────────────────────────────────────────────────
        EstadisticasRepositorio  estadisticasRepo = new EstadisticasRepositorio();
        IJugadorRepositorio      jugadorRepo      = new JugadorRepositorio(estadisticasRepo);
        IPartidaRepositorio      partidaRepo      = new PartidaRepositorio();
        IChatRepositorio         chatRepo         = new ChatRepositorio();
        IEstadoJuegoRepositorio  estadoRepo       = new EstadoJuegoRepositorio();

        // ── 2. Servicios ──────────────────────────────────────────────────────
        MusicaServicio  musicaSvc  = new MusicaServicio();
        AuthServicio    authSvc    = new AuthServicio(jugadorRepo, partidaRepo);
        PartidaServicio partidaSvc = new PartidaServicio(partidaRepo);
        ChatServicio    chatSvc    = new ChatServicio(chatRepo);

        // ── 3. Controlador principal ───────────────────────────────────────────
        AuthController auth = new AuthController(stage, 1100, 700,
                                                 authSvc, partidaSvc, chatSvc, musicaSvc,
                                                 estadoRepo);

        musicaSvc.reproducir(MusicaServicio.Track.MENU);
        auth.mostrarWelcome();
        stage.show();
    }

    @Override
    public void stop() {
        MusicaManager.getInstance().detener();
    }

    public static void main(String[] args) {
        DatabaseConnection.initDatabase();
        launch();
    }
}
