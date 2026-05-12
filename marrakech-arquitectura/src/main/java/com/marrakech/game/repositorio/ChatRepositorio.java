package com.marrakech.game.repositorio;

import com.marrakech.game.infrastructure.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de mensajes de chat. */
public class ChatRepositorio implements IChatRepositorio {

    /** Mensaje de chat (solo datos). */
    public static class Mensaje {
        public final int    id;
        public final String usuario;
        public final String texto;
        public final String hora;

        public Mensaje(int id, String usuario, String texto, String hora) {
            this.id      = id;
            this.usuario = usuario;
            this.texto   = texto;
            this.hora    = hora;
        }
    }

    @Override
    public void inicializarTabla() {
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

    @Override
    public void enviarMensaje(String partidaId, String usuario, String texto) {
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

    @Override
    public List<Mensaje> obtenerMensajes(String partidaId, int desdeId) {
        List<Mensaje> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT id, usuario, texto, hora FROM chat_mensajes " +
                "WHERE partida_id = ? AND id > ? ORDER BY id ASC LIMIT 50")) {
            ps.setString(1, partidaId);
            ps.setInt(2, desdeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                lista.add(new Mensaje(
                    rs.getInt("id"),
                    rs.getString("usuario"),
                    rs.getString("texto"),
                    rs.getString("hora")));
        } catch (Exception e) { /* tabla puede no existir aún */ }
        return lista;
    }

    @Override
    public int obtenerUltimoId(String partidaId) {
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
