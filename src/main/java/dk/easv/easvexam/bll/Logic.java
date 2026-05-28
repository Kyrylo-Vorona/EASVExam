package dk.easv.easvexam.bll;

import dk.easv.easvexam.be.ActivityLog;
import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.Profile;
import dk.easv.easvexam.be.User;
import dk.easv.easvexam.dal.DALManager;
import dk.easv.easvexam.gui.OpenView;

import java.util.List;
import java.util.Map;

public class Logic {
    private static Logic instance;
    private Logic() {}

    public static Logic getInstance() {
        if (instance == null) {
            instance = new Logic();
        }
        return instance;
    }

    public User login(String username, String password) throws MyException {
        if (username.isEmpty() || password.isEmpty()) {
            return null;
        }
        return DALManager.getInstance().getUsersDAO().login(username, password);
    }

    public void deleteUser(User user) throws MyException {
        DALManager.getInstance().getUsersDAO().deleteUser(user);
    }

    public void addProfile(String name, int rotateDegrees, int brightness) throws MyException {
        DALManager.getInstance().getProfilesDAO().addProfile(name, rotateDegrees, brightness);
    }

    public void editProfile(Profile profile) throws MyException {
        if (profile == null) {
            OpenView.showErrorAlert("Please select a profile to save.");
            return;
        }
        DALManager.getInstance().getProfilesDAO().editProfile(profile);
    }

    public void deleteProfile(Profile profile) throws MyException {
        DALManager.getInstance().getProfilesDAO().deleteProfile(profile.getId());
    }

    public void assignUsersToProfile(int profileId, List<Integer> userIds) throws MyException {
        DALManager.getInstance().getProfilesDAO().assignUsersToProfile(profileId, userIds);
    }

    public void saveDocumentToDb(String boxId, String client, String caseName, int profileId, int userId, String status, List<String> filePaths) throws MyException {
        DALManager.getInstance().getDocumentsDAO().saveDocumentToDb(boxId, client, caseName, profileId, userId, status, filePaths);
    }

    public void logActivity(int userId, String action) throws MyException {
        DALManager.getInstance().getDocumentsDAO().logActivity(userId, action);
    }

    public void addUserWithProfiles(String username, String password, String role, List<Integer> profileIds) throws MyException {
        int newUserId = DALManager.getInstance().getUsersDAO().addUser(username, password, role);
        if (!profileIds.isEmpty()) {
            DALManager.getInstance().getUsersDAO().saveUserProfiles(newUserId, profileIds);
        }
    }

    public void editUserWithProfiles(User user, String password, List<Integer> profileIds) throws MyException {
        DALManager.getInstance().getUsersDAO().editUser(user, password);
        DALManager.getInstance().getUsersDAO().deleteUserProfiles(user.getId());
        if (!profileIds.isEmpty()) {
            DALManager.getInstance().getUsersDAO().saveUserProfiles(user.getId(), profileIds);
        }
    }

    public List<Integer> getProfileIdsForUser(int userId) throws MyException {
        return DALManager.getInstance().getUsersDAO().getProfileIdsByUserId(userId);
    }

    public List<User> getAllUsers() throws MyException {
        return DALManager.getInstance().getUsersDAO().getAllUsers();
    }

    public List<Profile> getAllProfiles() throws MyException {
        return DALManager.getInstance().getProfilesDAO().getAllProfiles();
    }

    public Map<Integer, List<String>> getUsersProfilesMap() throws MyException {
        return DALManager.getInstance().getProfilesDAO().getUsersProfilesMap();
    }

    public List<Profile> getProfilesForUser(int userId) throws MyException {
        return DALManager.getInstance().getProfilesDAO().getProfilesForUser(userId);
    }

    public List<Integer> getUserIdsForProfile(int profileId) throws MyException {
        return DALManager.getInstance().getProfilesDAO().getUserIdsForProfile(profileId);
    }

    public List<ActivityLog> getAllLogs() throws MyException {
        return DALManager.getInstance().getDocumentsDAO().getAllLogs();
    }
}
