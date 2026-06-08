package dev.alan20111.todolist.controller;

import com.google.cloud.firestore.Firestore;
import dev.alan20111.todolist.config.DBConnection;
import dev.alan20111.todolist.model.Task;
import dev.alan20111.todolist.service.TaskService;
import dev.alan20111.todolist.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskFormController {
    @FXML private Label lblTitle;
    @FXML private TextField txtTaskName;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker dpDueDate;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private MenuButton menuCategories;
    @FXML private Button btnDelete;

    private Task currentTask;
    private boolean isEditMode = false;
    private final TaskService taskService = new TaskService();

    @FXML
    public void initialize() {
        cmbStatus.getItems().addAll("Pendiente", "En Progreso", "Completada");
        cmbStatus.getSelectionModel().selectFirst();

        dpDueDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #eeeeee;");
                }
            }
        });

        loadUserCategories();
    }

    public void setTaskData(Task task) {
        if (task != null) {
            this.currentTask = task;
            this.isEditMode = true;
            lblTitle.setText("Editar Tarea");
            btnDelete.setVisible(true);

            txtTaskName.setText(task.getName());
            txtDescription.setText(task.getDescription());
            cmbStatus.setValue(task.getStatus());
            if (task.getDueDate() != null && !task.getDueDate().equals("Sin fecha")) {
                dpDueDate.setValue(LocalDate.parse(task.getDueDate()));
            }
            Platform.runLater(() -> markExistingCategories(task.getCategories()));
        }
    }

    private void loadUserCategories() {
        new Thread(() -> {
            try {
                Firestore db = DBConnection.getInstance().getFirestore();
                String user = SessionManager.getInstance().getCurrentUsername();
                var query = db.collection("categories").whereEqualTo("owner", user).get().get();

                Platform.runLater(() -> {
                    List<String> selected = getSelectedCategories();
                    menuCategories.getItems().clear();
                    for (var doc : query.getDocuments()) {
                        String name = doc.getString("name");
                        CheckMenuItem item = new CheckMenuItem(name);
                        if (selected.contains(name)) item.setSelected(true);
                        menuCategories.getItems().add(item);
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private List<String> getSelectedCategories() {
        List<String> selected = new ArrayList<>();
        for (MenuItem item : menuCategories.getItems()) {
            if (item instanceof CheckMenuItem && ((CheckMenuItem) item).isSelected()) {
                selected.add(item.getText());
            }
        }
        return selected;
    }

    private void markExistingCategories(List<String> taskCats) {
        if (taskCats == null) return;
        for (MenuItem item : menuCategories.getItems()) {
            if (item instanceof CheckMenuItem && taskCats.contains(item.getText())) {
                ((CheckMenuItem) item).setSelected(true);
            }
        }
    }

    @FXML
    private void openCategoryManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/category_management.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Categorías");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHiding(e -> loadUserCategories());
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleSave() {
        String name = txtTaskName.getText();
        LocalDate date = dpDueDate.getValue();

        if (name == null || name.trim().isEmpty()) {
            showAlert("Error", "El nombre es obligatorio.");
            return;
        }
        if (date == null) {
            showAlert("Error", "Selecciona una fecha de vencimiento.");
            return;
        }

        Task task = (isEditMode) ? currentTask : new Task();
        task.setName(name);
        task.setDescription(txtDescription.getText());
        task.setStatus(cmbStatus.getValue());
        task.setDueDate(date.toString());
        task.setCategories(getSelectedCategories());
        if(!isEditMode) task.setCreationDate(LocalDate.now().toString());

        new Thread(() -> {
            try {
                if (isEditMode) {
                    new dev.alan20111.todolist.dao.TaskDAOImpl().update(task);
                } else {
                    taskService.insertTaskAsync(task).run();
                }
                Platform.runLater(this::closeWindow);
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    private void handleDelete() {
        if (currentTask != null) {
            new Thread(() -> {
                new dev.alan20111.todolist.dao.TaskDAOImpl().delete(currentTask.getId());
                Platform.runLater(this::closeWindow);
            }).start();
        }
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) txtTaskName.getScene().getWindow()).close(); }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}