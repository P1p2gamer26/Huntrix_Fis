package com.marrakech.game.presentation;

import java.sql.Connection;
import java.sql.Statement;

import com.marrakech.game.infrastructure.audio.MusicaManager;
import com.marrakech.game.infrastructure.database.DatabaseConnection;
import com.marrakech.game.presentation.controller.AuthController;
import com.marrakech.game.repository.ChatRepositorio;
import com.marrakech.game.repository.EstadisticasRepositorio;
import com.marrakech.game.repository.EstadoJuegoRepositorio;
import com.marrakech.game.repository.IChatRepositorio;
import com.marrakech.game.repository.IEstadoJuegoRepositorio;
import com.marrakech.game.repository.IJugadorRepositorio;
import com.marrakech.game.repository.IPartidaRepositorio;
import com.marrakech.game.repository.JugadorRepositorio;
import com.marrakech.game.repository.PartidaRepositorio;
import com.marrakech.game.service.AuthServicio;
import com.marrakech.game.service.ChatServicio;
import com.marrakech.game.service.MusicaServicio;
import com.marrakech.game.service.PartidaServicio;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Marrakech Game");
        stage.setMaximized(true);

        EstadisticasRepositorio  estadisticasRepo = new EstadisticasRepositorio();
        JugadorRepositorio       jugadorRepoRaw   = new JugadorRepositorio(estadisticasRepo);
        IJugadorRepositorio      jugadorRepo      = jugadorRepoRaw;
        IPartidaRepositorio      partidaRepo      = new PartidaRepositorio();
        IChatRepositorio         chatRepo         = new ChatRepositorio();
        IEstadoJuegoRepositorio  estadoRepo       = new EstadoJuegoRepositorio();

        // Asegurar que la tabla Jugador existe ANTES de resetear sesiones
        jugadorRepoRaw.garantizarTablaJugador();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("UPDATE Jugador SET sesion_activa = FALSE");
            System.out.println("[Main] Sesiones activas reseteadas.");
        } catch (Exception e) {
            System.err.println("[Main] Error reseteando sesiones: " + e.getMessage());
        }

        // Asegurar tablas de sincronización desde el inicio
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS estado_juego (" +
                "partida_id VARCHAR(20), turno_numero INT, " +
                "assam_x INT, assam_y INT, assam_dir INT, " +
                "tablero TEXT, listo BOOLEAN DEFAULT FALSE, ts TIMESTAMP, " +
                "PRIMARY KEY (partida_id, turno_numero))");
            st.execute("ALTER TABLE estado_juego ADD COLUMN IF NOT EXISTS listo BOOLEAN DEFAULT FALSE");
        } catch (Exception e) {
            System.err.println("[Main] Error creando estado_juego: " + e.getMessage());
        }

        MusicaServicio  musicaSvc  = new MusicaServicio();
        AuthServicio    authSvc    = new AuthServicio(jugadorRepo, partidaRepo);
        PartidaServicio partidaSvc = new PartidaServicio(partidaRepo);
        ChatServicio    chatSvc    = new ChatServicio(chatRepo);

        seedUsuariosPrueba(authSvc);
        seedRanking(partidaRepo);

        AuthController auth = new AuthController(stage, 1100, 700,
                                                 authSvc, partidaSvc, chatSvc, musicaSvc,
                                                 estadoRepo);

        musicaSvc.reproducir(MusicaServicio.Track.MENU);
        auth.mostrarWelcome();
        stage.show();
    }

    private void seedUsuariosPrueba(AuthServicio authSvc) {
        String[][] usuarios = {
            {"pipe", "pipe@test.com", "123456"},
            {"admin", "admin@test.com", "admin123"},
            {"test", "test@test.com", "test123"}
        };
        for (String[] u : usuarios) {
            String apodo  = u[0];
            String correo = u[1];
            String pass   = u[2];
            String res    = authSvc.registrarYLogin(apodo, correo, pass);
            if (res != null && res.equals(apodo)) {
                authSvc.cerrarSesion(apodo);
                System.out.println(">>> Usuario de prueba creado: " + apodo + " / " + pass);
            } else if ("APODO_EXISTE".equals(res) || "CORREO_EXISTE".equals(res)) {
                System.out.println(">>> Usuario ya existe: " + apodo + " / " + pass);
            }
        }
    }

    private void seedRanking(IPartidaRepositorio partidaRepo) {
        String[][] rankings = {
            {"pipe", "12"}, {"admin", "8"}, {"test", "5"},
            {"aladdin", "3"}, {"jasmine", "2"}
        };
        for (String[] r : rankings) {
            try (java.sql.Connection conn = com.marrakech.game.infrastructure.database.DatabaseConnection.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(
                    "MERGE INTO ranking (usuario, victorias) KEY(usuario) VALUES (?, ?)")) {
                ps.setString(1, r[0]);
                ps.setInt(2, Integer.parseInt(r[1]));
                ps.executeUpdate();
            } catch (Exception e) {
                System.err.println(">>> Error insertando ranking para " + r[0] + ": " + e.getMessage());
            }
        }
        System.out.println(">>> Ranking de prueba insertado.");
    }

    @Override
    public void stop() {
        MusicaManager.getInstance().detener();
    }

    public static void main(String[] args) {
        DatabaseConnection.initDatabase();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS chat_mensajes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "partida_id VARCHAR(20) NOT NULL, " +
                "usuario VARCHAR(60) NOT NULL, " +
                "texto VARCHAR(500) NOT NULL, " +
                "hora VARCHAR(8) NOT NULL, " +
                "ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        
        launch();
    }
}