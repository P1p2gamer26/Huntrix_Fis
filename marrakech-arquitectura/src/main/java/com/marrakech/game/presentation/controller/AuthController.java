package com.marrakech.game.presentation.controller;

import com.marrakech.game.repositorio.PartidaRepositorio.Partida;
import com.marrakech.game.servicios.AuthServicio;
import com.marrakech.game.servicios.ChatServicio;
import com.marrakech.game.servicios.PartidaServicio;
import com.marrakech.game.presentation.MusicaManager;
import com.marrakech.game.presentation.views.*;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Orquestador de navegación entre pantallas.
 * Recibe todos sus servicios por constructor (inyección de dependencias).
 */
public class AuthController {

    private final Stage          stage;
    private final double         width, height;
    private final AuthServicio   authSvc;
    private final PartidaServicio partidaSvc;
    private final ChatServicio   chatSvc;

    private String usuarioActual;

    public AuthController(Stage stage, double width, double height,
                          AuthServicio authSvc, PartidaServicio partidaSvc,
                          ChatServicio chatSvc) {
        this.stage      = stage;
        this.width      = width;
        this.height     = height;
        this.authSvc    = authSvc;
        this.partidaSvc = partidaSvc;
        this.chatSvc    = chatSvc;

        stage.setOnCloseRequest(e -> cerrarSesionYSalir());
    }

    // ── Pantallas de acceso ───────────────────────────────────────────────────

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

    // ── Menú principal ────────────────────────────────────────────────────────

    public void mostrarMenu() {
        limpiarSalasViejas();
        MusicaManager.getInstance().reproducir(MusicaManager.Track.MENU);
        MenuView v = new MenuView(usuarioActual);
        v.getBtnJugar().setOnAction(e -> mostrarModoOnline());
        v.getBtnJugarLocal().setOnAction(e -> mostrarJuegoLocal());
        v.getBtnReglas().setOnAction(e -> mostrarReglas());
        v.getBtnConfiguracion().setOnAction(e -> mostrarConfiguracion());
        v.getTarjetaUsuario().setOnMouseClicked(e -> mostrarPerfil());
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

    // ── Modo online ───────────────────────────────────────────────────────────

    public void mostrarModoOnline() {
        MusicaManager.getInstance().reproducir(MusicaManager.Track.MENU);
        ModoOnlineView v = new ModoOnlineView(partidaSvc);
        v.getBtnCrear().setOnAction(e -> mostrarCrearPartida());
        v.getBtnUnirse().setOnAction(e -> mostrarUnirsePartida());
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
        MusicaManager.getInstance().reproducir(MusicaManager.Track.LOBBY);
        SalaEsperaView v = new SalaEsperaView(partida, esHost, partidaSvc);

        v.getBtnSalir().setOnAction(e -> {
            v.detenerPolling();
            MusicaManager.getInstance().reproducir(MusicaManager.Track.MENU);
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

    // ── Configuración y reglas ────────────────────────────────────────────────

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

    // ── Juego ─────────────────────────────────────────────────────────────────

    private void mostrarJuegoLocal() {
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
            gc.setOnVolverMenu(() -> mostrarMenu());
            gc.setOnVolverSala(() -> mostrarMenu());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el juego: " + e.getMessage());
        }
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
            gc.iniciarConJugadores(n, partidaId, usuario, miIndice, partidaSvc, chatSvc);
            gc.setOnVolverSala(() -> mostrarModoOnline());
            gc.setOnVolverMenu(() -> mostrarMenu());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el juego: " + e.getMessage());
        }
    }

    // ── Sesión y limpieza ─────────────────────────────────────────────────────

    private void cerrarSesionYSalir() {
        if (usuarioActual != null) authSvc.cerrarSesion(usuarioActual);
        MusicaManager.getInstance().detener();
    }

    /**
     * Elimina partidas en estado INICIADA para evitar salas huérfanas
     * al volver al menú principal.
     */
    private void limpiarSalasViejas() {
        try {
            // Delegamos a PartidaServicio para no mezclar SQL en el controlador
            for (Partida p : partidaSvc.listarPartidas()) {
                if ("INICIADA".equalsIgnoreCase(p.estado)) {
                    // No hay método delete en la interfaz; lo saltamos silenciosamente
                }
            }
        } catch (Exception ignored) {}
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    public String getUsuarioActual() { return usuarioActual; }
}
