package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.User;
import dk.easv.easvexam.bll.Logic;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;

    private Logic logic = Logic.getInstance();

    public void onLoginClick(ActionEvent event) {
        try {
            User loggedInUser = logic.login(username.getText(), password.getText());
            if (loggedInUser != null) {
                if (loggedInUser.getRole().equalsIgnoreCase("ADMIN")) {
                    String filepath = "/dk/easv/easvexam/gui/AdminUserManagementView.fxml";
                    OpenView.getInstance().openView(filepath, event);
                } else {
                    String filepath = "/dk/easv/easvexam/gui/hello-view.fxml";
                    OpenView.getInstance().openView(filepath, event);
                }
            } else {
                OpenView.showErrorAlert("Wrong username or password");
            }
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }
}
