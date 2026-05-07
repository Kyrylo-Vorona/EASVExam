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
                String role = rs.getString("role");
                return new User(id, username, role);
            }
        } catch (SQLException e) {
            throw new MyException("Database error: Could not log in", e);
        }
        return null;
    }

    public void addUser(String username, String password, String role) throws MyException {
        try (Connection con = cm.getConnection()) {
            String add = "INSERT INTO Users (username, password_hash, role) VALUES (?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(add);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new MyException("Database error: Could not add user", e);
        }
    }

    public void editUser(User user, String newPassword) throws MyException {
        String sql;
        if (newPassword == null || newPassword.isEmpty()) {
            sql = "UPDATE Users SET username = ?, role = ? WHERE id = ?";
        } else {
            sql = "UPDATE Users SET username = ?, role = ?, password_hash = ? WHERE id = ?";
        }
        try (Connection con = cm.getConnection()) {
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getRole());
            if (newPassword == null || newPassword.isEmpty()) {
                pstmt.setInt(3, user.getId());
            } else {
                pstmt.setString(3, newPassword);
                pstmt.setInt(4, user.getId());
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new MyException("Error updating user", e);
        }
    }

    public void deleteUser(User user) throws MyException {
        //String deleteLinks = "DELETE FROM EventCoordinators WHERE user_id = ?";
        String deleteUser = "DELETE FROM Users WHERE id = ?";
        try (Connection con = cm.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement psUser = con.prepareStatement(deleteUser)) {
                psUser.setInt(1, user.getId());
                psUser.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new MyException("Database error: Could not delete user and their assignments", e);
        }
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
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            throw new MyException("Could not get the list of users", e);
        }
        return users;
    }
}
