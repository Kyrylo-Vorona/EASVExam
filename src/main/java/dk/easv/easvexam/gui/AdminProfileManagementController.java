package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.Profile;
import dk.easv.easvexam.be.User;
import dk.easv.easvexam.bll.Logic;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminProfileManagementController implements Initializable {
    @FXML
    private TableView<Profile> tableProfiles;
    @FXML
    private TableColumn<Profile, Integer> idColumn;
    @FXML
    private TableColumn<Profile, String> nameColumn;
    @FXML
    private TableColumn<Profile, Void> colActions;
    @FXML
    private TextField txtName;
    @FXML
    private Slider sliderBrightness;
    @FXML
    private Label lblBrightnessValue;
    @FXML
    private TextField txtRotation;
    @FXML
    private ImageView imagePreview;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label lblStatus;

    private Logic logic = Logic.getInstance();
    private ObservableList<Profile> profileObservableList = FXCollections.observableArrayList();
    private Profile selectedProfile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        setupActionsColumn();
        refreshTable();

        tableProfiles.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newValue) -> {
            if (newValue != null) showProfileDetails(newValue);
        });

        ColorAdjust colorAdjust = new ColorAdjust();
        imagePreview.setEffect(colorAdjust);
        sliderBrightness.valueProperty().addListener((obs, oldVal, newVal) -> {
            colorAdjust.setBrightness(newVal.doubleValue());
            lblBrightnessValue.setText((int)(newVal.doubleValue() * 100) + "%");
        });

        txtRotation.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (newVal != null && !newVal.isEmpty()) {
                    imagePreview.setRotate(Double.parseDouble(newVal));
                }
                else {
                    imagePreview.setRotate(0);
                }
            } catch (NumberFormatException ignored) {}
        });

        initPreview();
    }

    private void initPreview() {
        Image image = new Image(getClass().getResourceAsStream("images/test_document.png"));
        imagePreview.fitWidthProperty().unbind();
        imagePreview.fitHeightProperty().unbind();
        imagePreview.setImage(image);
        imagePreview.fitWidthProperty().bind(scrollPane.widthProperty());
        imagePreview.fitHeightProperty().bind(scrollPane.heightProperty());
    }

    private void showProfileDetails(Profile profile) {
        this.selectedProfile = profile;
        txtName.setText(profile.getName());
        double br = profile.getBrightness() / 100.0;
        sliderBrightness.setValue(br);
        txtRotation.setText(String.valueOf(profile.getRotateDegrees()));
        imagePreview.setRotate(profile.getRotateDegrees());
    }

    private void refreshTable() {
        try {
            profileObservableList.clear();
            profileObservableList.addAll(logic.getAllProfiles());
            tableProfiles.setItems(profileObservableList);
        } catch (MyException e) {
            OpenView.showErrorAlert("Could not load profiles: " + e.getMessage());
        }
    }

    @FXML
    private void onAddNewProfile(ActionEvent event) {
        try {
            logic.addProfile("New Profile", 0, 0);
            refreshTable();
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    private void setupActionsColumn() {
        Callback<TableColumn<Profile, Void>, TableCell<Profile, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Profile, Void> call(final TableColumn<Profile, Void> param) {
                return new TableCell<>() {
                    private final Button btnAssign = new Button("Assign");
                    private final Button btnDelete = new Button("Delete");
                    private final HBox pane = new HBox(btnAssign, btnDelete);

                    {
                        pane.setSpacing(5);
                        btnAssign.setOnAction(event -> {
                            Profile data = getTableView().getItems().get(getIndex());
                            openAssignUserView(event, data);
                        });

                        btnDelete.setOnAction(event -> {
                            Profile data = getTableView().getItems().get(getIndex());
                            handleDeleteProfile(data);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    private void handleDeleteProfile(Profile profile) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + profile.getName() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    logic.deleteProfile(profile);
                    refreshTable();
                } catch (MyException e) {
                    OpenView.showErrorAlert(e.getMessage());
                }
            }
        });
    }

    public void logOut(ActionEvent event) {
        try {
            String filepath = "/dk/easv/easvexam/gui/LoginView.fxml";
            OpenView.getInstance().openView(filepath, event);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    public void openUserManagement(ActionEvent event) {
        try {
            String filepath = "/dk/easv/easvexam/gui/AdminUserManagementView.fxml";
            OpenView.getInstance().openView(filepath, event);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }

    public void openAssignUserView(ActionEvent event, Profile profile) {
        try {
            FXMLLoader loader = OpenView.getInstance().openView("/dk/easv/easvexam/gui/AssignUsersView.fxml", event);
            AssignUsersController controller = loader.getController();
            controller.setTable(profile);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onSaveProfile(ActionEvent event) {
        try {
            selectedProfile.setName(txtName.getText());
            selectedProfile.setRotateDegrees(Integer.parseInt(txtRotation.getText()));
            int brightnessInt = (int) (sliderBrightness.getValue() * 100);
            selectedProfile.setBrightness(brightnessInt);
            logic.editProfile(selectedProfile);
            showSuccessNotification("Profile has been saved successfully!");
            tableProfiles.refresh();
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        } catch (NumberFormatException e) {
            OpenView.showErrorAlert("Rotation must be a number!");
        }
    }

    private void showSuccessNotification(String message) {
        lblStatus.setText(message);
        lblStatus.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 18px; -fx-padding: 0 15 0 0;");
        lblStatus.setOpacity(1.0);
        FadeTransition fade = new FadeTransition(Duration.seconds(1), lblStatus);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(Duration.seconds(4));
        fade.setOnFinished(e -> lblStatus.setText(""));
        fade.play();
    }
}
