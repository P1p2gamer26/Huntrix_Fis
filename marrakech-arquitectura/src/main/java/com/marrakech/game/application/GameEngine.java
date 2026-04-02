package com.marrakech.application;

import javafx.stage.Stage;
import com.marrakech.game.presentation.Main;

public class GameEngine {

    public void start(Stage stage) {
        new Main().start(stage);
    }
}