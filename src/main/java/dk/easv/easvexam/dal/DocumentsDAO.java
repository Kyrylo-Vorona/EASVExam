package dk.easv.easvexam.dal;

import dk.easv.easvexam.be.ActivityLog;
import dk.easv.easvexam.be.MyException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentsDAO {
    private final ConnectionManager cm;
    public DocumentsDAO() { cm  = new ConnectionManager(); }

    public void saveDocumentToDb(String boxId, String client, String caseName, int profileId, int userId, String status, List<String> filePaths) throws MyException {
        String insertDocSql = "INSERT INTO Documents (box_id, client, [case], profile_id, created_by, status, created_at) VALUES (?, ?, ?, ?, ?, ?, CONVERT(varchar(19), CURRENT_TIMESTAMP, 120))";
        String insertFileSql = "INSERT INTO Files (document_id, file_path) VALUES (?, ?)";

        try (Connection con = cm.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement stmtDoc = con.prepareStatement(insertDocSql, Statement.RETURN_GENERATED_KEYS)) {
                stmtDoc.setString(1, boxId);
                stmtDoc.setString(2, client);
                stmtDoc.setString(3, caseName);
                stmtDoc.setInt(4, profileId);
                stmtDoc.setInt(5, userId);
                stmtDoc.setString(6, status);
                stmtDoc.executeUpdate();

                try (ResultSet generatedKeys = stmtDoc.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int documentId = generatedKeys.getInt(1);

                        try (PreparedStatement stmtFile = con.prepareStatement(insertFileSql)) {
                            for (String path : filePaths) {
                                stmtFile.setInt(1, documentId);
                                stmtFile.setString(2, path);
                                stmtFile.addBatch();
                            }
                            stmtFile.executeBatch();
                        }
                    }
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw new MyException("Database error during export: ", e);
            }
        } catch (SQLException e) {
            throw new MyException("Database connection error: ", e);
        }
    }

    public void logActivity(int userId, String action) throws MyException {
        String sql = "INSERT INTO ActivityLogs (user_id, action, timestamp) VALUES (?, ?, CONVERT(varchar(19), CURRENT_TIMESTAMP, 120))";
        try (Connection con = cm.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, action);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new MyException("Failed to save log to DB: ", e);
        }
    }

    public List<ActivityLog> getAllLogs() throws MyException {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT l.id, u.username, l.action, l.timestamp FROM ActivityLogs l JOIN Users u ON l.user_id = u.id ORDER BY l.timestamp DESC";

        try (Connection con = cm.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ActivityLog log = new ActivityLog(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getTimestamp("timestamp")
                );
                logs.add(log);
            }
        } catch (SQLException e) {
            throw new MyException("Failed to load logs: ", e);
        }
        return logs;
    }
}
