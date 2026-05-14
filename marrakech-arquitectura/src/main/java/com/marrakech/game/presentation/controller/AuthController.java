package com.marrakech.game.presentation.controller;

import com.marrakech.game.repository.IEstadoJuegoRepositorio;
import com.marrakech.game.repository.PartidaRepositorio.Partida;
import com.marrakech.game.service.AuthServicio;
import com.marrakech.game.service.ChatServicio;
import com.marrakech.game.service.MusicaServicio;
import com.marrakech.game.service.PartidaServicio;
import com.marrakech.game.presentation.views.*;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class AuthController {

    private final Stage          stage;
    private final double         width, height;
    private final AuthServicio   authSvc;
    private final PartidaServicio partidaSvc;
    private final ChatServicio    chatSvc;
    private final MusicaServicio  musicaSvc;
    private final IEstadoJuegoRepositorio estadoRepo;

    private String usuarioActual;

    public AuthController(Stage stage, double width, double height,
                          AuthServicio authSvc, PartidaServicio partidaSvc,
                          ChatServicio chatSvc, MusicaServicio musicaSvc,
                          IEstadoJuegoRepositorio estadoRepo) {
        this.stage       = stage;
        this.width       = width;
        this.height      = height;
        this.authSvc     = authSvc;
        this.partidaSvc  = partidaSvc;
        this.chatSvc     = chatSvc;
        this.musicaSvc   = musicaSvc;
        this.estadoRepo  = estadoRepo;

        stage.setOnCloseRequest(e -> cerrarSesionYSalir());
    }

    public void mostrarWelcome() {
        musicaSvc.reproducir(MusicaServicio.Track.MENU);
        WelcomeView v = new WelcomeView();
        v.getBtnCrearCuenta().setOnAction(e -> mostrarRegister());
        v.getBtnYaTengoCuenta().setOnAction(e -> mostrarLogin());
        v.getBtnSalir().setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Salir");
            a.setHeaderText("¿Seguro que quieres salir?");
            a.setContentText("Se cerrará la aplicación.");
            if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK)
                Platform.exit();
        });
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarRegister() {
        RegisterView v = new RegisterView();
        v.getBtnVolver().setOnAction(e -> mostrarWelcome());
        v.getBtnRegistrar().setOnAction(e -> {
            if (!v.validarTodo()) return;
            String apodo  = v.getCampoApodo().getText().trim();
            String correo = v.getCampoCorreo().getText().trim();
            String pass   = v.getCampoContrasena().getText().trim();
            String res = authSvc.registrarYLogin(apodo, correo, pass);
            if ("APODO_EXISTE".equals(res))  { v.mostrarError("Ese apodo ya está en uso."); return; }
            if ("CORREO_EXISTE".equals(res)) { v.mostrarError("Ese correo ya está registrado."); return; }
            usuarioActual = apodo;
            mostrarMenu();
        });
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarLogin() {
        LoginView v = new LoginView();
        v.getBtnVolver().setOnAction(e -> mostrarWelcome());
        v.getBtnEntrar().setOnAction(e -> {
            if (!v.validarCampos()) return;
            String apodo = v.getCampoApodo().getText().trim();
            String pass  = v.getCampoContrasena().getText().trim();
            String res   = authSvc.login(apodo, pass);
            if (res == null)               { v.mostrarError("Apodo o contraseña incorrectos."); return; }
            if ("SESION_ACTIVA".equals(res)) {
                v.mostrarError("Esta cuenta ya tiene una sesión abierta en otro dispositivo.");
                return;
            }
            usuarioActual = res;
            mostrarMenu();
        });
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarMenu() {
        musicaSvc.reproducir(MusicaServicio.Track.MENU);
        MenuView v = new MenuView(usuarioActual);
        v.getBtnJugar().setOnAction(e -> mostrarModoOnline());
        v.getBtnJugarLocal().setOnAction(e -> mostrarJuegoLocal());
        v.getBtnReglas().setOnAction(e -> mostrarReglas());
        v.getBtnConfiguracion().setOnAction(e -> mostrarConfiguracion());
        v.getTarjetaUsuario().setOnMouseClicked(e -> mostrarPerfil());
        v.getBtnCerrarSesion().setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Cerrar sesión");
            a.setHeaderText("¿Seguro que quieres cerrar sesión?");
            a.setContentText("Volverás a la pantalla de acceso.");
            if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                authSvc.cerrarSesion(usuarioActual);
                usuarioActual = null;
                mostrarWelcome();
            }
        });
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarPerfil() {
        PerfilView v = new PerfilView(
            usuarioActual,
            authSvc.getCorreo(usuarioActual),
            "Activo",
            authSvc.getFechaRegistro(usuarioActual),
            authSvc.getVictorias(usuarioActual),
            authSvc);
        v.getBtnVolver().setOnAction(e -> mostrarMenu());
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarModoOnline() {
        musicaSvc.reproducir(MusicaServicio.Track.MENU);
        ModoOnlineView v = new ModoOnlineView(partidaSvc);
        v.getBtnCrear().setOnAction(e -> mostrarCrearPartida());
        v.getBtnUnirse().setOnAction(e -> mostrarUnirsePartida());
        v.getBtnVolver().setOnAction(e -> mostrarMenu());
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarCrearPartida() {
        CrearPartidaView v = new CrearPartidaView();
        v.getBtnVolver().setOnAction(e -> mostrarModoOnline());
        v.getBtnCrear().setOnAction(e -> {
            String id = partidaSvc.crearPartida(
                usuarioActual, v.getCantidadJugadores(),
                v.isPoderesActivados(), v.isPartidaRapida(), v.getDificultad());
            Partida creada = partidaSvc.obtenerPartida(id);
            if (creada != null) mostrarSalaEspera(creada, true);
        });
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarUnirsePartida() {
        UnirsePartidaView v = new UnirsePartidaView(partidaSvc);
        v.setOnVolver(() -> mostrarModoOnline());
        v.setOnUnirse(codigo -> {
            boolean ok = partidaSvc.unirsePartida(codigo, usuarioActual);
            if (ok) {
                Partida p = partidaSvc.obtenerPartida(codigo);
                if (p != null) mostrarSalaEspera(p, false);
            } else {
                mostrarAlerta("Error", "Código inválido o sala llena.");
            }
        });
        stage.setScene(new Scene(v, width, height));
    }

    public void mostrarSalaEspera(Partida partida, boolean esHost) {
        musicaSvc.reproducir(MusicaServicio.Track.LOBBY);
        SalaEsperaView v = new SalaEsperaView(partida, esHost, partidaSvc);

        v.getBtnSalir().setOnAction(e -> {
            v.detenerPolling();
            musicaSvc.reproducir(MusicaServicio.Track.MENU);
            mostrarModoOnline();
        });

        v.getBtnIniciar().setOnAction(e -> {
            v.detenerPolling();
            partidaSvc.iniciarPartida(v.getPartidaId());
            Partida fresca = partidaSvc.obtenerPartida(v.getPartidaId());
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

        v.setOnJuegoIniciado(() -> {
            Partida act = partidaSvc.obtenerPartida(partida.id);
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

    private void mostrarJuegoLocal() {
        musicaSvc.reproducir(MusicaServicio.Track.JUEGO);
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
            gc.setServicios(musicaSvc, chatSvc);
            gc.setEstadoRepositorio(estadoRepo);
            gc.setOnVolverMenu(() -> mostrarMenu());
            gc.setOnVolverSala(() -> mostrarMenu());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el juego: " + e.getMessage());
        }
    }

    private void mostrarJuego(int n, String partidaId, String usuario, int miIndice) {
        musicaSvc.reproducir(MusicaServicio.Track.JUEGO);
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
            gc.setServicios(musicaSvc, chatSvc);
            gc.setEstadoRepositorio(estadoRepo);
            gc.iniciarConJugadores(n, partidaId, usuario, miIndice, partidaSvc);
            gc.setOnVolverSala(() -> mostrarModoOnline());
            gc.setOnVolverMenu(() -> mostrarMenu());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el juego: " + e.getMessage());
        }
    }

    private void cerrarSesionYSalir() {
        if (usuarioActual != null) authSvc.cerrarSesion(usuarioActual);
        musicaSvc.detener();
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    public String getUsuarioActual() { return usuarioActual; }
}
