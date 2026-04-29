package dk.easv.easvexam.bll;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.User;
import dk.easv.easvexam.dal.DALManager;

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
}
