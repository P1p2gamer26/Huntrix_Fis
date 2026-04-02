package com.marrakech.game.infrastructure.persistence;

import com.marrakech.game.infrastructure.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EstadisticasRepository{

    public void actualizarEstadisticas(int idJugador, boolean gano, int monedasFinales){

        String sql = "MERGE INTO Estadisticas " + "(id_jugador, partidas_jugadas, partidas_ganadas, partidas_perdidas, total_monedas, ultima_actualizacion) " + "KEY(id_jugador) " + "VALUES (?, 1, ?, ?, ?, CURRENT_TIMESTAMP)";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, idJugador);
            stmt.setInt(2, gano ? 1 : 0);
            stmt.setInt(3, gano ? 0 : 1);
            stmt.setInt(4, monedasFinales);

            stmt.executeUpdate();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}