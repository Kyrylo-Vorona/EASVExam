package dk.easv.easvexam.be;

import java.sql.Timestamp;

public class ActivityLog {
    private int id;
    private String username;
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
    public String getTimestamp() {
        if (timestamp != null) {
            String timeStr = timestamp.toString();
            if (timeStr.endsWith(".0")) {
                return timeStr.substring(0, timeStr.length() - 2);
            }
            return timeStr;
        }
        return null;
    }
}
