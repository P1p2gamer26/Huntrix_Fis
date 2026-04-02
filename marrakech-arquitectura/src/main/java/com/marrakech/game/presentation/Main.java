package com.marrakech.game.presentation;

import org.h2.tools.Server;

import com.marrakech.game.presentation.controllers.AuthController;

import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;
import com.marrakech.game.infrastructure.database.DatabaseConnection;


public class Main extends Application {

    private static Server h2Server;

    @Override
    public void start(Stage stage) {
        iniciarServidorH2();

        stage.setTitle("Marrakech Game");
        stage.setMaximized(true);

        AuthController auth = new AuthController(stage, 1100, 700);
        auth.mostrarWelcome();

        stage.show();
    }
    public static void main(String[] args){
        DatabaseConnection.initDatabase();
        launch();
    }
}