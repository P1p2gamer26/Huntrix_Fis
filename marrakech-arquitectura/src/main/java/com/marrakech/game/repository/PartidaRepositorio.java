package com.marrakech.game.repository;

import com.marrakech.game.infrastructure.database.DatabaseConnection;

import java.sql.*;
import java.util.*;

/** Acceso a datos de partidas, jugadores por partida y ranking. */
public class PartidaRepositorio implements IPartidaRepositorio {

    // ── Modelos de datos internos ─────────────────────────────────────────────

    /** Datos de una sesión de partida (solo datos, sin lógica). */
    public static class Partida {
        public final String       id;
        public final String       nombre;
        public final int          maxJugadores;
        public final boolean      poderesActivados;
        public final boolean      partidaRapida;
        public final String       dificultad;
        public final List<String> jugadores;
        public final String       estado;

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

    /** Entrada del ranking (solo datos). */
    public static class RankingEntry {
        public final String usuario;
        public final int    victorias;
        public RankingEntry(String usuario, int victorias) {
            this.usuario   = usuario;
            this.victorias = victorias;
        }
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    public PartidaRepositorio() {
        inicializarTablas();
    }

    private void inicializarTablas() {
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
        } catch (SQLException e) {
            System.err.println("Error inicializando tablas de partida: " + e.getMessage());
        }
    }

    // ── Operaciones CRUD ──────────────────────────────────────────────────────

    @Override
    public String crearPartida(String nombreCreador, int maxJugadores,
                               boolean poderes, boolean rapida, String dificultad) {
        String id     = generarId();
        String nombre = nombreCreador.isEmpty() ? "Sala-" + id : nombreCreador + "'s Sala";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO partidas VALUES (?, ?, ?, ?, ?, ?, 'ESPERANDO')")) {
                ps.setString(1, id); ps.setString(2, nombre);
                ps.setInt(3, maxJugadores); ps.setBoolean(4, poderes);
                ps.setBoolean(5, rapida);   ps.setString(6, dificultad);
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

    @Override
    public boolean unirsePartida(String id, String nombreJugador) {
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
            System.err.println("Error uniéndose a partida: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void iniciarPartida(String id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE partidas SET estado = 'INICIADA' WHERE id = ?")) {
            ps.setString(1, id.toUpperCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error iniciando partida: " + e.getMessage());
        }
    }

    @Override
    public Partida obtenerPartida(String id) {
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

    @Override
    public List<Partida> listarPartidas() {
        List<Partida> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT * FROM partidas WHERE estado = 'ESPERANDO'")) {
            while (rs.next()) lista.add(construirPartida(conn, rs));
        } catch (SQLException e) {
            System.err.println("Error listando partidas: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public void registrarVictoria(String usuario) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "MERGE INTO ranking (usuario, victorias) KEY(usuario) " +
                "VALUES (?, COALESCE((SELECT victorias FROM ranking WHERE usuario = ?), 0) + 1)")) {
            ps.setString(1, usuario);
            ps.setString(2, usuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error registrando victoria: " + e.getMessage());
        }
    }

    @Override
    public void salirPartida(String id, String usuario) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM partida_jugadores WHERE partida_id = ? AND usuario = ?")) {
            ps.setString(1, id.toUpperCase());
            ps.setString(2, usuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saliendo de partida: " + e.getMessage());
        }
    }

    @Override
    public void abandonarPartida(String id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE partidas SET estado = 'ABANDONADA' WHERE id = ?")) {
            ps.setString(1, id.toUpperCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error abandonando partida: " + e.getMessage());
        }
    }

    @Override
    public List<RankingEntry> obtenerRanking() {
        List<RankingEntry> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT usuario, victorias FROM ranking ORDER BY victorias DESC")) {
            while (rs.next())
                lista.add(new RankingEntry(rs.getString("usuario"), rs.getInt("victorias")));
        } catch (SQLException e) {
            System.err.println("Error obteniendo ranking: " + e.getMessage());
        }
        return lista;
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private Partida construirPartida(Connection conn, ResultSet rs) throws SQLException {
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

    private String generarId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder("MRK-");
        for (int i = 0; i < 4; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
