package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.bll.ApiService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.StackPane;

import java.io.InputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;
import java.io.InputStream;

public class HelloController {

    @FXML
    private ImageView imageView;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private StackPane imageContainer;
    private double zoomFactor = 1.0;
    OpenView openview = new OpenView();

    @FXML
    protected void onScanButtonClick() {
        ApiService apiService = new ApiService();

        try {
            InputStream stream = apiService.fetchRandomTiff();
            try (ZipInputStream zipIn = new ZipInputStream(stream)) {
                ZipEntry entry = zipIn.getNextEntry();

                if (entry != null) {
                    byte[] bytes = zipIn.readAllBytes();
                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));

                    if (bufferedImage != null) {
                        javafx.scene.image.Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                        imageView.setImage(image);
                        zoomFactor = 1.0;
                        double viewWidth = scrollPane.getViewportBounds().getWidth();
                        imageView.setFitWidth(viewWidth * 0.8);
                        applyZoom();
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
        zoomFactor += 0.1;
        applyZoom();
    }

    @FXML
    protected void onZoomOut() {
        if (zoomFactor > 0.4) {
            zoomFactor -= 0.2;
            applyZoom();
        }
    }

    private void applyZoom() {
        imageView.setScaleX(zoomFactor);
        imageView.setScaleY(zoomFactor);
        imageContainer.setPrefSize(
                imageView.getBoundsInParent().getWidth(),
                imageView.getBoundsInParent().getHeight()
        );

        scrollPane.layout();
    }

    @FXML
    protected void onRotateButtonClick() {
        if (imageView.getImage() != null) {
            imageView.setRotate(imageView.getRotate() + 90);
        }
    }

    public void logOut(ActionEvent event) {
        try {
            String filepath = "/dk/easv/easvexam/gui/LoginView.fxml";
            openview.openView(filepath, event);
        } catch (MyException e) {
            OpenView.showErrorAlert(e.getMessage());
        }
    }
}
