package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.MyException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class OpenView {
    public FXMLLoader openView(String filepath, ActionEvent event) throws MyException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(filepath));
            Scene scene = new Scene(fxmlLoader.load(), 700, 450);
            if (getClass().getResource("/css/style.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            return fxmlLoader;
        }catch(IOException e){
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
