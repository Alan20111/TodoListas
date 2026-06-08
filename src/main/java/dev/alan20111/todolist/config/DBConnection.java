package dev.alan20111.todolist.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.InputStream;
import java.io.IOException;

public final class DBConnection {

    private static DBConnection instance;

    private Firestore firestoreDB;

    private DBConnection() {
        initFirebase();
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    private void initFirebase() {
        try {
            InputStream serviceAccount = getClass().getResourceAsStream("/firebase-service-account.json");

            if (serviceAccount == null) {
                throw new IOException("Archivo de configuración firebase-service-account.json no encontrado en /resources.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("[SISTEMA] Firebase inicializado correctamente.");
            }

            this.firestoreDB = FirestoreClient.getFirestore();

        } catch (IOException e) {
            System.err.println("[ERROR CRÍTICO] Falla al inicializar la conexión con Firebase.");
            e.printStackTrace();
        }
    }

    public Firestore getFirestore() {
        return firestoreDB;
    }
}