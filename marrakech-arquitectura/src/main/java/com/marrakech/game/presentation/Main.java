package com.marrakech.game.presentation;

import org.h2.tools.Server;

import com.marrakech.game.presentation.controllers.AuthController;

import javafx.application.Application;
import javafx.stage.Stage;

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

    private void iniciarServidorH2() {
        try {
            h2Server = Server.createTcpServer(
                "-tcpPort", "9092",
                "-tcpAllowOthers",
                "-ifNotExists"
            ).start();
            System.out.println("Servidor H2 iniciado en puerto 9092");
        } catch (Exception e) {
            System.out.println("Servidor H2 ya está corriendo (otra instancia lo inició)");
        }
    }

    @Override
    public void stop() {
        if (h2Server != null && h2Server.isRunning(true)) {
            h2Server.stop();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}