package com.marrakech.game.infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Statement;

public class DatabaseConnection{

    private static final String URL = "jdbc:h2:./marrakechdb";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initDatabase(){
        try(Connection conn  =getConnection();
        Statement stmt = conn.createStatement()){

            String sql = new String(Files.readAllBytes(Paths.get("src/main/resources/database.sql")));

            stmt.execute(sql);

            System.out.println("La base de datos se ejecuto e inicializo correctamente");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}