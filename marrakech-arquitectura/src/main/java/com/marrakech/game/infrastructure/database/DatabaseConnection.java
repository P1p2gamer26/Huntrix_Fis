package com.marrakech.game.infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatabaseConnection {

    private static final int    H2_PORT  = 9092;
    private static final String DB_NAME  = "marrakechdb";
    private static final String TCP_URL  =
        "jdbc:h2:tcp://localhost:" + H2_PORT + "/./" + DB_NAME + ";LOCK_TIMEOUT=10000";
    private static final String USER     = "sa";
    private static final String PASSWORD = "";

    private static org.h2.tools.Server h2Server   = null;
    private static boolean             serverReady = false;
    private static final Object        serverLock  = new Object();

    /**
     * Arranca el servidor TCP una sola vez. Llamar desde initDatabase() al inicio de la app.
     */
    public static void initServer() {
        synchronized (serverLock) {
            if (serverReady) return;
            try {
                h2Server = org.h2.tools.Server.createTcpServer(
                    "-tcp", "-tcpPort", String.valueOf(H2_PORT),
                    "-tcpAllowOthers", "-ifNotExists"
                ).start();
                System.out.println("[H2] Servidor TCP iniciado en puerto " + H2_PORT);
            } catch (Exception e) {
                // Puerto ocupado → otra instancia ya lo levantó
                System.out.println("[H2] Servidor ya activo, conectando como cliente.");
            }
            serverReady = true;
        }
    }

    /**
     * Devuelve una conexión NUEVA por cada llamada.
     * Así cada hilo (polling, escritura) tiene su propia conexión sin conflictos.
     * El servidor TCP de H2 maneja múltiples conexiones simultáneas sin problemas.
     */
    public static Connection getConnection() throws SQLException {
        if (!serverReady) initServer();
        return DriverManager.getConnection(TCP_URL, USER, PASSWORD);
    }

    public static void initDatabase() {
        initServer();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = new String(Files.readAllBytes(Paths.get("src/main/resources/database.sql")));
            stmt.execute(sql);
            System.out.println("La base de datos se ejecuto e inicializo correctamente");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
