package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.MyException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class OpenView {
    private static OpenView instance;

    private OpenView() {}

    public static OpenView getInstance() {
        if (instance == null) {
            instance = new OpenView();
        }
        return instance;
    }

    public FXMLLoader openView(String filepath, ActionEvent event) throws MyException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(filepath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean isMaximized = stage.isMaximized();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            if (isMaximized) {
                stage.setMaximized(false);
                stage.setMaximized(true);
            }
            return loader;
        } catch (IOException e) {
            throw new MyException("Could not load the window: " + filepath, e);
        }
    }

    public static void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Something went wrong");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
