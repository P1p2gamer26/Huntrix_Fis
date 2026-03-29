package com.marrakech.game.infrastructure;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PartidaRepository {

    public static class Partida {
        public final String id;
        public final String nombre;
        public final int maxJugadores;
        public final boolean poderesActivados;
        public final boolean partidaRapida;
        public final String dificultad;
        public final List<String> jugadores;

        public Partida(String id, String nombre, int maxJugadores,
                       boolean poderesActivados, boolean partidaRapida, String dificultad) {
            this.id               = id;
            this.nombre           = nombre;
            this.maxJugadores     = maxJugadores;
            this.poderesActivados = poderesActivados;
            this.partidaRapida    = partidaRapida;
            this.dificultad       = dificultad;
            this.jugadores        = new ArrayList<>();
        }

        public String resumen() {
            return nombre + " (" + jugadores.size() + "/" + maxJugadores + " jugadores) | Código: " + id;
        }
    }

    public static class RankingEntry {
        public final String usuario;
        public int victorias;
        public RankingEntry(String usuario, int victorias) {
            this.usuario  = usuario;
            this.victorias = victorias;
        }
    }

    private static final Map<String, Partida>      partidas = new ConcurrentHashMap<>();
    private static final Map<String, RankingEntry> ranking  = new ConcurrentHashMap<>();

    static {
        ranking.put("cap_jules",    new RankingEntry("cap_jules",    0));
        ranking.put("danirep",      new RankingEntry("danirep",      0));
        ranking.put("luna_leo",     new RankingEntry("luna_leo",     0));
    }

    public static String crearPartida(String nombreCreador, int maxJugadores,
                                      boolean poderes, boolean rapida, String dificultad) {
        String id = generarId();
        String nombre = nombreCreador.isEmpty() ? "Sala-" + id : nombreCreador + "'s Sala";
        Partida p = new Partida(id, nombre, maxJugadores, poderes, rapida, dificultad);
        p.jugadores.add(nombreCreador.isEmpty() ? "Jugador1" : nombreCreador);
        partidas.put(id, p);
        return id;
    }

    public static boolean unirsePartida(String id, String nombreJugador) {
        Partida p = partidas.get(id.toUpperCase());
        if (p == null) return false;
        if (p.jugadores.size() >= p.maxJugadores) return false;
        if (!p.jugadores.contains(nombreJugador)) p.jugadores.add(nombreJugador);
        return true;
    }

    public static Partida obtenerPartida(String id) {
        return partidas.get(id.toUpperCase());
    }

    public static List<Partida> listarPartidas() {
        return new ArrayList<>(partidas.values());
    }

    public static void registrarVictoria(String usuario) {
        ranking.computeIfAbsent(usuario, u -> new RankingEntry(u, 0)).victorias++;
    }

    public static List<RankingEntry> obtenerRanking() {
        List<RankingEntry> lista = new ArrayList<>(ranking.values());
        lista.sort((a, b) -> Integer.compare(b.victorias, a.victorias));
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