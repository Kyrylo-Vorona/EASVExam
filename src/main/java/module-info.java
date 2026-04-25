module dk.easv.easvexam {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires java.desktop;
    requires javafx.swing;
    requires javafx.base;
    requires javafx.graphics;


    opens dk.easv.easvexam to javafx.fxml;
    exports dk.easv.easvexam;
    exports dk.easv.easvexam.gui;
    opens dk.easv.easvexam.gui to javafx.fxml;
}