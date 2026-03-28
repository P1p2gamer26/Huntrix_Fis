package com.marrakech.game.presentation;

import com.marrakech.game.presentation.views.WelcomeView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Marrakech Game");

        // 👇 aquí cargas la primera pantalla
        stage.setScene(WelcomeView.getScene(stage));

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}