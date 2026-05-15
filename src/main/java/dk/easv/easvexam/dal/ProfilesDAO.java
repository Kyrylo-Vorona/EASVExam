package dk.easv.easvexam.dal;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.Profile;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                        rs.getInt("brightness_adjustment")
                ));
            }
        } catch (SQLException e) {
            throw new MyException("Could not get the list of profiles", e);
        }
        return profiles;
    }

    public void addProfile(String name, int rotateDegrees, int brightness) throws MyException {
        String sql = "INSERT INTO Profiles (profile_name, rotate_degrees, brightness_adjustment) VALUES (?, ?, ?)";
        try (Connection con = cm.getConnection()) {
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setInt(2, rotateDegrees);
            pstmt.setInt(3, brightness);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new MyException("Database error: Could not add profile", e);
        }
    }

    public void editProfile(Profile profile) throws MyException {
        try (Connection con = cm.getConnection()) {
            String sql = "UPDATE Profiles SET profile_name = ?, rotate_degrees = ?, brightness_adjustment = ? WHERE id = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, profile.getName());
            pstmt.setInt(2, profile.getRotateDegrees());
            pstmt.setInt(3, profile.getBrightness());
            pstmt.setInt(4, profile.getId());
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

    public Map<Integer, List<String>> getUsersProfilesMap() throws MyException {
        Map<Integer, List<String>> userProfilesMap = new HashMap<>();
        String sql = "SELECT up.user_id, p.profile_name FROM User_Profiles up JOIN Profiles p ON up.profile_id = p.id";

        try (Connection con = cm.getConnection()) {
            PreparedStatement pstmt = con.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String profileName = rs.getString("profile_name");
                userProfilesMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(profileName);
            }
        } catch (SQLException e) {
            throw new MyException("Could not fetch user profiles mapping", e);
        }
        return userProfilesMap;
    }

    public List<Profile> getProfilesForUser(int userId) throws MyException {
        List<Profile> userProfiles = new ArrayList<>();
        String sql = "SELECT p.* FROM Profiles p JOIN User_Profiles up ON p.id = up.profile_id WHERE up.user_id = ?";
        try (Connection con = cm.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("profile_name");
                int rotation = rs.getInt("rotate_degrees");
                int brightness = rs.getInt("brightness_adjustment");

                Profile profile = new Profile(id, name, rotation, brightness);
                userProfiles.add(profile);
            }
        } catch (SQLException e) {
            throw new MyException("Could not fetch profiles for user ID: " + userId, e);
        }
        return userProfiles;
    }

    public List<Integer> getUserIdsForProfile(int profileId) throws MyException {
        List<Integer> userIds = new ArrayList<>();
        String sql = "SELECT user_id FROM User_Profiles WHERE profile_id = ?";

        try (Connection con = cm.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, profileId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                userIds.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            throw new MyException("Could not fetch assigned users for profile", e);
        }
        return userIds;
    }
}

