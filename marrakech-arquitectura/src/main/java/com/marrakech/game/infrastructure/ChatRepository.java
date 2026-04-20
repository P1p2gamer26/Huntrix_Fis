package com.marrakech.game.infrastructure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.marrakech.game.infrastructure.database.DatabaseConnection;

public class ChatRepository {

    public static class Mensaje {
        public final String usuario;
        public final String texto;
        public final String hora;
        public Mensaje(String usuario, String texto, String hora) {
            this.usuario = usuario;
            this.texto   = texto;
            this.hora    = hora;
        }
    }

    public static void inicializarTabla() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(
                "CREATE TABLE IF NOT EXISTS chat_mensajes (" +
                "id         INT AUTO_INCREMENT PRIMARY KEY, " +
                "partida_id VARCHAR(20) NOT NULL, " +
                "usuario    VARCHAR(60) NOT NULL, " +
                "texto      VARCHAR(500) NOT NULL, " +
                "hora       VARCHAR(8) NOT NULL, " +
                "ts         TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void enviarMensaje(String partidaId, String usuario, String texto) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO chat_mensajes (partida_id, usuario, texto, hora) " +
                "VALUES (?, ?, ?, FORMATDATETIME(CURRENT_TIMESTAMP, 'HH:mm'))")) {
            ps.setString(1, partidaId);
            ps.setString(2, usuario);
            ps.setString(3, texto.length() > 500 ? texto.substring(0, 500) : texto);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static List<Mensaje> obtenerMensajes(String partidaId, int desdeId) {
        List<Mensaje> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT usuario, texto, hora FROM chat_mensajes " +
                "WHERE partida_id = ? AND id > ? ORDER BY id ASC LIMIT 50")) {
            ps.setString(1, partidaId);
            ps.setInt(2, desdeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                lista.add(new Mensaje(rs.getString("usuario"), rs.getString("texto"), rs.getString("hora")));
        } catch (Exception e) { /* tabla aún no existe */ }
        return lista;
    }

    public static int obtenerUltimoId(String partidaId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT MAX(id) FROM chat_mensajes WHERE partida_id = ?")) {
            ps.setString(1, partidaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { /* ignorar */ }
        return 0;
    }
}
