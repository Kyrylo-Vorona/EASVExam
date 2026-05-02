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
    public FXMLLoader openView(String filepath, ActionEvent event) throws MyException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(filepath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean isMaximized = stage.isMaximized(); // Запоминаем текущее состояние

            Scene scene = new Scene(root); // Создаем сцену без жестких размеров
            stage.setScene(scene);

            stage.show(); // Сначала показываем окно

            // И только ПОСЛЕ show принудительно возвращаем полноэкранный режим
            if (isMaximized) {
                stage.setMaximized(false); // Маленький хак: сброс и повторная установка
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
