package dk.easv.easvexam.dal;

public class DALManager {
    private static DALManager instance;
    private UsersDAO usersDAO;

    public static DALManager getInstance() {
        if (instance == null) {
            instance = new DALManager();
        }
        return instance;
    }

    private final ConnectionManager cm;

    private DALManager() {
        cm = new ConnectionManager();
    }

    public UsersDAO getUsersDAO() {
        if (usersDAO == null) usersDAO = new UsersDAO();
        return usersDAO;
    }
}
