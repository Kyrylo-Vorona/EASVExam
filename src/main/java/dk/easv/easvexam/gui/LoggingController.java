package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.ActivityLog;
import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.bll.Logic;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggingController implements Initializable {

    @FXML
    private TableView<ActivityLog> tblLogs;
    @FXML
    private TableColumn<ActivityLog, String> colUser;
    @FXML
    private TableColumn<ActivityLog, String> colAction;
    @FXML
    private TableColumn<ActivityLog, Timestamp> colTimestamp;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> comboTimeFilter;

    private Logic logic = Logic.getInstance();
    private ObservableList<ActivityLog> allLogsList = FXCollections.observableArrayList();
    private FilteredList<ActivityLog> filteredLogs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        comboTimeFilter.setItems(FXCollections.observableArrayList(
                "All Time",
                "Last 30 minutes",
                "Last 4 hours",
                "Last 1 day",
                "Last 5 days"
        ));
        comboTimeFilter.setValue("All Time");
        loadLogs();
        filteredLogs = new FilteredList<>(allLogsList, p -> true);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        comboTimeFilter.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        tblLogs.setItems(filteredLogs);
    }

    private void loadLogs() {
        try {
            List<ActivityLog> allLogs = logic.getAllLogs();
            allLogsList.clear();
            allLogsList.addAll(allLogs);
        } catch (MyException e) {
            OpenView.showErrorAlert("Failed to load activity logs: " + e.getMessage());
        }
    }

    private void updateFilter() {
        String searchText = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        String timeFilter = comboTimeFilter.getValue() == null ? "All Time" : comboTimeFilter.getValue();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter sqlFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        filteredLogs.setPredicate(log -> {
            if (!searchText.isEmpty()) {
                String user = log.getUsername() == null ? "" : log.getUsername().toLowerCase();
                String action = log.getAction() == null ? "" : log.getAction().toLowerCase();

                if (!user.contains(searchText) && !action.contains(searchText)) {
                    return false;
                }
            }

            if (!timeFilter.equals("All Time") && log.getTimestamp() != null) {
                try {
                    LocalDateTime logTime = LocalDateTime.parse(log.getTimestamp(), sqlFormatter);
                    long minutesBetween = Duration.between(logTime, now).toMinutes();

                    if (timeFilter.equals("Last 30 minutes")) {
                        return minutesBetween <= 30;
                    } else if (timeFilter.equals("Last 4 hours")) {
                        return minutesBetween <= (4 * 60);
                    } else if (timeFilter.equals("Last 1 day")) {
                        return minutesBetween <= (24 * 60);
                    } else if (timeFilter.equals("Last 5 days")) {
                        return minutesBetween <= (5 * 24 * 60);
                    } else {
                        return true;
                    }
                } catch (Exception e) {
                    return true;
                }
            }

            return true;
        });
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
