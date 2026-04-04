package com.marrakech.game.infrastructure;

import java.sql.*;
import java.util.*;
import com.marrakech.game.infrastructure.database.DatabaseConnection;

public class PartidaRepository {

    public static class Partida {
        public final String id;
        public final String nombre;
        public final int maxJugadores;
        public final boolean poderesActivados;
        public final boolean partidaRapida;
        public final String dificultad;
        public final List<String> jugadores;
        public final String estado; // "ESPERANDO" | "INICIADA"

        public Partida(String id, String nombre, int maxJugadores,
                       boolean poderesActivados, boolean partidaRapida,
                       String dificultad, List<String> jugadores, String estado) {
            this.id               = id;
            this.nombre           = nombre;
            this.maxJugadores     = maxJugadores;
            this.poderesActivados = poderesActivados;
            this.partidaRapida    = partidaRapida;
            this.dificultad       = dificultad;
            this.jugadores        = jugadores;
            this.estado           = estado;
        }

        public String resumen() {
            return nombre + " (" + jugadores.size() + "/" + maxJugadores + ") | Código: " + id;
        }
    }

    public static class RankingEntry {
        public final String usuario;
        public final int victorias;
        public RankingEntry(String u, int v) { usuario = u; victorias = v; }
    }

    static { inicializarTablas(); }

    private static void inicializarTablas() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS partidas (" +
                "id VARCHAR(20) PRIMARY KEY," +
                "nombre VARCHAR(100)," +
                "max_jugadores INT," +
                "poderes BOOLEAN," +
                "rapida BOOLEAN," +
                "dificultad VARCHAR(20)," +
                "estado VARCHAR(20) DEFAULT 'ESPERANDO'" +
                ")");
            st.execute("CREATE TABLE IF NOT EXISTS partida_jugadores (" +
                "partida_id VARCHAR(20)," +
                "usuario VARCHAR(100)," +
                "PRIMARY KEY (partida_id, usuario)" +
                ")");
            st.execute("CREATE TABLE IF NOT EXISTS ranking (" +
                "usuario VARCHAR(100) PRIMARY KEY," +
                "victorias INT DEFAULT 0" +
                ")");
            // Agregar columna estado si ya existe la tabla sin ella
            try { st.execute("ALTER TABLE partidas ADD COLUMN IF NOT EXISTS estado VARCHAR(20) DEFAULT 'ESPERANDO'"); }
            catch (Exception ignored) {}
        } catch (SQLException e) {
            System.err.println("Error inicializando tablas: " + e.getMessage());
        }
    }

    public static String crearPartida(String nombreCreador, int maxJugadores,
                                      boolean poderes, boolean rapida, String dificultad) {
        String id     = generarId();
        String nombre = nombreCreador.isEmpty() ? "Sala-" + id : nombreCreador + "'s Sala";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO partidas VALUES (?, ?, ?, ?, ?, ?, 'ESPERANDO')")) {
                ps.setString(1, id); ps.setString(2, nombre);
                ps.setInt(3, maxJugadores); ps.setBoolean(4, poderes);
                ps.setBoolean(5, rapida); ps.setString(6, dificultad);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO partida_jugadores VALUES (?, ?)")) {
                ps.setString(1, id);
                ps.setString(2, nombreCreador.isEmpty() ? "Jugador1" : nombreCreador);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error creando partida: " + e.getMessage());
        }
        return id;
    }

    public static boolean unirsePartida(String id, String nombreJugador) {
        Partida p = obtenerPartida(id);
        if (p == null) return false;
        if (p.jugadores.size() >= p.maxJugadores) return false;
        if (p.jugadores.contains(nombreJugador)) return true;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO partida_jugadores VALUES (?, ?)")) {
            ps.setString(1, id.toUpperCase());
            ps.setString(2, nombreJugador);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error uniéndose: " + e.getMessage());
            return false;
        }
    }

    public static void iniciarPartida(String id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE partidas SET estado = 'INICIADA' WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error iniciando partida: " + e.getMessage());
        }
    }

    public static Partida obtenerPartida(String id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM partidas WHERE id = ?")) {
            ps.setString(1, id.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return construirPartida(conn, rs);
        } catch (SQLException e) {
            System.err.println("Error obteniendo partida: " + e.getMessage());
        }
        return null;
    }

    public static List<Partida> listarPartidas() {
        List<Partida> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM partidas WHERE estado = 'ESPERANDO'")) {
            while (rs.next()) lista.add(construirPartida(conn, rs));
        } catch (SQLException e) {
            System.err.println("Error listando: " + e.getMessage());
        }
        return lista;
    }

    private static Partida construirPartida(Connection conn, ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        List<String> jugadores = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT usuario FROM partida_jugadores WHERE partida_id = ?")) {
            ps.setString(1, id);
            ResultSet rj = ps.executeQuery();
            while (rj.next()) jugadores.add(rj.getString("usuario"));
        }
        String estado = "ESPERANDO";
        try { estado = rs.getString("estado"); } catch (Exception ignored) {}
        return new Partida(id, rs.getString("nombre"), rs.getInt("max_jugadores"),
            rs.getBoolean("poderes"), rs.getBoolean("rapida"),
            rs.getString("dificultad"), jugadores, estado);
    }

    public static void registrarVictoria(String usuario) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "MERGE INTO ranking (usuario, victorias) KEY(usuario) " +
                "VALUES (?, COALESCE((SELECT victorias FROM ranking WHERE usuario = ?), 0) + 1)")) {
            ps.setString(1, usuario); ps.setString(2, usuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error registrando victoria: " + e.getMessage());
        }
    }

    public static List<RankingEntry> obtenerRanking() {
        List<RankingEntry> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT usuario, victorias FROM ranking ORDER BY victorias DESC")) {
            while (rs.next())
                lista.add(new RankingEntry(rs.getString("usuario"), rs.getInt("victorias")));
        } catch (SQLException e) {
            System.err.println("Error ranking: " + e.getMessage());
        }
        return lista;
    }

    private static String generarId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("MRK-");
        for (int i = 0; i < 4; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
