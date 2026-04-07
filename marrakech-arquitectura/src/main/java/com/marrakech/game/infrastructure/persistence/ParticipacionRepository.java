package com.marrakech.game.infrastructure.persistence;

import com.marrakech.game.infrastructure.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ParticipacionRepository {

    public void guardarParticipacion(int idPartida, int idJugador, String nombrePartida,
                                     String color, int monedasActuales,
                                     int alfombrasRestantes, int alfombrasColocadas,
                                     boolean esGanador) {
        String sql = "INSERT INTO Participacion " +
                     "(nombre_partida, rol, color, listo, estado_conexion, monedas_actuales, " +
                     "alfombras_restantes, alfombras_colocadas, es_ganador, fecha_ultimo_cambio, id_partida, id_jugador) " +
                     "VALUES (?, 'JUGADOR', ?, TRUE, 'ACTIVO', ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombrePartida);
            stmt.setString(2, color);
            stmt.setInt(3, monedasActuales);
            stmt.setInt(4, alfombrasRestantes);
            stmt.setInt(5, alfombrasColocadas);
            stmt.setBoolean(6, esGanador);
            stmt.setInt(7, idPartida);
            stmt.setInt(8, idJugador);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet obtenerParticipacionJugador(int idPartida, int idJugador) {
        String sql = "SELECT * FROM Participacion WHERE id_partida = ? AND id_jugador = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idPartida);
            stmt.setInt(2, idJugador);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
