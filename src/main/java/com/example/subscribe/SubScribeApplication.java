// src/main/java/com/example/subscribe/SubScribeApplication.java
package com.example.subscribe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.subscribe.database.DatabaseManager;
import com.example.subscribe.events.EventBusManager;
import com.example.subscribe.utils.ConfigManager;

public class SubScribeApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Initialize configuration
        ConfigManager.loadConfig();

        // Initialize database
        DatabaseManager.getInstance().initDatabase();

        // Initialize event bus
        EventBusManager.getInstance();

        // Load main FXML
        FXMLLoader fxmlLoader = new FXMLLoader(
                SubScribeApplication.class.getResource("/com/example/subscribe/fxml/main-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        scene.getStylesheets().add(
                getClass().getResource("/com/example/subscribe/css/application.css").toExternalForm()
        );

        stage.setTitle("SubScribe - Subscription Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}