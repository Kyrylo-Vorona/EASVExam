package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.Profile;
import dk.easv.easvexam.bll.Logic;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
    private CheckBox chkBarcode;
    @FXML
    private ImageView imagePreview;
    @FXML
    private ScrollPane scrollPane;

    private Logic logic = Logic.getInstance();
    private ObservableList<Profile> profileObservableList = FXCollections.observableArrayList();
    private Profile selectedProfile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        setupActionsColumn();
        refreshTable();
        tableProfiles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showProfileDetails(newValue);
            }
        });
        ColorAdjust colorAdjust = new ColorAdjust();
        imagePreview.setEffect(colorAdjust);

        sliderBrightness.valueProperty().addListener((obs, oldVal, newVal) -> {
            colorAdjust.setBrightness(newVal.doubleValue());
            lblBrightnessValue.setText((int)(newVal.doubleValue() * 100) + "%");
        });

        tableProfiles.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newValue) -> {
            if (newValue != null) {
                showProfileDetails(newValue);
            }
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
        chkBarcode.setSelected(profile.isSplitByBarcode());
    }

    private void applyEffects() {
        if (imagePreview.getImage() == null) return;

        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(sliderBrightness.getValue());

        try {
            double rotation = Double.parseDouble(txtRotation.getText());
            imagePreview.setRotate(rotation);
        } catch (NumberFormatException e) {

        }

        imagePreview.setEffect(colorAdjust);
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

    @FXML
    public void onSaveProfile(ActionEvent event) {
        if (selectedProfile == null) {
            OpenView.showErrorAlert("Please select a profile to save.");
            return;
        }
        try {
            selectedProfile.setName(txtName.getText());
            selectedProfile.setRotateDegrees(Integer.parseInt(txtRotation.getText()));
            int brightnessInt = (int) (sliderBrightness.getValue() * 100);
            selectedProfile.setBrightness(brightnessInt);
            selectedProfile.setSplitByBarcode(chkBarcode.isSelected());
            logic.editProfile(selectedProfile);
            tableProfiles.refresh();
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        } catch (NumberFormatException e) {
            OpenView.showErrorAlert("Rotation must be a number!");
        }
    }
}
