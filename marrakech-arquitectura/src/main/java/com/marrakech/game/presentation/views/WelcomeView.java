package com.marrakech.game.presentation.views;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class WelcomeView {

    public static Scene getScene(Stage stage) {

        Button crearCuenta = new Button("Crear nueva cuenta");
        Button login = new Button("Ya tengo cuenta");

        crearCuenta.setOnAction(e -> stage.setScene(RegisterView.getScene(stage)));
        login.setOnAction(e -> stage.setScene(LoginView.getScene(stage)));

        VBox root = new VBox(20, crearCuenta, login);
        root.setStyle("-fx-alignment: center;");

        return new Scene(root, 800, 600);
    }
}