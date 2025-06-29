module graa {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    exports org.example;
    opens org.example to javafx.graphics, javafx.fxml;
}