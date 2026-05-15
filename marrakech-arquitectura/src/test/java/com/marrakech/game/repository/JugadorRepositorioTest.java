package com.marrakech.game.repository;

import com.marrakech.game.infrastructure.database.DatabaseConnection;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JugadorRepositorioTest {

    private static JugadorRepositorio repo;

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
                "correo VARCHAR(100) UNIQUE NOT NULL," +
                "password VARCHAR(255) NOT NULL," +
                "fecha_registro TIMESTAMP NOT NULL," +
                "estado VARCHAR(15)," +
                "foto BLOB," +
                "sesion_activa BOOLEAN DEFAULT FALSE)");
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
        repo = new JugadorRepositorio(new EstadisticasRepositorio());
    }

    @BeforeEach
    void cleanUp() throws Exception {
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement()) {
            s.execute("DELETE FROM Estadisticas");
            s.execute("DELETE FROM Jugador");
        }
    }

    @Test @Order(1)
    void crearJugador_insertaYRetornaIdValido() {
        repo.crearJugador("testUser", "test@test.com", "pass123");
        int id = repo.obtenerIdJugador("testUser");
        assertTrue(id > 0);
    }

    @Test @Order(2)
    void nombreExiste_devuelveTrueSiExiste() {
        repo.crearJugador("existente", "existe@test.com", "pass");
        assertTrue(repo.nombreExiste("existente"));
        assertFalse(repo.nombreExiste("noExiste"));
    }

    @Test @Order(3)
    void correoExiste_devuelveTrueSiExiste() {
        repo.crearJugador("mailUser", "mail@test.com", "pass");
        assertTrue(repo.correoExiste("mail@test.com"));
        assertFalse(repo.correoExiste("otro@test.com"));
    }

    @Test @Order(4)
    void loginJugador_credencialesCorrectas_retornaNombre() {
        repo.crearJugador("loginUser", "login@test.com", "secreta");
        String res = repo.loginJugador("loginUser", "secreta");
        assertEquals("loginUser", res);
    }

    @Test @Order(5)
    void loginJugador_passwordIncorrecto_retornaNull() {
        repo.crearJugador("secureUser", "secure@test.com", "correcta");
        assertNull(repo.loginJugador("secureUser", "incorrecta"));
    }

    @Test @Order(6)
    void loginJugador_usuarioNoExiste_retornaNull() {
        assertNull(repo.loginJugador("noExiste", "pass"));
    }

    @Test @Order(7)
    void cerrarSesion_marcaSesionInactiva() {
        repo.crearJugador("sessionUser", "session@test.com", "pass");
        repo.loginJugador("sessionUser", "pass");
        repo.cerrarSesion("sessionUser");
        assertDoesNotThrow(() -> repo.cerrarSesion("sessionUser"));
    }

    @Test @Order(8)
    void cerrarSesion_conNull_noLanzaExcepcion() {
        assertDoesNotThrow(() -> repo.cerrarSesion(null));
    }

    @Test @Order(9)
    void obtenerIdJugador_existente_retornaId() {
        repo.crearJugador("idUser", "id@test.com", "pass");
        int id = repo.obtenerIdJugador("idUser");
        assertTrue(id > 0);
    }

    @Test @Order(10)
    void obtenerIdJugador_noExiste_retornaMenosUno() {
        assertEquals(-1, repo.obtenerIdJugador("fakeUser"));
    }

    @Test @Order(11)
    void getCorreo_devuelveCorreo() {
        repo.crearJugador("correoUser", "correo@test.com", "pass");
        assertEquals("correo@test.com", repo.getCorreo("correoUser"));
    }

    @Test @Order(12)
    void getCorreo_noExiste_retornaVacio() {
        assertEquals("", repo.getCorreo("noExiste"));
    }

    @Test @Order(13)
    void getFechaRegistro_devuelveFecha() {
        repo.crearJugador("fechaUser", "fecha@test.com", "pass");
        String fecha = repo.getFechaRegistro("fechaUser");
        assertNotNull(fecha);
        assertFalse(fecha.isEmpty());
    }

    @Test @Order(14)
    void getFechaRegistro_noExiste_retornaRaya() {
        assertEquals("\u2014", repo.getFechaRegistro("fakeUser"));
    }
}
