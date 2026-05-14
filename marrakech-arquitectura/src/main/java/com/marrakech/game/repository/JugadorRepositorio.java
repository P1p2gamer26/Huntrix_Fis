package com.marrakech.game.repository;

import com.marrakech.game.infrastructure.database.DatabaseConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;

/** Acceso a datos de la tabla Jugador. Implementa {@link IJugadorRepositorio}. */
public class JugadorRepositorio implements IJugadorRepositorio {

    private final EstadisticasRepositorio estadisticasRepo;

    public JugadorRepositorio(EstadisticasRepositorio estadisticasRepo) {
        this.estadisticasRepo = estadisticasRepo;
        agregarColumnasExtra();
    }

    // ── Creación ──────────────────────────────────────────────────────────────

    @Override
    public boolean crearJugador(String nombre, String correo, String password) {
        String sql = "INSERT INTO Jugador (nombre_usuario, correo, password, " +
                     "fecha_registro, estado) VALUES (?, ?, ?, CURRENT_TIMESTAMP, 'ACTIVO')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql,
                                         PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
            stmt.setString(2, correo);
            stmt.setString(3, password);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) estadisticasRepo.inicializarEstadisticas(keys.getInt(1));
            return true;
        } catch (Exception e) {
            System.err.println("[JugadorRepositorio] Error creando jugador: " + e.getMessage());
            return false;
        }
    }

    // ── Validaciones de existencia ─────────────────────────────────────────────

    @Override
    public boolean correoExiste(String correo) {
        String sql = "SELECT id_jugador FROM Jugador WHERE correo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo);
            return stmt.executeQuery().next();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean nombreExiste(String nombre) {
        String sql = "SELECT id_jugador FROM Jugador WHERE nombre_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            return stmt.executeQuery().next();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ── Login / Logout ────────────────────────────────────────────────────────

    /**
     * Intenta autenticar al jugador.
     * @return null=credenciales incorrectas, "SESION_ACTIVA"=ya hay sesión, nombre=éxito
     */
    @Override
    public String loginJugador(String apodo, String password) {
        agregarColumnasExtra();
        String sql = "SELECT nombre_usuario, sesion_activa FROM Jugador " +
                     "WHERE nombre_usuario = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, apodo);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            String nombre = rs.getString("nombre_usuario");
            if (rs.getBoolean("sesion_activa")) marcarSesion(nombre, false);
            marcarSesion(nombre, true);
            return nombre;
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public void cerrarSesion(String nombreUsuario) {
        if (nombreUsuario == null) return;
        marcarSesion(nombreUsuario, false);
    }

    private void marcarSesion(String nombre, boolean activa) {
        String sql = "UPDATE Jugador SET sesion_activa = ? WHERE nombre_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, activa);
            stmt.setString(2, nombre);
            stmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Datos del perfil ───────────────────────────────────────────────────────

    @Override
    public int obtenerIdJugador(String nombre) {
        String sql = "SELECT id_jugador FROM Jugador WHERE nombre_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id_jugador");
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    @Override
    public String getCorreo(String nombreUsuario) {
        String sql = "SELECT correo FROM Jugador WHERE nombre_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombreUsuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("correo");
        } catch (Exception e) { e.printStackTrace(); }
        return "";
    }

    @Override
    public String getFechaRegistro(String nombreUsuario) {
        String sql = "SELECT fecha_registro FROM Jugador WHERE nombre_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombreUsuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Object fecha = rs.getObject("fecha_registro");
                return fecha != null ? fecha.toString().substring(0, 10) : "—";
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "—";
    }

    // ── Foto de perfil ─────────────────────────────────────────────────────────

    @Override
    public boolean guardarFoto(String nombreUsuario, File archivoImagen) {
        agregarColumnasExtra();
        String sql = "UPDATE Jugador SET foto = ? WHERE nombre_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             FileInputStream fis = new FileInputStream(archivoImagen)) {
            stmt.setBinaryStream(1, fis, (int) archivoImagen.length());
            stmt.setString(2, nombreUsuario);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    @Override
    public byte[] getFoto(String nombreUsuario) {
        agregarColumnasExtra();
        String sql = "SELECT foto FROM Jugador WHERE nombre_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombreUsuario);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                InputStream is = rs.getBinaryStream("foto");
                if (is != null) return is.readAllBytes();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ── Inicialización de tabla (idempotente) ──────────────────────────────────

    private void agregarColumnasExtra() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS Jugador (" +
                "id_jugador INT AUTO_INCREMENT PRIMARY KEY, " +
                "nombre_usuario VARCHAR(50) UNIQUE NOT NULL, " +
                "correo VARCHAR(100) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "estado VARCHAR(15))");
            st.execute("ALTER TABLE Jugador ADD COLUMN IF NOT EXISTS fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            st.execute("ALTER TABLE Jugador ADD COLUMN IF NOT EXISTS foto BLOB");
            st.execute("ALTER TABLE Jugador ADD COLUMN IF NOT EXISTS sesion_activa BOOLEAN DEFAULT FALSE");
        } catch (Exception e) {
            System.err.println("[JugadorRepositorio] Error en tabla Jugador: " + e.getMessage());
        }
    }
}
