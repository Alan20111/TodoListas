package dev.alan20111.todolist.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import dev.alan20111.todolist.config.DBConnection;
import dev.alan20111.todolist.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;
    @FXML private Button btnRegister;

    @FXML
    private void handleLogin() {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("Por favor, llena todos los campos.");
            return;
        }

        btnLogin.setDisable(true);
        lblError.setText("Validando credenciales...");

        new Thread(() -> {
            try {
                Firestore db = DBConnection.getInstance().getFirestore();
                ApiFuture<QuerySnapshot> query = db.collection("users")
                        .whereEqualTo("username", user)
                        .whereEqualTo("password", pass)
                        .get();

                if (!query.get().isEmpty()) {
                    SessionManager.getInstance().setCurrentUsername(user);
                    Platform.runLater(this::navigateToTasks);
                } else {
                    Platform.runLater(() -> {
                        lblError.setText("Usuario o contraseña incorrectos.");
                        btnLogin.setDisable(false);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() -> {
                    lblError.setText("Error de conexión con Firebase.");
                    btnLogin.setDisable(false);
                });
            }
        }).start();
    }

    private void navigateToTasks() {
        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/tasks.fxml"));
            stage.setScene(new Scene(root, 800, 600));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleRegister() {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("Escribe un usuario y contraseña para registrarte.");
            return;
        }

        btnLogin.setDisable(true);
        btnRegister.setDisable(true);
        lblError.setText("Registrando en la nube...");

        new Thread(() -> {
            try {
                Firestore db = DBConnection.getInstance().getFirestore();

                // 1. Verificar si el usuario ya existe
                ApiFuture<QuerySnapshot> checkUser = db.collection("users")
                        .whereEqualTo("username", user)
                        .get();

                if (!checkUser.get().isEmpty()) {
                    Platform.runLater(() -> {
                        lblError.setText("Error: El usuario '" + user + "' ya existe.");
                        btnLogin.setDisable(false);
                        btnRegister.setDisable(false);
                    });
                    return;
                }

                // 2. Si no existe, lo creamos
                java.util.Map<String, Object> newUser = new java.util.HashMap<>();
                newUser.put("username", user);
                newUser.put("password", pass);

                db.collection("users").document().set(newUser).get();

                Platform.runLater(() -> {
                    lblError.setText("¡Cuenta creada! Ya puedes iniciar sesión.");
                    lblError.setTextFill(javafx.scene.paint.Color.GREEN);
                    txtPassword.clear();
                    btnLogin.setDisable(false);
                    btnRegister.setDisable(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    lblError.setText("Error al registrar: " + e.getMessage());
                    lblError.setTextFill(javafx.scene.paint.Color.RED);
                    btnLogin.setDisable(false);
                    btnRegister.setDisable(false);
                });
            }
        }).start();
    }
}
