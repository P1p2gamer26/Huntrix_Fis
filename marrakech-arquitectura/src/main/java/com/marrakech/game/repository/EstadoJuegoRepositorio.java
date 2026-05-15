package com.marrakech.game.repository;

import com.marrakech.game.infrastructure.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class EstadoJuegoRepositorio implements IEstadoJuegoRepositorio {

    private boolean tablaCreada;

    @Override
    public void guardar(String partidaId, int turno, int ax, int ay, int adir, String tableroJson) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            asegurarTabla(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                "MERGE INTO estado_juego " +
                "(partida_id, turno_numero, assam_x, assam_y, assam_dir, tablero, ts) " +
                "KEY(partida_id, turno_numero) VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)")) {
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

    private void asegurarTabla(Connection conn) throws Exception {
        if (!tablaCreada) {
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS estado_juego (" +
                "partida_id VARCHAR(20), turno_numero INT, " +
                "assam_x INT, assam_y INT, assam_dir INT, " +
                "tablero TEXT, ts TIMESTAMP, " +
                "PRIMARY KEY (partida_id, turno_numero))");
            tablaCreada = true;
        }
    }
}
