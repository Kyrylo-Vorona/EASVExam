package dk.easv.easvexam.gui;

import dk.easv.easvexam.bll.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
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
                        System.out.println("Success");
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
    protected void onRotateButtonClick() {
        if (imageView.getImage() != null) {
            imageView.setRotate(imageView.getRotate() + 90);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
