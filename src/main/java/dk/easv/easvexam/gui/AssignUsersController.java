package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.Profile;
import dk.easv.easvexam.be.User;
import dk.easv.easvexam.bll.Logic;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AssignUsersController {
    @FXML private Label lblTitle;
    @FXML private TableView<UserWrapper> tableUsers;
    @FXML private TableColumn<UserWrapper, Boolean> colSelected;
    @FXML private TableColumn<UserWrapper, String> colUsername;
    @FXML private TableColumn<UserWrapper, String> colRole;
    @FXML
    private TextField searchUserField;

    private Logic logic = Logic.getInstance();
    private Profile selectedProfile;
    private ObservableList<UserWrapper> userWrappers = FXCollections.observableArrayList();

    public void setTable(Profile profile) {
        this.selectedProfile = profile;
        lblTitle.setText("Assigning to: " + profile.getName() + " (ID: " + profile.getId() + ")");
        loadUsers();
    }

    private void loadUsers() {
        try {
            List<User> allUsers = logic.getAllUsers();
            List<Integer> assignedIds = logic.getUserIdsForProfile(selectedProfile.getId());
            userWrappers.clear();

            for (User u : allUsers) {
                if (u.getRole().equalsIgnoreCase("USER")) {
                    boolean isAlreadyAssigned = assignedIds.contains(u.getId());
                    userWrappers.add(new UserWrapper(u, isAlreadyAssigned));
                }
            }
            FilteredList<UserWrapper> filteredData = new FilteredList<>(userWrappers, p -> true);
            searchUserField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(wrapper -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = newValue.toLowerCase().trim();
                    User user = wrapper.getUser();
                    if (user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    else if (user.getRole() != null && user.getRole().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    return false;
                });
            });

            SortedList<UserWrapper> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(tableUsers.comparatorProperty());
            tableUsers.setItems(sortedData);
            setupTable();
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    private void setupTable() {
        tableUsers.setEditable(true);
        colUsername.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUser().getUsername()));
        colRole.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUser().getRole()));
        colSelected.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colSelected.setCellFactory(CheckBoxTableCell.forTableColumn(colSelected));
    }

    @FXML
    private void onSave(ActionEvent event) {
        try {
            List<Integer> idsToAssign = new ArrayList<>();
            for (UserWrapper wrapper : userWrappers) {
                if (wrapper.isSelected()) {
                    idsToAssign.add(wrapper.getUser().getId());
                }
            }

            logic.assignUsersToProfile(selectedProfile.getId(), idsToAssign);
            cancel(event);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    public void cancel(ActionEvent event) {
        try {
            String filepath = "/dk/easv/easvexam/gui/AdminProfileManagementView.fxml";
            OpenView.getInstance().openView(filepath, event);
        } catch(MyException e){
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