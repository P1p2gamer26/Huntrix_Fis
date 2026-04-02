package com.marrakech.game.infrastructure.persistence;

import com.marrakech.game.infrastructure.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JugadorRepository{

    public void crearJugador(String nombre, String correo, String password) {
        String sql = "INSERT INTO Jugador (nombre_usuario, correo, password, fecha_registro, estado) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, nombre);
            stmt.setString(2, correo);
            stmt.setString(3, password);
            stmt.setString(4, "ACTIVO");

            stmt.executeUpdate();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean correoExiste(String correo){
        String sql = "SELECT * FROM Jugador WHERE correo = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setString(1, correo);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }
}