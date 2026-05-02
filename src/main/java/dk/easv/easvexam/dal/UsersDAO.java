package dk.easv.easvexam.dal;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public List<User> getAllUsers() throws MyException {
        List<User> users = new ArrayList<>();
        try (Connection con = cm.getConnection()) {
            String select = "SELECT * FROM Users";
            PreparedStatement pstmt = con.prepareStatement(select);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            throw new MyException("Could not get the list of movies", e);
        }
        return users;
    }
}
