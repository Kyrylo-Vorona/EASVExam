package dk.easv.easvexam.dal;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.Profile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfilesDAO {
    private final ConnectionManager cm;
    public ProfilesDAO() { cm  = new ConnectionManager(); }

    public List<Profile> getAllProfiles() throws MyException {
        List<Profile> profiles = new ArrayList<>();
        try (Connection con = cm.getConnection()) {
            String sql = "SELECT * FROM Profiles";
            PreparedStatement pstmt = con.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                profiles.add(new Profile(
                        rs.getInt("id"),
                        rs.getString("profile_name"),
                        rs.getInt("rotate_degrees"),
                        rs.getInt("brightness_adjustment"),
                        rs.getBoolean("split_by_barcode")
                ));
            }
        } catch (SQLException e) {
            throw new MyException("Could not get the list of profiles", e);
        }
        return profiles;
    }

    public void addProfile(String name, int rotateDegrees, int brightness) throws MyException {
        String sql = "INSERT INTO Profiles (profile_name, rotate_degrees, brightness_adjustment, split_by_barcode) VALUES (?, ?, ?, ?)";
        try (Connection con = cm.getConnection()) {
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setInt(2, rotateDegrees);
            pstmt.setInt(3, brightness);
            pstmt.setBoolean(4, false);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new MyException("Database error: Could not add profile", e);
        }
    }

    public void editProfile(Profile profile) throws MyException {
        try (Connection con = cm.getConnection()) {
            String sql = "UPDATE Profiles SET profile_name = ?, rotate_degrees = ?, brightness_adjustment = ?, split_by_barcode = ? WHERE id = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, profile.getName());
            pstmt.setInt(2, profile.getRotateDegrees());
            pstmt.setInt(3, profile.getBrightness());
            pstmt.setBoolean(4, profile.isSplitByBarcode());
            pstmt.setInt(5, profile.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new MyException("Could not edit selected profile", e);
        }
    }

    public void deleteProfile(int id) throws MyException {
        try (Connection con = cm.getConnection()) {
            String sql = "DELETE FROM Profiles WHERE id = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }catch (SQLException e) {
            throw new MyException("Could not delete selected profile", e);
        }
    }

    public void assignUsersToProfile(int profileId, List<Integer> userIds) throws MyException {
        String deleteSql = "DELETE FROM User_Profiles WHERE profile_id = ?";
        String insertSql = "INSERT INTO User_Profiles (user_id, profile_id) VALUES (?, ?)";
        try (Connection con = cm.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement deleteStmt = con.prepareStatement(deleteSql)) {
                    deleteStmt.setInt(1, profileId);
                    deleteStmt.executeUpdate();
                }
                try (PreparedStatement insertStmt = con.prepareStatement(insertSql)) {
                    for (Integer userId : userIds) {
                        insertStmt.setInt(1, userId);
                        insertStmt.setInt(2, profileId);
                        insertStmt.addBatch(); // Добавляем в пакет
                    }
                    insertStmt.executeBatch();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new MyException("Database error: Could not assign users to profile", e);
        }
    }
}

