package com.marrakech.game.presentation;

import com.marrakech.game.infrastructure.database.DatabaseConnection;
import com.marrakech.game.presentation.controllers.AuthController;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Marrakech Game");
        stage.setMaximized(true);

        MusicaManager.getInstance().reproducir(MusicaManager.Track.MENU);

        AuthController auth = new AuthController(stage, 1100, 700);
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