package com.marrakech.game.infrastructure.persistence;

import com.marrakech.game.infrastructure.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PartidaRepository {

    public void crearPartida(int idSala) {
        String sql = "INSERT INTO Partida (estado, fecha_inicio, id_sala) VALUES ('EN_CURSO', CURRENT_TIMESTAMP, ?)";

        try(Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, idSala);
            stmt.executeUpdate();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public int obtenerPartidaActiva(int idSala){
        String sql = "SELECT id_partida FROM Partida WHERE id_sala = ? AND estado = 'EN_CURSO'";

        try(Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1, idSala);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()){
                return rs.getInt("id_partida");
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return -1;
    }
}