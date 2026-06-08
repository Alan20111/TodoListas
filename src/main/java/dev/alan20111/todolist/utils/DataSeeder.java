package dev.alan20111.todolist.utils;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import dev.alan20111.todolist.config.DBConnection;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class DataSeeder {

    public static void main(String[] args) {
        System.out.println("inyección de datos");

        Firestore db = DBConnection.getInstance().getFirestore();

        String[] users = {"Daniel", "Matha", "Alan", "Maria", "Carlos"};

        String[] possibleCategories = {"Universidad", "Trabajo", "Hogar", "Proyectos", "Salud", "Finanzas"};
        String[] statuses = {"Pendiente", "En Progreso", "Completada"};
        String[] taskNames = {"Revisar reporte de", "Comprar materiales para", "Estudiar para examen de", "Enviar correo a", "Terminar diseño de", "Agendar junta sobre"};

        Random random = new Random();

        try {
            for (String user : users) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("username", user);
                userData.put("password", "123");
                db.collection("users").add(userData).get();
                System.out.println("Usuario registrado: " + user);

                List<String> userCats = new ArrayList<>();
                for (String catName : possibleCategories) {
                    Map<String, Object> catData = new HashMap<>();
                    catData.put("name", catName);
                    catData.put("owner", user);

                    DocumentReference catRef = db.collection("categories").add(catData).get();
                    catRef.update("id", catRef.getId()).get();
                    userCats.add(catName);
                }

                for (int i = 1; i <= 15; i++) {
                    Map<String, Object> taskData = new HashMap<>();

                    String randomTaskName = taskNames[random.nextInt(taskNames.length)] + " " + userCats.get(random.nextInt(userCats.size()));
                    taskData.put("name", randomTaskName);
                    taskData.put("description", "Descripción generada automáticamente para pruebas de estrés y gráficos estadísticos. Registro #" + i);
                    taskData.put("owner", user);
                    taskData.put("status", statuses[random.nextInt(statuses.length)]);

                    LocalDate creationDate = LocalDate.now().minusDays(random.nextInt(60));
                    taskData.put("creationDate", creationDate.toString());

                    if (random.nextInt(100) < 70) {

                        taskData.put("dueDate", creationDate.plusDays(random.nextInt(20) + 1).toString());
                    } else {
                        taskData.put("dueDate", "Sin fecha");
                    }

                    List<String> selectedCats = new ArrayList<>();
                    int numCats = random.nextInt(3);
                    for(int j = 0; j < numCats; j++) {
                        String randomCat = userCats.get(random.nextInt(userCats.size()));
                        if(!selectedCats.contains(randomCat)) {
                            selectedCats.add(randomCat);
                        }
                    }
                    taskData.put("categories", selectedCats);

                    DocumentReference taskRef = db.collection("tasks").add(taskData).get();
                    taskRef.update("id", taskRef.getId()).get();
                }
                System.out.println("   -> Generadas 15 tareas y categorías para " + user);
            }
            System.exit(0);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error");
            e.printStackTrace();
        }
    }
}