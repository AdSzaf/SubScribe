
package com.example.subscribe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.subscribe.database.DatabaseManager;
import com.example.subscribe.events.EventBusManager;
import com.example.subscribe.services.ReminderService;
import com.example.subscribe.utils.ConfigManager;
import com.example.subscribe.services.ReminderService;
import javafx.scene.image.Image;

public class SubScribeApplication extends Application {
    private ReminderService reminderService;
    @Override
    public void start(Stage stage) throws Exception {

        ConfigManager.loadConfig();

        DatabaseManager.getInstance().initDatabase();

        EventBusManager.getInstance();

        reminderService = new ReminderService();
        reminderService.start();

        FXMLLoader fxmlLoader = new FXMLLoader(
                SubScribeApplication.class.getResource("/com/example/subscribe/fxml/main-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        scene.getStylesheets().add(
                getClass().getResource("/com/example/subscribe/css/application.css").toExternalForm()
        );

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/subscribe/images/icons/icon1.png")));
        
        stage.setTitle("SubScribe - Subscription Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
    @Override
    public void stop() throws Exception {
        if (reminderService != null) {
            reminderService.stop();
        }
        super.stop();
    }
}