package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.Profile;
import dk.easv.easvexam.be.User;
import dk.easv.easvexam.bll.Logic;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.controlsfx.control.CheckComboBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AddUserController implements Initializable {
    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private ComboBox<String> comboRole;
    @FXML private Button saveButton;
    @FXML private Label lblTitle;
    @FXML private CheckComboBox<Profile> checkComboBox;

    private Logic logic = Logic.getInstance();
    private User userToEdit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboRole.setItems(FXCollections.observableArrayList("ADMIN", "USER"));
        comboRole.setEditable(false);
        comboRole.getSelectionModel().select("USER");
        try {
            List<Profile> allProfiles = logic.getAllProfiles();
            checkComboBox.getItems().addAll(allProfiles);
            checkComboBox.setConverter(new javafx.util.StringConverter<Profile>() {
                @Override
                public String toString(Profile profile) {
                    return profile == null ? "" : profile.getName();
                }

                @Override
                public Profile fromString(String string) {
                    return null;
                }
            });
            checkComboBox.setTitle("Select Profiles...");
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    public void setUserData(User user) {
        this.userToEdit = user;
        lblTitle.setText("User Details - ID: " + user.getId());
        usernameField.setText(user.getUsername());
        if (user.getRole() != null) {
            comboRole.getSelectionModel().select(user.getRole().toUpperCase());
        }
        passwordField.setText("");
        passwordField.setPromptText("Leave empty to keep old password");
        saveButton.setText("Update");

        try {
            List<Integer> assignedProfileIds = logic.getProfileIdsForUser(user.getId());

            for (Profile p : checkComboBox.getItems()) {
                if (assignedProfileIds.contains(p.getId())) {
                    checkComboBox.getCheckModel().check(p);
                }
            }
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
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

            ObservableList<Profile> selectedProfiles = checkComboBox.getCheckModel().getCheckedItems();
            List<Integer> profileIds = selectedProfiles.stream()
                    .map(Profile::getId)
                    .collect(Collectors.toList());

            if (userToEdit == null) {
                if (password.isEmpty()) {
                    OpenView.showErrorAlert("Password is required for new users!");
                    return;
                }

                logic.addUserWithProfiles(username, password, role, profileIds);
            } else {
                userToEdit.setUsername(username);
                userToEdit.setRole(role);
                logic.editUserWithProfiles(userToEdit, password, profileIds);
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
