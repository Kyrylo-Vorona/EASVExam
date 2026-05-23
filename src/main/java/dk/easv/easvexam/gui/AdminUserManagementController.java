package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.User;
import dk.easv.easvexam.bll.Logic;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AdminUserManagementController implements Initializable {
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, Void> colActions;
    @FXML
    private TableColumn<User, String> profilesColumn;
    @FXML
    private TextField searchUserField;
    @FXML
    private TableView<User> userTable;
    private ObservableList<User> userList;
    private Map<Integer, List<String>> userProfilesMap;

    private Logic logic = Logic.getInstance();

    public void logOut(ActionEvent event) {
        try {
            String filepath = "/dk/easv/easvexam/gui/LoginView.fxml";
            OpenView.getInstance().openView(filepath, event);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            readDataIntoList();
            setupActionsColumn();
            userTable.setFixedCellSize(35);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    private void readDataIntoList() throws MyException {
        userList = FXCollections.observableArrayList();
        userList.addAll(logic.getAllUsers());
        userProfilesMap = logic.getUsersProfilesMap();
        userTable.setItems(userList);
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        setupProfilesColumn();
        FilteredList<User> filteredData = new FilteredList<>(userList, p -> true);
        searchUserField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase().trim();
                if (user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sortedData);
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        setupProfilesColumn();
    }

    private void setupActionsColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button("Edit");
                    private final Button btnDelete = new Button("Delete");
                    private final HBox container = new HBox(10, btnEdit, btnDelete);

                    {
                        btnEdit.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            openEditUser(event, user);
                        });

                        btnDelete.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            handleDeleteUser(user);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(container);
                        }
                    }
                };
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    private void setupProfilesColumn() {
        profilesColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            List<String> profileNames = userProfilesMap.get(user.getId());
            if (profileNames == null || profileNames.isEmpty()) {
                return new javafx.beans.property.SimpleStringProperty("-");
            }
            String joinedNames = String.join(", ", profileNames);
            return new javafx.beans.property.SimpleStringProperty(joinedNames);
        });
    }

    public void openAddUser(ActionEvent event) {
        try {
            String filepath = "/dk/easv/easvexam/gui/AddUserView.fxml";
            OpenView.getInstance().openView(filepath, event);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    private void openEditUser(ActionEvent event, User user) {
        try {
            FXMLLoader loader = OpenView.getInstance().openView("/dk/easv/easvexam/gui/AddUserView.fxml", event);
            AddUserController controller = loader.getController();
            controller.setUserData(user);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setContentText("Are you sure you want to delete " + user.getUsername() + "?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            logic.deleteUser(user);
            readDataIntoList();
        }
    }

    public void openProfileManagement(ActionEvent event) {
        try {
            String filepath = "/dk/easv/easvexam/gui/AdminProfileManagementView.fxml";
            OpenView.getInstance().openView(filepath, event);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    @FXML
    public void openLogging(ActionEvent event) {
        try {
            String filepath = "/dk/easv/easvexam/gui/LoggingView.fxml";
            OpenView.getInstance().openView(filepath, event);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }
}
