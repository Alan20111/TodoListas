package dev.alan20111.todolist.controller;

import dev.alan20111.todolist.model.Task;
import dev.alan20111.todolist.service.TaskService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsController {

    @FXML private PieChart pieChartStatus;
    @FXML private BarChart<String, Number> barChartCategories;
    @FXML private ComboBox<String> cmbTimePeriod;

    private final TaskService taskService = new TaskService();
    private List<Task> allTasks;

    @FXML
    public void initialize() {
        cmbTimePeriod.getItems().addAll("Todo", "Esta Semana", "Este Mes", "Este Año");
        cmbTimePeriod.getSelectionModel().select("Todo");
        cmbTimePeriod.setOnAction(e -> updateCharts());
        loadDataFromFirebase();
    }

    private void loadDataFromFirebase() {
        javafx.concurrent.Task<List<Task>> worker = taskService.getAllTasksAsync();
        worker.setOnSucceeded(e -> {
            this.allTasks = worker.getValue();
            updateCharts();
        });
        new Thread(worker).start();
    }

    private void updateCharts() {
        if (allTasks == null) return;

        String period = cmbTimePeriod.getValue();
        LocalDate now = LocalDate.now();

        List<Task> filteredTasks = allTasks.stream().filter(t -> {
            if (period.equals("Todo")) return true;
            try {
                LocalDate creation = LocalDate.parse(t.getCreationDate());
                if (period.equals("Esta Semana")) return ChronoUnit.DAYS.between(creation, now) <= 7;
                if (period.equals("Este Mes")) return creation.getMonth() == now.getMonth() && creation.getYear() == now.getYear();
                if (period.equals("Este Año")) return creation.getYear() == now.getYear();
            } catch (Exception e) { return false; }
            return true;
        }).collect(Collectors.toList());

        long completed = filteredTasks.stream().filter(t -> "Completada".equals(t.getStatus())).count();
        long notCompleted = filteredTasks.size() - completed;

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Completadas (" + completed + ")", completed),
                new PieChart.Data("Pendientes (" + notCompleted + ")", notCompleted)
        );
        pieChartStatus.setData(pieData);

        Map<String, Integer> categoryCounts = new HashMap<>();
        for (Task t : filteredTasks) {
            if (t.getCategories() != null) {
                for (String cat : t.getCategories()) {
                    categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0) + 1);
                }
            }
        }

        barChartCategories.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        categoryCounts.forEach((cat, count) -> series.getData().add(new XYChart.Data<>(cat, count)));
        barChartCategories.getData().add(series);
    }

    @FXML private void closeWindow() { ((Stage) cmbTimePeriod.getScene().getWindow()).close(); }
}