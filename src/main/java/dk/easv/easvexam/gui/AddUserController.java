package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.User;
import dk.easv.easvexam.bll.Logic;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class AddUserController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField roleField;
    @FXML
    private Button saveButton;

    private Logic logic = Logic.getInstance();
    private User userToEdit;

    public void cancel(ActionEvent event) {
        try {
            String filepath = "/dk/easv/easvexam/gui/AdminUserManagementView.fxml";
            OpenView.getInstance().openView(filepath, event);
        }catch(MyException e){
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    public void setUserData(User user) {
        this.userToEdit = user;
        usernameField.setText(user.getUsername());
        roleField.setText(user.getRole());
        passwordField.setText("");
        passwordField.setPromptText("Leave empty to keep old password");
        saveButton.setText("Update");
    }

    public void addUser(ActionEvent event) {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String role = roleField.getText();
            if(userToEdit == null) {
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
        }catch (MyException e){
            OpenView.showErrorAlert(e.getMessage());
        }
    }
}
