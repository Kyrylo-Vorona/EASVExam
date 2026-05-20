package dk.easv.easvexam.be;

import java.sql.Timestamp;

public class ActivityLog {
    private int id;
    private String username; // Вместо ID храним имя
    private String action;
    private Timestamp timestamp;

    public ActivityLog(int id, String username, String action, Timestamp timestamp) {
        this.id = id;
        this.username = username;
        this.action = action;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getAction() { return action; }
    public Timestamp getTimestamp() { return timestamp; }
}
