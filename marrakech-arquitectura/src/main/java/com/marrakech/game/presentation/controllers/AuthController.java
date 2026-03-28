package com.marrakech.game.presentation.controllers;

import com.marrakech.game.presentation.views.ConfiguracionView;
import com.marrakech.game.presentation.views.LoginView;
import com.marrakech.game.presentation.views.MenuView;
import com.marrakech.game.presentation.views.RegisterView;
import com.marrakech.game.presentation.views.ReglasView;
import com.marrakech.game.presentation.views.WelcomeView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AuthController {

    private final Stage stage;
    private final double width;
    private final double height;

    public AuthController(Stage stage, double width, double height) {
        this.stage  = stage;
        this.width  = width;
        this.height = height;
    }

    public void mostrarWelcome() {
        WelcomeView view = new WelcomeView();
        view.getBtnCrearCuenta().setOnAction(e -> mostrarRegister());
        view.getBtnYaTengoCuenta().setOnAction(e -> mostrarLogin());
        stage.setScene(new Scene(view, width, height));
    }

    public void mostrarRegister() {
        RegisterView view = new RegisterView();
        view.getBtnRegistrar().setOnAction(e -> mostrarMenu());
        stage.setScene(new Scene(view, width, height));
    }

    public void mostrarLogin() {
        LoginView view = new LoginView();
        view.getBtnEntrar().setOnAction(e -> mostrarMenu());
        stage.setScene(new Scene(view, width, height));
    }

    public void mostrarMenu() {
        MenuView view = new MenuView();
        view.getBtnJugar().setOnAction(e -> mostrarJuego());
        view.getBtnReglas().setOnAction(e -> mostrarReglas());
        view.getBtnConfiguracion().setOnAction(e -> mostrarConfiguracion());
        stage.setScene(new Scene(view, width, height));
    }

    public void mostrarReglas() {
        ReglasView view = new ReglasView();
        view.getBtnVolver().setOnAction(e -> mostrarMenu());
        stage.setScene(new Scene(view, width, height));
    }

    public void mostrarConfiguracion() {
        ConfiguracionView view = new ConfiguracionView();
        view.getBtnVolver().setOnAction(e -> mostrarMenu());
        view.getBtnGuardar().setOnAction(e -> mostrarMenu());
        stage.setScene(new Scene(view, width, height));
    }

    private void mostrarJuego() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/marrakech/game/game-view.fxml")
            );
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(
                getClass().getResource("/com/marrakech/game/game.css").toExternalForm()
            );
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}