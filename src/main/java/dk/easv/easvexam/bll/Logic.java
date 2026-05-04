package dk.easv.easvexam.bll;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.User;
import dk.easv.easvexam.dal.DALManager;

import java.util.List;

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

    public void addUser(String username, String password, String role) throws MyException {
        if (username.isEmpty() || password.isEmpty() || role.isEmpty()) {
            return;
        }
        DALManager.getInstance().getUsersDAO().addUser(username, password, role);
    }

    public void editUser(User user, String newPassword) throws MyException {
        DALManager.getInstance().getUsersDAO().editUser(user, newPassword);
    }

    public void deleteUser(User user) throws MyException {
        DALManager.getInstance().getUsersDAO().deleteUser(user);
    }

    public List<User> getAllUsers() throws MyException {
        return DALManager.getInstance().getUsersDAO().getAllUsers();
    }
}
