package dev.alan20111.todolist.controller;

import com.google.cloud.firestore.Firestore;
import dev.alan20111.todolist.config.DBConnection;
import dev.alan20111.todolist.model.Task;
import dev.alan20111.todolist.service.ReportService;
import dev.alan20111.todolist.service.TaskService;
import dev.alan20111.todolist.utils.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.List;

public class TaskController {

    @FXML private Label lblWelcome;
    @FXML private ComboBox<String> cmbReportType;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterCategory;

    @FXML private TableView<Task> tableTasks;
    @FXML private TableColumn<Task, String> colName;
    @FXML private TableColumn<Task, String> colDescription;
    @FXML private TableColumn<Task, String> colCategories;
    @FXML private TableColumn<Task, String> colStatus;
    @FXML private TableColumn<Task, String> colDueDate;
    @FXML private TableColumn<Task, String> colCreationDate;
    @FXML private TableColumn<Task, Void> colActions;

    private final TaskService taskService = new TaskService();
    private final ObservableList<Task> taskList = FXCollections.observableArrayList();
    private FilteredList<Task> filteredData;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            if (stage != null) {
                Screen screen = Screen.getPrimary();
                double halfWidth = screen.getVisualBounds().getWidth() / 2;
                double height = screen.getVisualBounds().getHeight() * 0.85; // 85% del alto de la pantalla

                stage.setWidth(halfWidth);
                stage.setHeight(height);
                stage.centerOnScreen(); // Lo centra perfectamente
            }
        });

        cmbReportType.getItems().addAll("PDF", "Excel");

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colCreationDate.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        colCategories.setCellValueFactory(data -> new SimpleStringProperty(
                (data.getValue().getCategories() != null && !data.getValue().getCategories().isEmpty())
                        ? String.join(", ", data.getValue().getCategories()) : "Ninguna"
        ));

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Inspeccionar");
            {
                btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
                btn.setOnAction(e -> openTaskForm(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        filteredData = new FilteredList<>(taskList, p -> true);
        tableTasks.setItems(filteredData);
        txtSearch.textProperty().addListener((obs, old, newVal) -> applyFilters());
        cmbFilterCategory.setOnAction(e -> applyFilters());

        String user = SessionManager.getInstance().getCurrentUsername();
        if (user != null) lblWelcome.setText("Mis Tareas - Sesión de: " + user.toUpperCase());

        loadTasks();
        refreshCategoryFilter();
    }

    private void applyFilters() {
        String search = txtSearch.getText().toLowerCase().trim();
        String cat = cmbFilterCategory.getValue();
        filteredData.setPredicate(t -> {
            boolean mText = search.isEmpty() || t.getName().toLowerCase().contains(search) || t.getDescription().toLowerCase().contains(search);
            boolean mCat = cat == null || cat.equals("Todas") || (t.getCategories() != null && t.getCategories().contains(cat));
            return mText && mCat;
        });
    }

    @FXML private void clearFilters() {
        txtSearch.clear();
        cmbFilterCategory.getSelectionModel().select("Todas");
    }

    private void loadTasks() {
        javafx.concurrent.Task<List<Task>> worker = taskService.getAllTasksAsync();
        worker.setOnSucceeded(e -> taskList.setAll(worker.getValue()));
        new Thread(worker).start();
    }

    private void refreshCategoryFilter() {
        new Thread(() -> {
            try {
                Firestore db = DBConnection.getInstance().getFirestore();
                String user = SessionManager.getInstance().getCurrentUsername();
                var query = db.collection("categories").whereEqualTo("owner", user).get().get();
                Platform.runLater(() -> {
                    cmbFilterCategory.getItems().clear();
                    cmbFilterCategory.getItems().add("Todas");
                    for (var doc : query.getDocuments()) cmbFilterCategory.getItems().add(doc.getString("name"));
                    cmbFilterCategory.getSelectionModel().select("Todas");
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML private void openTaskFormForNew() { openTaskForm(null); }

    private void openTaskForm(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/task_form.fxml"));
            Parent root = loader.load();
            ((TaskFormController)loader.getController()).setTaskData(task);
            Stage stage = new Stage();
            stage.setTitle(task == null ? "Nueva Tarea" : "Editar Tarea");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHiding(e -> loadTasks());
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void openCategoryManager() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/category_management.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Gestor de Categorías");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHiding(e -> refreshCategoryFilter());
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void openStatistics() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/statistics.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Estadísticas");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));

            // También forzamos a que la ventana de estadísticas mida la mitad
            Screen screen = Screen.getPrimary();
            double halfWidth = screen.getVisualBounds().getWidth() / 2;
            stage.setWidth(halfWidth);
            stage.setHeight(screen.getVisualBounds().getHeight() * 0.8);
            stage.centerOnScreen();

            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleLogout(javafx.event.ActionEvent e) {
        try {
            SessionManager.getInstance().logout();
            Stage stage = (Stage)((javafx.scene.Node)e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/login.fxml")), 400, 450));
            stage.centerOnScreen();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @FXML private void handleGenerateReport() {
        String reportType = cmbReportType.getValue();
        if (reportType == null) {
            showAlert("Aviso", "Selecciona un formato.", Alert.AlertType.WARNING);
            return;
        }

        List<Task> tasksToExport = tableTasks.getItems();
        ReportService reportService = new ReportService();
        String fileName = "Reporte_Tareas_" + System.currentTimeMillis();

        try {
            if (reportType.equals("PDF")) {
                reportService.generatePDF(tasksToExport, fileName + ".pdf");
            } else {
                reportService.generateExcel(tasksToExport, fileName + ".xlsx");
            }
            showAlert("Éxito", "Reporte generado exitosamente:\n" + fileName, Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo generar el reporte.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}