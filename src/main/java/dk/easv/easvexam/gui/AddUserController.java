package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.User;
import dk.easv.easvexam.bll.Logic;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class AddUserController implements Initializable {
    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private ComboBox<String> comboRole;
    @FXML private Button saveButton;

    private Logic logic = Logic.getInstance();
    private User userToEdit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboRole.setItems(FXCollections.observableArrayList("ADMIN", "USER"));
        comboRole.setEditable(false);
        comboRole.getSelectionModel().select("USER");
    }

    public void setUserData(User user) {
        this.userToEdit = user;
        usernameField.setText(user.getUsername());
        if (user.getRole() != null) {
            comboRole.getSelectionModel().select(user.getRole().toUpperCase());
        }
        passwordField.setText("");
        passwordField.setPromptText("Leave empty to keep old password");
        saveButton.setText("Update");
    }

    public void addUser(ActionEvent event) {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String role = comboRole.getValue();

            if (username.isEmpty() || role == null) {
                OpenView.showErrorAlert("Username and Role are required!");
                return;
            }

            if (userToEdit == null) {
                if (password.isEmpty()) {
                    OpenView.showErrorAlert("Password is required for new users!");
                    return;
                }
                logic.addUser(username, password, role);
            } else {
                userToEdit.setUsername(username);
                userToEdit.setRole(role);
                logic.editUser(userToEdit, password);
            }
            cancel(event);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    public void cancel(ActionEvent event) {
        try {
            String filepath = "/dk/easv/easvexam/gui/AdminUserManagementView.fxml";
            OpenView.getInstance().openView(filepath, event);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }
}
