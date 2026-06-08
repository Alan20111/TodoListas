package dev.alan20111.todolist.service;

import dev.alan20111.todolist.dao.TaskDAOImpl;
import dev.alan20111.todolist.model.Task;
import java.util.List;

public class TaskService {
    private final TaskDAOImpl taskDAO;

    public TaskService() {
        this.taskDAO = new TaskDAOImpl();
    }

    public javafx.concurrent.Task<Void> insertTaskAsync(Task task) {
        return new javafx.concurrent.Task<Void>() {
            @Override protected Void call() throws Exception { taskDAO.insert(task); return null; }
        };
    }

    public javafx.concurrent.Task<List<Task>> getAllTasksAsync() {
        return new javafx.concurrent.Task<List<Task>>() {
            @Override protected List<Task> call() throws Exception { return taskDAO.getAllTasks(); }
        };
    }

    public javafx.concurrent.Task<Void> updateTaskAsync(Task task) {
        return new javafx.concurrent.Task<Void>() {
            @Override protected Void call() throws Exception { taskDAO.update(task); return null; }
        };
    }

    public javafx.concurrent.Task<Void> deleteTaskAsync(String taskId) {
        return new javafx.concurrent.Task<Void>() {
            @Override protected Void call() throws Exception { taskDAO.delete(taskId); return null; }
        };
    }

    public javafx.concurrent.Task<List<Task>> getTasksByStatusAsync(String status) {
        return new javafx.concurrent.Task<List<Task>>() {
            @Override protected List<Task> call() throws Exception { return taskDAO.getTasksByStatus(status); }
        };
    }
}