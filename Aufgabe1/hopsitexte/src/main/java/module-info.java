module com.aufgabe1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.aufgabe1 to javafx.fxml;

    exports com.aufgabe1;
}
