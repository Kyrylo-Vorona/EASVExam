package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.Profile;
import dk.easv.easvexam.be.User;
import dk.easv.easvexam.bll.ApiService;
import dk.easv.easvexam.bll.Logic;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;
import java.io.InputStream;

public class HelloController implements Initializable {
    @FXML
    private Slider brightnessSlider;
    @FXML
    private TextField rotateField;
    @FXML
    private ComboBox<Profile> comboProfiles;
    @FXML
    private ImageView imageView;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private StackPane imageContainer;
    private double zoomFactor = 1.0;
    private ColorAdjust colorAdjust = new ColorAdjust();
    private User currentUser;
    private Logic logic = Logic.getInstance();

    @FXML
    protected void onScanButtonClick() {
        try {
            InputStream stream = ApiService.getInstance().fetchRandomTiff();
            try (ZipInputStream zipIn = new ZipInputStream(stream)) {
                ZipEntry entry = zipIn.getNextEntry();
                if (entry != null) {
                    byte[] bytes = zipIn.readAllBytes();
                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
                    if (bufferedImage != null) {
                        javafx.scene.image.Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                        imageView.fitWidthProperty().unbind();
                        imageView.fitHeightProperty().unbind();
                        imageView.setImage(image);
                        imageView.setPreserveRatio(true);
                        zoomFactor = 1.0;
                        imageView.setScaleX(1.0);
                        imageView.setScaleY(1.0);
                        imageView.setRotate(0);
                        colorAdjust.setBrightness(0.0);
                        brightnessSlider.setValue(0.0);
                        scrollPane.setVvalue(0.0);
                        scrollPane.setHvalue(0.0);
                        imageView.fitWidthProperty().bind(scrollPane.widthProperty());
                        imageView.fitHeightProperty().bind(scrollPane.heightProperty());
                        imageContainer.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                        Profile selected = comboProfiles.getValue();
                        if (selected != null) {
                            imageView.setRotate(selected.getRotateDegrees());
                            double brightnessVal = (selected.getBrightness() - 50) / 50.0;
                            applyBrightness(brightnessVal);
                            brightnessSlider.setValue(brightnessVal);
                        }
                    } else {
                        System.err.println("Error: file inside of ZIP is not an image or crashed");
                    }
                } else {
                    System.err.println("Error: ZIP-Archive is empty");
                }
            }
        } catch (Exception e) {
            System.err.println("Error " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadUserProfiles();
    }

    private void loadUserProfiles() {
        try {
            if (currentUser != null) {
                List profiles = logic.getProfilesForUser(currentUser.getId());
                comboProfiles.setItems(FXCollections.observableArrayList(profiles));
            }
        } catch (MyException e) {
            OpenView.showErrorAlert("Could not load profiles: " + e.getMessage());
        }
    }

    @FXML
    protected void onZoomIn() {
        imageView.fitWidthProperty().unbind();
        imageView.fitHeightProperty().unbind();
        zoomFactor += 0.1;
        applyZoom();
    }

    @FXML
    protected void onZoomOut() {
        if (zoomFactor > 0.4) {
            imageView.fitWidthProperty().unbind();
            imageView.fitHeightProperty().unbind();
            zoomFactor -= 0.1;
            applyZoom();
        }
    }

    private void applyZoom() {
        imageView.setScaleX(zoomFactor);
        imageView.setScaleY(zoomFactor);
        imageContainer.setPrefSize(imageView.getBoundsInParent().getWidth(), imageView.getBoundsInParent().getHeight());
        scrollPane.layout();
    }

    public void onRotateButtonClick() {
        if (imageView.getImage() != null) {
            try {
                String text = rotateField.getText();
                double degrees = text.isEmpty() ? 0 : Double.parseDouble(text);
                imageView.setRotate(imageView.getRotate() + degrees);
                rotateField.setStyle("");
            } catch (NumberFormatException e) {
                rotateField.setStyle("-fx-border-color: red;");
            }
        }
    }

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
        imageView.setEffect(colorAdjust);
        brightnessSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyBrightness(newValue.doubleValue());
        });
        comboProfiles.setConverter(new StringConverter<Profile>() {
            @Override
            public String toString(Profile profile) {
                return (profile == null) ? "" : profile.getName();
            }
            @Override
            public Profile fromString(String string) {
                return null;
            }
        });
    }

    private void applyBrightness(double value) {
        colorAdjust.setBrightness(value);
    }
}
