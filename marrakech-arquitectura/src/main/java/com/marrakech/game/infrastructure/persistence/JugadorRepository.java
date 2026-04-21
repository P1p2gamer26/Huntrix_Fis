package com.marrakech.game.infrastructure.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import com.marrakech.game.domain.valdata.Email;
import com.marrakech.game.domain.valdata.Nombre;
import com.marrakech.game.domain.valdata.Password;


import com.marrakech.game.infrastructure.database.DatabaseConnection;

public class JugadorRepository {

    private final EstadisticasRepository estadisticasRepo = new EstadisticasRepository();

    public void crearJugador(String nombre, String correo, String password){

        try{
            Nombre nombreValido = new Nombre(nombre);
            Email emailValido = new Email(correo);
            Password passwordValido = new Password(password);

            String sql = "INSERT INTO Jugador (nombre_usuario, correo, password, fecha_registro, estado) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)";

            try(Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)){

                stmt.setString(1, nombreValido.value());
                stmt.setString(2, emailValido.value());
                stmt.setString(3, passwordValido.value());
                stmt.setString(4, "ACTIVO");

                stmt.executeUpdate();
            }

        }catch (IllegalArgumentException e){
            System.out.println("Error de validacion: " + e.getMessage());
        }catch(Exception e){
            e.printStackTrace();
        }
    }


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

    public String loginJugador(String apodo, String password) {
        String sql = "SELECT nombre_usuario FROM Jugador WHERE nombre_usuario = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, apodo);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("nombre_usuario");
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

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

    public boolean guardarFoto(String nombreUsuario, File archivoImagen) {
        agregarColumnaFotoSiNoExiste();
        String sql = "UPDATE Jugador SET foto = ? WHERE nombre_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             FileInputStream fis = new FileInputStream(archivoImagen)) {
            stmt.setBinaryStream(1, fis, (int) archivoImagen.length());
            stmt.setString(2, nombreUsuario);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public byte[] getFoto(String nombreUsuario) {
        agregarColumnaFotoSiNoExiste();
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

    private void agregarColumnaFotoSiNoExiste() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE Jugador ADD COLUMN IF NOT EXISTS foto BLOB");
        } catch (Exception e) { /* columna ya existe */ }
    }
}