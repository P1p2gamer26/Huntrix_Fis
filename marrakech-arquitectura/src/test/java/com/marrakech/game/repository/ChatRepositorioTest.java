package com.marrakech.game.repository;

import com.marrakech.game.infrastructure.database.DatabaseConnection;
import com.marrakech.game.repository.ChatRepositorio.Mensaje;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChatRepositorioTest {

    private static ChatRepositorio repo;

    @BeforeAll
    static void setUpAll() throws Exception {
        DatabaseConnection.initServer();
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement()) {
            s.execute("DROP TABLE IF EXISTS chat_mensajes");
        }
        repo = new ChatRepositorio();
        repo.inicializarTabla();
    }

    @BeforeEach
    void cleanTable() throws Exception {
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement()) {
            s.execute("DELETE FROM chat_mensajes");
            s.execute("ALTER TABLE chat_mensajes ALTER COLUMN id RESTART WITH 1");
        }
    }

    @Test @Order(1)
    void inicializarTabla_creaTabla_permiteInsertar() {
        repo.enviarMensaje("MRK-TEST", "user1", "hola");
        List<Mensaje> msgs = repo.obtenerMensajes("MRK-TEST", 0);
        assertEquals(1, msgs.size());
    }

    @Test @Order(2)
    void enviarMensaje_y_obtener_recuperaTexto() {
        repo.enviarMensaje("MRK-1", "ana", "Hola mundo");
        List<Mensaje> msgs = repo.obtenerMensajes("MRK-1", 0);
        assertEquals(1, msgs.size());
        assertEquals("ana", msgs.get(0).usuario);
        assertEquals("Hola mundo", msgs.get(0).texto);
    }

    @Test @Order(3)
    void obtenerMensajes_desdeId_filtraCorrectamente() {
        repo.enviarMensaje("MRK-2", "a", "msg1");
        repo.enviarMensaje("MRK-2", "b", "msg2");
        int ultimoId = repo.obtenerUltimoId("MRK-2");
        List<Mensaje> msgs = repo.obtenerMensajes("MRK-2", ultimoId);
        assertTrue(msgs.isEmpty());
        msgs = repo.obtenerMensajes("MRK-2", 0);
        assertEquals(2, msgs.size());
    }

    @Test @Order(4)
    void obtenerUltimoId_sinMensajes_retornaCero() {
        assertEquals(0, repo.obtenerUltimoId("MRK-VACIO"));
    }

    @Test @Order(5)
    void obtenerUltimoId_conMensajes_retornaId() {
        repo.enviarMensaje("MRK-LAST", "x", "test");
        int id = repo.obtenerUltimoId("MRK-LAST");
        assertTrue(id > 0);
    }

    @Test @Order(6)
    void enviarMensaje_textoLargo_trunca() {
        String largo = "a".repeat(600);
        repo.enviarMensaje("MRK-TRUNC", "u", largo);
        List<Mensaje> msgs = repo.obtenerMensajes("MRK-TRUNC", 0);
        assertEquals(1, msgs.size());
        assertEquals(500, msgs.get(0).texto.length());
    }

    @Test @Order(7)
    void obtenerMensajes_partidaDistinta_noSeMezclan() {
        repo.enviarMensaje("PARTIDA-A", "u1", "msgA");
        repo.enviarMensaje("PARTIDA-B", "u2", "msgB");
        assertEquals(1, repo.obtenerMensajes("PARTIDA-A", 0).size());
        assertEquals(1, repo.obtenerMensajes("PARTIDA-B", 0).size());
    }

    @Test @Order(8)
    void obtenerUltimoId_idEmpiezaEnUno() {
        repo.enviarMensaje("MRK-ID1", "u", "first");
        assertEquals(1, repo.obtenerUltimoId("MRK-ID1"));
    }
}
