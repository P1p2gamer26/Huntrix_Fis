package com.marrakech.game.infrastructure.persistence;

import com.marrakech.game.infrastructure.database.DatabaseConnection;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;

public class JugadorRepository {

    private final EstadisticasRepository estadisticasRepo = new EstadisticasRepository();

    // ── Inicialización ────────────────────────────────────────────────────────

    public void crearJugador(String nombre, String correo, String password) {
        agregarColumnasExtra();
        String sql = "INSERT INTO Jugador (nombre_usuario, correo, password, fecha_registro, estado) " +
                     "VALUES (?, ?, ?, CURRENT_TIMESTAMP, 'ACTIVO')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
            stmt.setString(2, correo);
            stmt.setString(3, password);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) estadisticasRepo.inicializarEstadisticas(keys.getInt(1));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Validaciones de existencia ────────────────────────────────────────────

    public boolean correoExiste(String correo) {
        String sql = "SELECT id_jugador FROM Jugador WHERE correo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo);
            return stmt.executeQuery().next();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean nombreExiste(String nombre) {
        String sql = "SELECT id_jugador FROM Jugador WHERE nombre_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            return stmt.executeQuery().next();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ── Login / Logout con sesión única ───────────────────────────────────────

    /**
     * Intenta hacer login. Retorna:
     *   null         → credenciales incorrectas
     *   "SESION_ACTIVA" → ya hay una sesión abierta en otro lado
     *   nombre_usuario → login exitoso
     */
    public String loginJugador(String apodo, String password) {
        agregarColumnasExtra();
        String sql = "SELECT nombre_usuario, sesion_activa FROM Jugador " +
                     "WHERE nombre_usuario = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, apodo);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null; // credenciales incorrectas

            boolean sesionActiva = rs.getBoolean("sesion_activa");
            if (sesionActiva) return "SESION_ACTIVA";

            String nombre = rs.getString("nombre_usuario");
            // Marcar sesión como activa
            marcarSesion(nombre, true);
            return nombre;
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

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

    // ── Datos del perfil ──────────────────────────────────────────────────────

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

    // ── Foto de perfil ────────────────────────────────────────────────────────

    public boolean guardarFoto(String nombreUsuario, java.io.File archivoImagen) {
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

    // ── Columnas extra (idempotente) ──────────────────────────────────────────

    private void agregarColumnasExtra() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE Jugador ADD COLUMN IF NOT EXISTS foto BLOB");
            st.execute("ALTER TABLE Jugador ADD COLUMN IF NOT EXISTS sesion_activa BOOLEAN DEFAULT FALSE");
        } catch (Exception e) { /* columnas ya existen */ }
    }
}