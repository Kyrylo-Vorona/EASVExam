package dk.easv.easvexam.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private final String url = "jdbc:sqlserver://10.176.111.34:1433;databaseName=EASVExam;user=CS2025b_e_18;password=CS2025bE18#23;encrypt=true;trustServerCertificate=true;";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }
}
