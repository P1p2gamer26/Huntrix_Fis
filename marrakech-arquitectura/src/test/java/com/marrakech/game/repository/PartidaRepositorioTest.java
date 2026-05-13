package com.marrakech.game.repository;

import com.marrakech.game.infrastructure.database.DatabaseConnection;
import com.marrakech.game.repository.PartidaRepositorio.Partida;
import com.marrakech.game.repository.PartidaRepositorio.RankingEntry;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PartidaRepositorioTest {

    private static PartidaRepositorio repo;

    @BeforeAll
    static void setUpAll() throws Exception {
        DatabaseConnection.initServer();
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement()) {
            s.execute("DROP TABLE IF EXISTS partida_jugadores");
            s.execute("DROP TABLE IF EXISTS partidas");
            s.execute("DROP TABLE IF EXISTS ranking");
            s.execute("CREATE TABLE IF NOT EXISTS partidas (" +
                "id VARCHAR(20) PRIMARY KEY," +
                "nombre VARCHAR(100)," +
                "max_jugadores INT," +
                "poderes BOOLEAN," +
                "rapida BOOLEAN," +
                "dificultad VARCHAR(20)," +
                "estado VARCHAR(20) DEFAULT 'ESPERANDO')");
            s.execute("CREATE TABLE IF NOT EXISTS partida_jugadores (" +
                "partida_id VARCHAR(20)," +
                "usuario VARCHAR(100)," +
                "PRIMARY KEY (partida_id, usuario))");
            s.execute("CREATE TABLE IF NOT EXISTS ranking (" +
                "usuario VARCHAR(100) PRIMARY KEY," +
                "victorias INT DEFAULT 0)");
        }
        repo = new PartidaRepositorio();
    }

    @BeforeEach
    void cleanUp() throws Exception {
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement()) {
            s.execute("DELETE FROM partida_jugadores");
            s.execute("DELETE FROM partidas");
            s.execute("DELETE FROM ranking");
        }
    }

    @Test @Order(1)
    void crearPartida_retornaIdValido() {
        String id = repo.crearPartida("host1", 4, false, false, "normal");
        assertNotNull(id);
        assertTrue(id.startsWith("MRK-"));
        assertEquals(8, id.length());
    }

    @Test @Order(2)
    void obtenerPartida_devuelvePartidaCreada() {
        String id = repo.crearPartida("owner", 2, true, true, "rapida");
        Partida p = repo.obtenerPartida(id);
        assertNotNull(p);
        assertEquals(2, p.maxJugadores);
        assertTrue(p.poderesActivados);
        assertTrue(p.partidaRapida);
        assertEquals("rapida", p.dificultad);
    }

    @Test @Order(3)
    void obtenerPartida_idInexistente_retornaNull() {
        assertNull(repo.obtenerPartida("MRK-ZZZZ"));
    }

    @Test @Order(4)
    void unirsePartida_agregaJugador() {
        String id = repo.crearPartida("host2", 4, false, false, "normal");
        assertTrue(repo.unirsePartida(id, "player1"));
        Partida p = repo.obtenerPartida(id);
        assertEquals(2, p.jugadores.size());
        assertTrue(p.jugadores.contains("player1"));
    }

    @Test @Order(5)
    void unirsePartida_salaLlena_retornaFalse() {
        String id = repo.crearPartida("host3", 2, false, false, "normal");
        repo.unirsePartida(id, "player1");
        assertFalse(repo.unirsePartida(id, "player2"));
    }

    @Test @Order(6)
    void unirsePartida_idInexistente_retornaFalse() {
        assertFalse(repo.unirsePartida("MRK-INVALID", "player"));
    }

    @Test @Order(7)
    void unirsePartida_jugadorYaEnSala_retornaTrue() {
        String id = repo.crearPartida("host4", 4, false, false, "normal");
        assertTrue(repo.unirsePartida(id, "host4"));
    }

    @Test @Order(8)
    void iniciarPartida_cambiaEstado() {
        String id = repo.crearPartida("host5", 2, false, false, "normal");
        repo.iniciarPartida(id);
        Partida p = repo.obtenerPartida(id);
        assertEquals("INICIADA", p.estado);
    }

    @Test @Order(9)
    void listarPartidas_soloRetornaEsperando() {
        String id1 = repo.crearPartida("a", 2, false, false, "nor");
        String id2 = repo.crearPartida("b", 2, false, false, "nor");
        repo.iniciarPartida(id2);
        List<Partida> list = repo.listarPartidas();
        for (Partida p : list) assertEquals("ESPERANDO", p.estado);
    }

    @Test @Order(10)
    void registrarVictoria_creaEntry() {
        repo.registrarVictoria("campeon");
        List<RankingEntry> rank = repo.obtenerRanking();
        assertTrue(rank.stream().anyMatch(e -> e.usuario.equals("campeon")));
    }

    @Test @Order(11)
    void registrarVictoria_incrementaContador() {
        repo.registrarVictoria("repeater");
        repo.registrarVictoria("repeater");
        List<RankingEntry> rank = repo.obtenerRanking();
        RankingEntry e = rank.stream().filter(r -> r.usuario.equals("repeater")).findFirst().orElse(null);
        assertNotNull(e);
        assertEquals(2, e.victorias);
    }

    @Test @Order(12)
    void obtenerRanking_ordenDescendente() {
        repo.registrarVictoria("segundo");
        repo.registrarVictoria("segundo");
        repo.registrarVictoria("primero");
        repo.registrarVictoria("primero");
        repo.registrarVictoria("primero");
        List<RankingEntry> rank = repo.obtenerRanking();
        assertEquals("primero", rank.get(0).usuario);
        assertEquals("segundo", rank.get(1).usuario);
    }

    @Test @Order(13)
    void resumen_formatoCorrecto() {
        String id = repo.crearPartida("host", 4, false, false, "nor");
        Partida p = repo.obtenerPartida(id);
        String resumen = p.resumen();
        assertTrue(resumen.contains("(1/4)"));
        assertTrue(resumen.contains(id));
    }
}
