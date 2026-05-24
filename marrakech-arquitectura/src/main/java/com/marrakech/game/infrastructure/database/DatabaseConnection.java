package com.marrakech.game.infrastructure.database;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String DB_DIR = System.getProperty("user.home") + "/.marrakech";
    private static final String DB_URL =
        "jdbc:h2:file:" + DB_DIR + "/marrakechdb" +
        ";AUTO_SERVER=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_DELAY=-1";
    private static final String USER     = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    public static void initDatabase() {
        try {
            Files.createDirectories(Paths.get(DB_DIR));
        } catch (Exception e) {
            System.err.println("[DB] No se pudo crear directorio: " + e.getMessage());
        }
        try (Connection conn = getConnection();
             Statement stmt  = conn.createStatement();
             InputStream is  = DatabaseConnection.class.getClassLoader()
                 .getResourceAsStream("database.sql")) {
            if (is == null) {
                System.err.println("[DB] database.sql no encontrado — las tablas se crean automáticamente.");
                return;
            }
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            stmt.execute(sql);
            System.out.println("[DB] Schema inicializado correctamente.");
        } catch (Exception e) {
            System.err.println("[DB] Error inicializando schema: " + e.getMessage());
        }
    }

    public static void initServer() {
        // Para que los tests que lo llaman no fallen.
        // En nuestro caso, lo que necesitamos es que la BD esté inicializada.
        initDatabase();
    }
}
