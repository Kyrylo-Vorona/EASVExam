package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.bll.ApiService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.io.InputStream;
import java.net.URL;
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
    private ImageView imageView;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private StackPane imageContainer;
    private double zoomFactor = 1.0;
    private ColorAdjust colorAdjust = new ColorAdjust();

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
    }

    private void applyBrightness(double value) {
        colorAdjust.setBrightness(value);
    }
}
