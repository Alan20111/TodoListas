package dev.alan20111.todolist.utils;

public class SessionManager {
    private static SessionManager instance;
    private String currentUsername;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public void logout() {
        this.currentUsername = null;
    }
}