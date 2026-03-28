package com.marrakech.game.presentation.views;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginView {

    public static Scene getScene(Stage stage) {

        TextField user = new TextField();
        user.setPromptText("Apodo");

        PasswordField pass = new PasswordField();
        pass.setPromptText("Contraseña");

        Button entrar = new Button("Entrar");

        entrar.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        LoginView.class.getResource("/com/marrakech/game/game-view.fxml")
                );

                Scene gameScene = new Scene(loader.load(), 1100, 700);
                stage.setScene(gameScene);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox root = new VBox(10, user, pass, entrar);
        root.setStyle("-fx-alignment: center;");

        return new Scene(root, 800, 600);
    }
}