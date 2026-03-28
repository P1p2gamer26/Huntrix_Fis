package com.marrakech.game.presentation.views;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterView {

    public static Scene getScene(Stage stage) {

        TextField user = new TextField();
        user.setPromptText("Apodo");

        PasswordField pass = new PasswordField();
        pass.setPromptText("Contraseña");

        Button registrar = new Button("Registrar");

        registrar.setOnAction(e -> {
            // luego guardas usuario
            stage.setScene(LoginView.getScene(stage));
        });

        VBox root = new VBox(10, user, pass, registrar);
        root.setStyle("-fx-alignment: center;");

        return new Scene(root, 800, 600);
    }
}