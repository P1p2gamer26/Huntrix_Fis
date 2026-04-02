package com.marrakech.game.presentation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import com.marrakech.game.infrastructure.database.DatabaseConnection;


public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Asegúrate de mover tu carpeta 'resources' aquí también
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/marrakech/game/game-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 700);
        stage.setTitle("Marrakech Game - Funcional");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args){
        DatabaseConnection.initDatabase();
        launch();
    }
}
