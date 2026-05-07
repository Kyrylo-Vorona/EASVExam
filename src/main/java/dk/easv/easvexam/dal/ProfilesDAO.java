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

    public void addProfile(int id, String name, int rotateDegrees, int brightness) throws MyException {
        try (Connection con = cm.getConnection()) {
            String add = "INSERT INTO Profiles (profile_name, rotate_degrees, brightness_adjustment, split_by_barcode) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(add);
            pstmt.setInt(1, id);
            pstmt.setString(2, name);
            pstmt.setInt(3, rotateDegrees);
            pstmt.setInt(4, brightness);
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
}

