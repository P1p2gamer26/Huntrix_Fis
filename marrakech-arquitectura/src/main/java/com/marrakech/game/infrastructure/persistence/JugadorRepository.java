package com.marrakech.game.infrastructure.persistence;

import com.marrakech.game.infrastructure.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JugadorRepository {

    public void crearJugador(String nombre, String correo, String password) {
        String sql = "INSERT INTO Jugador (nombre_usuario, correo, password, fecha_registro, estado) VALUES (?, ?, ?, CURRENT_TIMESTAMP, 'ACTIVO')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setString(2, correo);
            stmt.setString(3, password);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean correoExiste(String correo) {
        String sql = "SELECT id_jugador FROM Jugador WHERE correo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo);
            return stmt.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Retorna el nombre_usuario si credenciales correctas, null si no
    public String loginJugador(String apodo, String password) {
        String sql = "SELECT nombre_usuario FROM Jugador WHERE nombre_usuario = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, apodo);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("nombre_usuario");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean nombreExiste(String nombre) {
        String sql = "SELECT id_jugador FROM Jugador WHERE nombre_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            return stmt.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
