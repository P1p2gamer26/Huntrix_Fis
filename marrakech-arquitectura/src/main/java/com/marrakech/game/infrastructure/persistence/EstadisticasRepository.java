package com.marrakech.game.infrastructure.persistence;

import com.marrakech.game.infrastructure.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EstadisticasRepository {

    // Crea registro de estadísticas al registrar un jugador
    public void inicializarEstadisticas(int idJugador) {
        String sql = "INSERT INTO Estadisticas (id_jugador, partidas_jugadas, partidas_ganadas, partidas_perdidas, total_monedas, ultima_actualizacion) " +
                     "VALUES (?, 0, 0, 0, 0, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idJugador);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Suma al finalizar una partida
    public void actualizarEstadisticas(int idJugador, boolean gano, int monedasFinales) {
        String sql = "UPDATE Estadisticas SET " +
                     "partidas_jugadas = partidas_jugadas + 1, " +
                     "partidas_ganadas = partidas_ganadas + ?, " +
                     "partidas_perdidas = partidas_perdidas + ?, " +
                     "total_monedas = total_monedas + ?, " +
                     "ultima_actualizacion = CURRENT_TIMESTAMP " +
                     "WHERE id_jugador = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gano ? 1 : 0);
            stmt.setInt(2, gano ? 0 : 1);
            stmt.setInt(3, monedasFinales);
            stmt.setInt(4, idJugador);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int[] obtenerEstadisticas(int idJugador) {
        String sql = "SELECT partidas_jugadas, partidas_ganadas, partidas_perdidas, total_monedas FROM Estadisticas WHERE id_jugador = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idJugador);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new int[]{
                    rs.getInt("partidas_jugadas"),
                    rs.getInt("partidas_ganadas"),
                    rs.getInt("partidas_perdidas"),
                    rs.getInt("total_monedas")
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new int[]{0, 0, 0, 0};
    }
}
