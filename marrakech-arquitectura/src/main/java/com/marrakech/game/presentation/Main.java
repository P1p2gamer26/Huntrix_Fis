package com.marrakech.game.presentation;

import com.marrakech.game.infrastructure.database.DatabaseConnection;
import com.marrakech.game.repositorio.*;
import com.marrakech.game.servicios.*;
import com.marrakech.game.presentation.controller.AuthController;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación.
 * Aquí se construye el árbol completo de dependencias:
 *   repositorios → servicios → controladores.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Marrakech Game");
        stage.setMaximized(true);

        // ── 1. Repositorios ───────────────────────────────────────────────────
        EstadisticasRepositorio estadisticasRepo = new EstadisticasRepositorio();
        IJugadorRepositorio     jugadorRepo      = new JugadorRepositorio(estadisticasRepo);
        IPartidaRepositorio     partidaRepo      = new PartidaRepositorio();
        IChatRepositorio        chatRepo         = new ChatRepositorio();

        // ── 2. Servicios ──────────────────────────────────────────────────────
        AuthServicio    authSvc    = new AuthServicio(jugadorRepo, partidaRepo);
        PartidaServicio partidaSvc = new PartidaServicio(partidaRepo);
        ChatServicio    chatSvc    = new ChatServicio(chatRepo);

        // ── 3. Controlador principal (recibe servicios por constructor) ────────
        AuthController auth = new AuthController(stage, 1100, 700,
                                                 authSvc, partidaSvc, chatSvc);

        MusicaManager.getInstance().reproducir(MusicaManager.Track.MENU);
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
