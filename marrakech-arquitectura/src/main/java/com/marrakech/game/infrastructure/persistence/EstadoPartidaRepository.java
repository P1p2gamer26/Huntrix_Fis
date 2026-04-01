package com.marrakech.game.infrastructure.persistence;

import com.marrakech.game.infrastructure.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EstadoPartidaRepository{

    public void guardarEstado(int idPartida, int turnoNumero, int assamFila, int assamColumna, String assamDireccion, String tableroEstadoJson){

        String sql = "INSERT INTO EstadoPartida " + "(turno_numero, assam_fila, assam_col, assam_direccion, tablero_estado, fecha_guardado, id_partida) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, turnoNumero);
            stmt.setInt(2, assamFila);
            stmt.setInt(3, assamColumna);
            stmt.setString(4, assamDireccion);
            stmt.setString(5, tableroEstadoJson);
            stmt.setInt(6, idPartida);

            stmt.executeUpdate();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String cargarUltimoEstado(int idPartida){

        String sql = "SELECT tablero_estado " + "FROM EstadoPartida " + "WHERE id_partida = ? " + "ORDER BY fecha_guardado DESC " + "LIMIT 1";

        try(Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, idPartida);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()){
                return rs.getString("tablero_estado");
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}