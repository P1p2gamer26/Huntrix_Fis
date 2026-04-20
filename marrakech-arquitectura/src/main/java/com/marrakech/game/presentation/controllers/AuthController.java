package com.marrakech.game.presentation.controllers;

import com.marrakech.game.infrastructure.PartidaRepository;
import com.marrakech.game.infrastructure.PartidaRepository.Partida;
import com.marrakech.game.infrastructure.database.DatabaseConnection;
import com.marrakech.game.infrastructure.persistence.JugadorRepository;
import com.marrakech.game.presentation.MusicaManager;
import com.marrakech.game.presentation.views.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.sql.*;

public class AuthController {

    private final Stage stage;
    private final double width, height;
    private String usuarioActual;
    private final JugadorRepository jugadorRepo = new JugadorRepository();

    public AuthController(Stage stage, double width, double height) {
        this.stage = stage; this.width = width; this.height = height;
    }

    public void mostrarWelcome() {
        MusicaManager.getInstance().reproducir(MusicaManager.Track.MENU);
        WelcomeView v = new WelcomeView();
        v.getBtnCrearCuenta().setOnAction(e -> mostrarRegister());
        v.getBtnYaTengoCuenta().setOnAction(e -> mostrarLogin());
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarRegister() {
        RegisterView v = new RegisterView();
        v.getBtnVolver().setOnAction(e -> mostrarWelcome());
        v.getBtnRegistrar().setOnAction(e -> {
            String apodo  = v.getCampoApodo().getText().trim();
            String correo = v.getCampoCorreo().getText().trim();
            String pass   = v.getCampoContrasena().getText().trim();
            if (apodo.isEmpty()||correo.isEmpty()||pass.isEmpty()){mostrarAlerta("Error","Todos los campos son obligatorios.");return;}
            if (jugadorRepo.nombreExiste(apodo)){mostrarAlerta("Error","Apodo en uso.");return;}
            if (jugadorRepo.correoExiste(correo)){mostrarAlerta("Error","Correo ya registrado.");return;}
            jugadorRepo.crearJugador(apodo, correo, pass);
            usuarioActual = apodo; mostrarMenu();
        });
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarLogin() {
        LoginView v = new LoginView();
        v.getBtnVolver().setOnAction(e -> mostrarWelcome());
        v.getBtnEntrar().setOnAction(e -> {
            String apodo = v.getCampoApodo().getText().trim();
            String pass  = v.getCampoContrasena().getText().trim();
            if (apodo.isEmpty()||pass.isEmpty()){mostrarAlerta("Error","Ingresa apodo y contraseña.");return;}
            String nombre = jugadorRepo.loginJugador(apodo, pass);
            if (nombre==null){mostrarAlerta("Error","Credenciales incorrectas.");return;}
            usuarioActual = nombre; mostrarMenu();
        });
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarMenu() {
        limpiarSalasViejas();
        MusicaManager.getInstance().reproducir(MusicaManager.Track.MENU);
        MenuView v = new MenuView(usuarioActual);
        v.getBtnJugar().setOnAction(e -> mostrarModoOnline());
        v.getBtnReglas().setOnAction(e -> mostrarReglas());
        v.getBtnConfiguracion().setOnAction(e -> mostrarConfiguracion());
        v.getTarjetaUsuario().setOnMouseClicked(e -> mostrarPerfil());
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarPerfil() {
        String correo        = jugadorRepo.getCorreo(usuarioActual);
        String fechaRegistro = jugadorRepo.getFechaRegistro(usuarioActual);
        int victorias = PartidaRepository.obtenerRanking().stream()
            .filter(r -> r.usuario.equals(usuarioActual))
            .mapToInt(r -> r.victorias).findFirst().orElse(0);
        PerfilView v = new PerfilView(
            usuarioActual,
            correo        != null ? correo        : "",
            "Activo",
            fechaRegistro != null ? fechaRegistro : "—",
            victorias);
        v.getBtnVolver().setOnAction(e -> mostrarMenu());
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarModoOnline() {
        MusicaManager.getInstance().reproducir(MusicaManager.Track.MENU);
        ModoOnlineView v = new ModoOnlineView();
        v.getBtnCrear().setOnAction(e -> mostrarCrearPartida());
        v.getBtnUnirse().setOnAction(e -> mostrarUnirsePartida());
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarCrearPartida() {
        CrearPartidaView v = new CrearPartidaView();
        v.getBtnVolver().setOnAction(e -> mostrarModoOnline());
        v.getBtnCrear().setOnAction(e -> {
            String id = PartidaRepository.crearPartida(
                usuarioActual, v.getCantidadJugadores(),
                v.isPoderesActivados(), v.isPartidaRapida(), v.getDificultad());
            Partida creada = PartidaRepository.obtenerPartida(id);
            if (creada != null) mostrarSalaEspera(creada, true);
        });
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarUnirsePartida() {
        UnirsePartidaView v = new UnirsePartidaView();
        v.setOnVolver(() -> mostrarModoOnline());
        v.setOnUnirse(codigo -> {
            boolean ok = PartidaRepository.unirsePartida(codigo, usuarioActual);
            if (ok) {
                Partida p = PartidaRepository.obtenerPartida(codigo);
                if (p != null) mostrarSalaEspera(p, false);
            } else {
                mostrarAlerta("Error", "Código inválido o sala llena.");
            }
        });
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarSalaEspera(Partida partida, boolean esHost) {
        MusicaManager.getInstance().reproducir(MusicaManager.Track.LOBBY);
        SalaEsperaView v = new SalaEsperaView(partida, esHost);

        v.getBtnSalir().setOnAction(e -> {
            v.detenerPolling();
            MusicaManager.getInstance().reproducir(MusicaManager.Track.MENU);
            mostrarModoOnline();
        });

        // HOST: marca la partida como INICIADA y espera 2.5s para que el guest
        // también detecte el cambio, así entran al tablero al mismo tiempo
        v.getBtnIniciar().setOnAction(e -> {
            v.detenerPolling();
            PartidaRepository.iniciarPartida(v.getPartidaId());
            Partida fresca = PartidaRepository.obtenerPartida(v.getPartidaId());
            int miIdx = (fresca != null) ? fresca.jugadores.indexOf(usuarioActual) : 0;
            if (miIdx < 0) miIdx = 0;
            final int    idxFinal = miIdx;
            final int    nFinal   = v.getNumJugadores();
            final String pidFinal = v.getPartidaId();
            new Thread(() -> {
                try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> mostrarJuego(nFinal, pidFinal, usuarioActual, idxFinal));
            }, "host-delay").start();
        });

        // GUEST: entra al tablero cuando detecta la partida INICIADA por polling
        v.setOnJuegoIniciado(() -> {
            Partida act = PartidaRepository.obtenerPartida(partida.id);
            int n     = act != null ? act.maxJugadores : 2;
            int miIdx = act != null ? act.jugadores.indexOf(usuarioActual) : 1;
            if (miIdx < 0) miIdx = 1;
            mostrarJuego(n, partida.id, usuarioActual, miIdx);
        });

        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarReglas() {
        ReglasView v = new ReglasView();
        v.getBtnVolver().setOnAction(e -> mostrarMenu());
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarConfiguracion() {
        ConfiguracionView v = new ConfiguracionView();
        v.getBtnVolver().setOnAction(e -> mostrarMenu());
        v.getBtnGuardar().setOnAction(e -> mostrarMenu());
        stage.setScene(new Scene(v, width, height));
    }

    private void mostrarJuego(int n, String partidaId, String usuario, int miIndice) {
        MusicaManager.getInstance().reproducir(MusicaManager.Track.JUEGO);
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/marrakech/game/game-view.fxml"));
            Parent root = loader.load();
            GameController gc = loader.getController();
            Scene scene = new Scene(root, width, height);
            try {
                scene.getStylesheets().add(
                    getClass().getResource("/com/marrakech/game/game.css").toExternalForm());
            } catch (Exception ignored) {}
            stage.setScene(scene);
            gc.iniciarConJugadores(n, partidaId, usuario, miIndice);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el juego: " + e.getMessage());
        }
    }

    private void limpiarSalasViejas() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("DELETE FROM partida_jugadores WHERE partida_id IN " +
                "(SELECT id FROM partidas WHERE estado = 'INICIADA')");
            st.execute("DELETE FROM partidas WHERE estado = 'INICIADA'");
        } catch (Exception e) { /* ignorar */ }
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    public String getUsuarioActual() { return usuarioActual; }
}