package dev.alan20111.todolist;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class c_controller {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
