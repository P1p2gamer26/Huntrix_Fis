package com.marrakech.game.repository;

import com.marrakech.game.infrastructure.database.DatabaseConnection;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EstadisticasRepositorioTest {

    private static EstadisticasRepositorio repo;
    private static int idJugador;

    @BeforeAll
    static void setUpAll() throws Exception {
        DatabaseConnection.initServer();
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement()) {
            s.execute("SET REFERENTIAL_INTEGRITY FALSE");
            s.execute("DROP TABLE IF EXISTS Estadisticas");
            s.execute("DROP TABLE IF EXISTS Jugador");
            s.execute("SET REFERENTIAL_INTEGRITY TRUE");
            s.execute("CREATE TABLE IF NOT EXISTS Jugador (" +
                "id_jugador INT AUTO_INCREMENT PRIMARY KEY," +
                "nombre_usuario VARCHAR(50) UNIQUE NOT NULL," +
                "correo VARCHAR(100)," +
                "password VARCHAR(255))");
            s.execute("CREATE TABLE IF NOT EXISTS Estadisticas (" +
                "id_estadistica INT AUTO_INCREMENT PRIMARY KEY," +
                "partidas_jugadas INT DEFAULT 0," +
                "partidas_ganadas INT DEFAULT 0," +
                "partidas_perdidas INT DEFAULT 0," +
                "total_monedas INT DEFAULT 0," +
                "ultima_actualizacion TIMESTAMP," +
                "id_jugador INT UNIQUE," +
                "FOREIGN KEY (id_jugador) REFERENCES Jugador(id_jugador))");
        }
        repo = new EstadisticasRepositorio();
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement()) {
            s.execute("INSERT INTO Jugador (nombre_usuario) VALUES ('statsUser')");
            var rs = s.executeQuery("SELECT id_jugador FROM Jugador WHERE nombre_usuario = 'statsUser'");
            rs.next(); idJugador = rs.getInt(1);
        }
    }

    @BeforeEach
    void cleanUp() throws Exception {
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement()) {
            s.execute("DELETE FROM Estadisticas");
        }
    }

    @Test @Order(1)
    void inicializarEstadisticas_creaRegistro() {
        repo.inicializarEstadisticas(idJugador);
        int[] stats = repo.obtenerEstadisticas(idJugador);
        assertNotNull(stats);
        assertEquals(4, stats.length);
        assertEquals(0, stats[0]); // partidas_jugadas
        assertEquals(0, stats[1]); // partidas_ganadas
        assertEquals(0, stats[2]); // partidas_perdidas
        assertEquals(0, stats[3]); // total_monedas
    }

    @Test @Order(2)
    void actualizarEstadisticas_victoria_incrementaGanadas() {
        repo.inicializarEstadisticas(idJugador);
        repo.actualizarEstadisticas(idJugador, true, 15);
        int[] stats = repo.obtenerEstadisticas(idJugador);
        assertEquals(1, stats[0]); // partidas_jugadas
        assertEquals(1, stats[1]); // partidas_ganadas
        assertEquals(0, stats[2]); // partidas_perdidas
        assertEquals(15, stats[3]); // total_monedas
    }

    @Test @Order(3)
    void actualizarEstadisticas_derrota_incrementaPerdidas() {
        repo.inicializarEstadisticas(idJugador);
        repo.actualizarEstadisticas(idJugador, false, 5);
        int[] stats = repo.obtenerEstadisticas(idJugador);
        assertEquals(1, stats[0]); // partidas_jugadas
        assertEquals(0, stats[1]); // partidas_ganadas
        assertEquals(1, stats[2]); // partidas_perdidas
        assertEquals(5, stats[3]); // total_monedas
    }

    @Test @Order(4)
    void actualizarEstadisticas_multiples_acumulaCorrectamente() {
        repo.inicializarEstadisticas(idJugador);
        repo.actualizarEstadisticas(idJugador, true, 10);
        repo.actualizarEstadisticas(idJugador, false, 8);
        repo.actualizarEstadisticas(idJugador, true, 12);
        int[] stats = repo.obtenerEstadisticas(idJugador);
        assertEquals(3, stats[0]); // partidas_jugadas
        assertEquals(2, stats[1]); // partidas_ganadas
        assertEquals(1, stats[2]); // partidas_perdidas
        assertEquals(30, stats[3]); // total_monedas
    }

    @Test @Order(5)
    void obtenerEstadisticas_sinRegistro_retornaCeros() {
        int[] stats = repo.obtenerEstadisticas(9999);
        assertArrayEquals(new int[]{0, 0, 0, 0}, stats);
    }
}
