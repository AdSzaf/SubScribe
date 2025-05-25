module com.example.subscribe {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.google.common;


    exports com.example.subscribe;
    exports com.example.subscribe.controllers to javafx.fxml;
    exports com.example.subscribe.models;
    exports com.example.subscribe.events to com.google.common;

    opens com.example.subscribe to javafx.fxml;
    opens com.example.subscribe.controllers to javafx.fxml, com.google.common;
    opens com.example.subscribe.models to javafx.base;
    opens com.example.subscribe.events to com.google.common;


}