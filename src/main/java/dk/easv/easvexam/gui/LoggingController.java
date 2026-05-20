package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.ActivityLog;
import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.bll.Logic;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class LoggingController implements Initializable {

    @FXML
    private TableView<ActivityLog> tblLogs;
    @FXML
    private TableColumn<ActivityLog, String> colUser;
    @FXML
    private TableColumn<ActivityLog, String> colAction;
    @FXML
    private TableColumn<ActivityLog, Timestamp> colTimestamp;

    private Logic logic = Logic.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        loadLogs();
    }

    private void loadLogs() {
        try {
            List<ActivityLog> allLogs = logic.getAllLogs();
            tblLogs.setItems(FXCollections.observableArrayList(allLogs));
        } catch (MyException e) {
            OpenView.showErrorAlert("Failed to load activity logs: " + e.getMessage());
        }
    }

    @FXML
    public void openUserManagement(ActionEvent event) {
        try {
            String filepath = "/dk/easv/easvexam/gui/AdminUserManagementView.fxml";
            OpenView.getInstance().openView(filepath, event);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }
}
