package dev.alan20111.todolist.dao;

import dev.alan20111.todolist.model.Task;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface TaskDAO {
    void insert(Task task) throws InterruptedException, ExecutionException;
    void update(Task task) throws InterruptedException, ExecutionException;
    void delete(String taskId) throws InterruptedException, ExecutionException;
    List<Task> getAllTasks() throws InterruptedException, ExecutionException;
    List<Task> getTasksByStatus(String status) throws InterruptedException, ExecutionException;
}