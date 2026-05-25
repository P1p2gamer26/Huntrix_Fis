package com.marrakech.game.presentation;

import java.sql.Connection;
import java.sql.Statement;

import com.marrakech.game.infrastructure.audio.MusicaManager;
import com.marrakech.game.infrastructure.database.DatabaseConnection;
import com.marrakech.game.presentation.controller.AuthController;
import com.marrakech.game.repository.ChatRepositorio;
import com.marrakech.game.repository.EstadisticasRepositorio;
import com.marrakech.game.repository.EstadoJuegoRepositorio;
import com.marrakech.game.repository.IChatRepositorio;
import com.marrakech.game.repository.IEstadoJuegoRepositorio;
import com.marrakech.game.repository.IJugadorRepositorio;
import com.marrakech.game.repository.IPartidaRepositorio;
import com.marrakech.game.repository.JugadorRepositorio;
import com.marrakech.game.repository.PartidaRepositorio;
import com.marrakech.game.service.AuthServicio;
import com.marrakech.game.service.ChatServicio;
import com.marrakech.game.service.MusicaServicio;
import com.marrakech.game.service.PartidaServicio;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Marrakech Game");
        stage.setMaximized(true);

        EstadisticasRepositorio  estadisticasRepo = new EstadisticasRepositorio();
        IJugadorRepositorio      jugadorRepo      = new JugadorRepositorio(estadisticasRepo);
        IPartidaRepositorio      partidaRepo      = new PartidaRepositorio();
        IChatRepositorio         chatRepo         = new ChatRepositorio();
        IEstadoJuegoRepositorio  estadoRepo       = new EstadoJuegoRepositorio();

        MusicaServicio  musicaSvc  = new MusicaServicio();
        AuthServicio    authSvc    = new AuthServicio(jugadorRepo, partidaRepo);
        PartidaServicio partidaSvc = new PartidaServicio(partidaRepo);
        ChatServicio    chatSvc    = new ChatServicio(chatRepo);

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
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS chat_mensajes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "partida_id VARCHAR(20) NOT NULL, " +
                "usuario VARCHAR(60) NOT NULL, " +
                "texto VARCHAR(500) NOT NULL, " +
                "hora VARCHAR(8) NOT NULL, " +
                "ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        
        launch();
    }
}