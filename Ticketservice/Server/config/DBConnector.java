package Ticketservice.Server.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {

    public static Connection getConnection() throws SQLException {
        String dbURL = "jdbc:mariadb://localhost:3306/test";
        String user = "root";
        String password = "";

        return DriverManager.getConnection(dbURL, user, password);
    }
}