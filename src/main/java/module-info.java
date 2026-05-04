module dk.easv.easvexam {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires java.desktop;
    requires javafx.swing;
    requires javafx.base;
    requires javafx.graphics;
    requires com.microsoft.sqlserver.jdbc;
    requires java.sql;
    //requires dk.easv.easvexam;
    //requires dk.easv.easvexam;


    opens dk.easv.easvexam to javafx.fxml;
    exports dk.easv.easvexam;
    exports dk.easv.easvexam.gui;
    opens dk.easv.easvexam.gui to javafx.fxml;
    opens dk.easv.easvexam.be to javafx.base;
}