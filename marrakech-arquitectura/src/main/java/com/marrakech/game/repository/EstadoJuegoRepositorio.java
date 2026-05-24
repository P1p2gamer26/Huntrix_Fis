package com.marrakech.game.repository;

import com.marrakech.game.infrastructure.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EstadoJuegoRepositorio implements IEstadoJuegoRepositorio {

    private boolean tablaCreada;

    @Override
    public void guardar(String partidaId, int turno, int ax, int ay, int adir, String tableroJson) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            asegurarTabla(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                "MERGE INTO estado_juego " +
                "(partida_id, turno_numero, assam_x, assam_y, assam_dir, tablero, listo, ts) " +
                "KEY(partida_id, turno_numero) VALUES (?,?,?,?,?,?,FALSE,CURRENT_TIMESTAMP)")) {
                ps.setString(1, partidaId); ps.setInt(2, turno);
                ps.setInt(3, ax);           ps.setInt(4, ay);
                ps.setInt(5, adir);         ps.setString(6, tableroJson);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Error guardando estado: " + e.getMessage());
        }
    }

    @Override
    public String cargarUltimo(String partidaId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT turno_numero, assam_x, assam_y, assam_dir, tablero " +
                "FROM estado_juego WHERE partida_id = ? " +
                "ORDER BY turno_numero DESC LIMIT 1")) {
            ps.setString(1, partidaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("turno_numero") + "|" + rs.getInt("assam_x") + "|" +
                       rs.getInt("assam_y")      + "|" + rs.getInt("assam_dir") + "|" +
                       rs.getString("tablero");
        } catch (Exception e) {
            System.err.println("Error leyendo estado: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void marcarListo(String partidaId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE estado_juego SET listo = TRUE " +
                "WHERE partida_id = ? AND turno_numero = (" +
                "  SELECT MAX(turno_numero) FROM estado_juego WHERE partida_id = ?)")) {
            ps.setString(1, partidaId);
            ps.setString(2, partidaId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error marcando listo: " + e.getMessage());
        }
    }

    @Override
    public boolean estaListo(String partidaId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT listo FROM estado_juego WHERE partida_id = ? " +
                "ORDER BY turno_numero DESC LIMIT 1")) {
            ps.setString(1, partidaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("listo");
        } catch (Exception e) {
            System.err.println("Error leyendo listo: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void desmarcarListo(String partidaId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE estado_juego SET listo = FALSE " +
                "WHERE partida_id = ? AND turno_numero = (" +
                "  SELECT MAX(turno_numero) FROM estado_juego WHERE partida_id = ?)")) {
            ps.setString(1, partidaId);
            ps.setString(2, partidaId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error desmarcando listo: " + e.getMessage());
        }
    }

    @Override
    public void limpiar(String partidaId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            asegurarTabla(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM estado_juego WHERE partida_id = ?")) {
                ps.setString(1, partidaId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Error limpiando estado: " + e.getMessage());
        }
    }

    private void asegurarTabla(Connection conn) throws Exception {
        if (!tablaCreada) {
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS estado_juego (" +
                "partida_id VARCHAR(20), turno_numero INT, " +
                "assam_x INT, assam_y INT, assam_dir INT, " +
                "tablero TEXT, listo BOOLEAN DEFAULT FALSE, ts TIMESTAMP, " +
                "PRIMARY KEY (partida_id, turno_numero))");
            // Si la tabla ya existía sin la columna listo, agregarla
            try {
                conn.createStatement().execute(
                    "ALTER TABLE estado_juego ADD COLUMN IF NOT EXISTS listo BOOLEAN DEFAULT FALSE");
            } catch (Exception ignored) {}
            tablaCreada = true;
        }
    }
}
