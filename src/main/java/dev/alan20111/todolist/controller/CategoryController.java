package dev.alan20111.todolist.controller;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import dev.alan20111.todolist.config.DBConnection;
import dev.alan20111.todolist.model.Category;
import dev.alan20111.todolist.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class CategoryController {

    @FXML private TextField txtNewCategory;
    @FXML private ListView<String> listCategories;

    private Firestore db;
    private String currentUser;

    @FXML
    public void initialize() {
        db = DBConnection.getInstance().getFirestore();
        currentUser = SessionManager.getInstance().getCurrentUsername();
        loadCategories();
    }

    private void loadCategories() {
        db.collection("categories")
                .whereEqualTo("owner", currentUser)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) return;
                    Platform.runLater(() -> {
                        listCategories.getItems().clear();
                        if (snapshots != null) {
                            for (QueryDocumentSnapshot doc : snapshots) {
                                listCategories.getItems().add(doc.getString("name"));
                            }
                        }
                    });
                });
    }

    @FXML
    private void handleAddCategory() {
        String name = txtNewCategory.getText().trim();
        if (name.isEmpty()) return;

        new Thread(() -> {
            try {
                Category cat = new Category(null, name, currentUser);
                DocumentReference ref = db.collection("categories").add(cat).get();
                ref.update("id", ref.getId());
                Platform.runLater(() -> txtNewCategory.clear());
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    private void handleDeleteCategory() {
        String selected = listCategories.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        new Thread(() -> {
            try {
                QuerySnapshot query = db.collection("categories")
                        .whereEqualTo("owner", currentUser)
                        .whereEqualTo("name", selected).get().get();
                for (QueryDocumentSnapshot doc : query.getDocuments()) {
                    doc.getReference().delete();
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}