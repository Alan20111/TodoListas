package dev.alan20111.todolist.dao;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import dev.alan20111.todolist.config.DBConnection;
import dev.alan20111.todolist.model.Task;
import dev.alan20111.todolist.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class TaskDAOImpl {
    private final String COLLECTION_NAME = "tasks";

    public void insert(Task task) {
        try {
            Firestore db = DBConnection.getInstance().getFirestore();
            task.setOwner(SessionManager.getInstance().getCurrentUsername());
            ApiFuture<DocumentReference> future = db.collection(COLLECTION_NAME).add(task);
            String generatedId = future.get().getId();
            db.collection(COLLECTION_NAME).document(generatedId).update("id", generatedId);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update(Task task) {
        try {
            Firestore db = DBConnection.getInstance().getFirestore();
            task.setOwner(SessionManager.getInstance().getCurrentUsername());
            db.collection(COLLECTION_NAME).document(task.getId()).set(task);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void delete(String taskId) {
        try {
            Firestore db = DBConnection.getInstance().getFirestore();
            db.collection(COLLECTION_NAME).document(taskId).delete();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        try {
            Firestore db = DBConnection.getInstance().getFirestore();
            String currentUser = SessionManager.getInstance().getCurrentUsername();
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).whereEqualTo("owner", currentUser).get();
            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                tasks.add(doc.toObject(Task.class));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return tasks;
    }

    public List<Task> getTasksByStatus(String status) {
        List<Task> tasks = new ArrayList<>();
        try {
            Firestore db = DBConnection.getInstance().getFirestore();
            String currentUser = SessionManager.getInstance().getCurrentUsername();
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                    .whereEqualTo("owner", currentUser)
                    .whereEqualTo("status", status)
                    .get();
            for (QueryDocumentSnapshot document : future.get().getDocuments()) {
                tasks.add(document.toObject(Task.class));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return tasks;
    }
}