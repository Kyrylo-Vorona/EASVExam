package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.Profile;
import dk.easv.easvexam.be.User;
import dk.easv.easvexam.bll.Logic;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AssignUsersController {
    @FXML private Label lblTitle;
    @FXML private TableView<UserWrapper> tableUsers;
    @FXML private TableColumn<UserWrapper, Boolean> colSelected;
    @FXML private TableColumn<UserWrapper, String> colUsername;
    @FXML private TableColumn<UserWrapper, String> colRole;

    private Logic logic = Logic.getInstance();
    private Profile selectedProfile;
    private ObservableList<UserWrapper> userWrappers = FXCollections.observableArrayList();

    public void setTable(Profile profile) {
        this.selectedProfile = profile;
        lblTitle.setText("Assigning to: " + profile.getName());
        loadUsers();
    }

    private void loadUsers() {
        try {
            List<User> allUsers = logic.getAllUsers();
            userWrappers.clear();

            List<User> onlyRegularUsers = allUsers.stream()
                    .filter(u -> u.getRole().equalsIgnoreCase("USER"))
                    .collect(Collectors.toList());

            for (User u : onlyRegularUsers) {
                userWrappers.add(new UserWrapper(u, false));
            }

            tableUsers.setItems(userWrappers);
            setupTable();
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    private void setupTable() {
        colUsername.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUser().getUsername()));
        colRole.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUser().getRole()));

        colSelected.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colSelected.setCellFactory(CheckBoxTableCell.forTableColumn(colSelected));
        tableUsers.setEditable(true);
    }

    @FXML
    private void onSave(ActionEvent event) {
        try {
            List<Integer> selectedUserIds = userWrappers.stream()
                    .filter(UserWrapper::isSelected)
                    .map(w -> w.getUser().getId())
                    .collect(Collectors.toList());

            logic.assignUsersToProfile(selectedProfile.getId(), selectedUserIds);
            cancel(event);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    public void cancel(ActionEvent event) {
        try {
            String filepath = "/dk/easv/easvexam/gui/AdminProfileManagementView.fxml";
            OpenView.getInstance().openView(filepath, event);
        }catch(MyException e){
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    public static class UserWrapper {
        private final User user;
        private final BooleanProperty selected;

        public UserWrapper(User user, boolean isSelected) {
            this.user = user;
            this.selected = new SimpleBooleanProperty(isSelected);
        }

        public User getUser() { return user; }
        public boolean isSelected() { return selected.get(); }
        public BooleanProperty selectedProperty() { return selected; }
    }
}