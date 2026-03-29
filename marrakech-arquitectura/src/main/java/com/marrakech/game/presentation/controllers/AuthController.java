package com.marrakech.game.presentation.controllers;

import com.marrakech.game.infrastructure.PartidaRepository;
import com.marrakech.game.infrastructure.PartidaRepository.Partida;
import com.marrakech.game.presentation.views.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AuthController {

    private final Stage stage;
    private final double width;
    private final double height;

    private String usuarioActual = "Jugador1";

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
        view.getBtnRegistrar().setOnAction(e -> {
            String apodo = view.getCampoApodo().getText().trim();
            if (!apodo.isEmpty()) usuarioActual = apodo;
            mostrarMenu();
        });
        stage.setScene(new Scene(view, width, height));
    }

    public void mostrarLogin() {
        LoginView view = new LoginView();
        view.getBtnEntrar().setOnAction(e -> {
            String apodo = view.getCampoApodo().getText().trim();
            if (!apodo.isEmpty()) usuarioActual = apodo;
            mostrarMenu();
        });
        stage.setScene(new Scene(view, width, height));
    }

    public void mostrarMenu() {
        MenuView view = new MenuView();
        view.getBtnJugar().setOnAction(e -> mostrarModoOnline());
        view.getBtnReglas().setOnAction(e -> mostrarReglas());
        view.getBtnConfiguracion().setOnAction(e -> mostrarConfiguracion());
        stage.setScene(new Scene(view, width, height));
    }

    public void mostrarModoOnline() {
        ModoOnlineView view = new ModoOnlineView();
        view.getBtnCrear().setOnAction(e -> mostrarCrearPartida());
        view.getBtnUnirse().setOnAction(e -> mostrarUnirsePartida());
        stage.setScene(new Scene(view, width, height));
    }

    public void mostrarCrearPartida() {
        CrearPartidaView view = new CrearPartidaView();
        view.getBtnVolver().setOnAction(e -> mostrarModoOnline());
        view.getBtnCrear().setOnAction(e -> {
            String idPartida = PartidaRepository.crearPartida(
                usuarioActual,
                view.getCantidadJugadores(),
                view.isPoderesActivados(),
                view.isPartidaRapida(),
                view.getDificultad()
            );
            Partida partida = PartidaRepository.obtenerPartida(idPartida);
            mostrarSalaEspera(partida);
        });
        stage.setScene(new Scene(view, width, height));
    }

    public void mostrarUnirsePartida() {
        UnirsePartidaView view = new UnirsePartidaView();
        view.setOnUnirse(codigo -> {
            boolean ok = PartidaRepository.unirsePartida(codigo, usuarioActual);
            if (ok) {
                Partida partida = PartidaRepository.obtenerPartida(codigo);
                mostrarSalaEspera(partida);
            }
        });
        stage.setScene(new Scene(view, width, height));
    }

    public void mostrarSalaEspera(Partida partida) {
        SalaEsperaView view = new SalaEsperaView(partida);
        view.getBtnIniciar().setOnAction(e -> mostrarJuego());
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