package dk.easv.easvexam.dal;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersDAO {
    private final ConnectionManager cm;
    public UsersDAO() { cm  = new ConnectionManager(); }

    public User login(String username, String password) throws MyException {
        String sql = "SELECT * FROM Users WHERE username = ? AND password_hash = ?";

        try (Connection con = cm.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String email = rs.getString("email");
                String role = rs.getString("role");
                return new User(id, username, role, email);
            }
        } catch (SQLException e) {
            throw new MyException("Database error: Could not log in", e);
        }
        return null;
    }
}
