module org.example.tarea4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;


    opens org.example.tarea4 to javafx.fxml;
    exports org.example.tarea4;
}