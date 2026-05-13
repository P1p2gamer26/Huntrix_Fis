package com.marrakech.game.infrastructure.database;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseConnectionTest {

    @BeforeAll
    static void setUp() {
        DatabaseConnection.initServer();
    }

    @Test @Order(1)
    void initServer_noLanzaExcepcion() {
        assertDoesNotThrow(DatabaseConnection::initServer);
    }

    @Test @Order(2)
    void initServer_esIdempotente() {
        assertDoesNotThrow(() -> {
            DatabaseConnection.initServer();
            DatabaseConnection.initServer();
            DatabaseConnection.initServer();
        });
    }

    @Test @Order(3)
    void getConnection_devuelveConexionValida() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            assertFalse(conn.isClosed());
        }
    }

    @Test @Order(4)
    void getConnection_permiteCrearTablasYEjecutarSQL() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS test_table (id INT PRIMARY KEY, name VARCHAR(50))");
            st.execute("INSERT INTO test_table VALUES (1, 'hello')");
            var rs = st.executeQuery("SELECT name FROM test_table WHERE id = 1");
            assertTrue(rs.next());
            assertEquals("hello", rs.getString("name"));
            st.execute("DROP TABLE test_table");
        }
    }

    @Test @Order(5)
    void getConnection_multiplesLlamadas_devuelveConexionesDistintas() throws Exception {
        Connection c1 = DatabaseConnection.getConnection();
        Connection c2 = DatabaseConnection.getConnection();
        assertNotSame(c1, c2);
        assertFalse(c1.isClosed());
        assertFalse(c2.isClosed());
        c1.close();
        c2.close();
    }
}
