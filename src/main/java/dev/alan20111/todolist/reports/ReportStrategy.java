package dev.alan20111.todolist.reports;

import dev.alan20111.todolist.model.Task;
import java.util.List;

public interface ReportStrategy {
    void generateReport(List<Task> tasks) throws Exception;
}