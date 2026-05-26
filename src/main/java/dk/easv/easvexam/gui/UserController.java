package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.Document;
import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.Profile;
import dk.easv.easvexam.be.User;
import dk.easv.easvexam.bll.ApiService;
import dk.easv.easvexam.bll.BarcodeService;
import dk.easv.easvexam.bll.Logic;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import java.awt.*;
import javafx.scene.input.KeyEvent;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javax.imageio.IIOImage;

import java.awt.image.RescaleOp;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import javax.imageio.ImageWriter;
import java.io.InputStream;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class UserController implements Initializable {
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
    @FXML
    private TextField txtBoxId;
    @FXML
    private TextField txtClient;
    @FXML
    private TextField txtCase;
    @FXML
    private CheckBox chkMultiPage;
    @FXML
    private ComboBox<String> comboStatus;
    @FXML
    private TreeView<String> docTreeView;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblTotalScans;

    private TreeItem<String> rootItem = new TreeItem<>("Scanned Items");
    private double zoomFactor = 1.0;
    private ColorAdjust colorAdjust = new ColorAdjust();
    private User currentUser;
    private Logic logic = Logic.getInstance();
    private Document currentDocument = new Document();
    private List<Document> allScannedDocuments = new ArrayList<>();
    private BarcodeService barcodeService = new BarcodeService();
    private Map<TreeItem<String>, BufferedImage> treeImageMap = new HashMap<>();

    private void showSelectedPage(TreeItem<String> selectedItem) {
        BufferedImage bufferedImage = treeImageMap.get(selectedItem);
        if (bufferedImage != null) {
            javafx.scene.image.Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setImage(image);

            double viewWidth = scrollPane.getViewportBounds().getWidth();
            double viewHeight = scrollPane.getViewportBounds().getHeight();
            imageView.setFitWidth(viewWidth - 10);
            imageView.setFitHeight(viewHeight - 10);
        }
    }

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
                        String barcodeValue = barcodeService.detectBarcode(bufferedImage);

                        if (barcodeValue != null) {
                            if (!currentDocument.getPages().isEmpty()) {
                                allScannedDocuments.add(currentDocument);
                            }
                            currentDocument = new Document();
                            currentDocument.setBarcode(barcodeValue);

                            TreeItem<String> docItem = new TreeItem<>("Document [" + barcodeValue + "]");
                            rootItem.getChildren().add(docItem);

                            TreeItem<String> pageItem = new TreeItem<>("Page " + (docItem.getChildren().size() + 1));
                            docItem.getChildren().add(pageItem);
                            treeImageMap.put(pageItem, bufferedImage);
                        } else {
                            if (rootItem.getChildren().isEmpty()) {
                                TreeItem<String> defaultDoc = new TreeItem<>("Document [No Barcode]");
                                rootItem.getChildren().add(defaultDoc);
                            }
                            TreeItem<String> lastDoc = rootItem.getChildren().get(rootItem.getChildren().size() - 1);
                            TreeItem<String> pageItem = new TreeItem<>("Page " + (lastDoc.getChildren().size() + 1));
                            lastDoc.getChildren().add(pageItem);
                            treeImageMap.put(pageItem, bufferedImage);
                            logic.logActivity(currentUser.getId(), "Scanned a new document page from API.");
                        }

                        currentDocument.addPage(bufferedImage);
                        javafx.scene.image.Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                        imageView.fitWidthProperty().unbind();
                        imageView.fitHeightProperty().unbind();
                        imageView.setImage(image);
                        imageView.setPreserveRatio(true);
                        imageView.setRotate(0);
                        imageView.setScaleX(1.0);
                        imageView.setScaleY(1.0);
                        zoomFactor = 1.0;
                        Profile selected = comboProfiles.getValue();
                        if (selected != null) {
                            imageView.setRotate(selected.getRotateDegrees());
                            double brightnessVal = selected.getBrightness() / 100.0;
                            applyBrightness(brightnessVal);
                            brightnessSlider.setValue(brightnessVal);
                        }
                        double viewWidth = scrollPane.getViewportBounds().getWidth();
                        double viewHeight = scrollPane.getViewportBounds().getHeight();
                        if (viewWidth <= 0) viewWidth = scrollPane.getWidth();
                        if (viewHeight <= 0) viewHeight = scrollPane.getHeight();
                        imageView.setFitWidth(viewWidth - 10);
                        imageView.setFitHeight(viewHeight - 10);
                        imageContainer.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                        scrollPane.setVvalue(0.0);
                        scrollPane.setHvalue(0.0);
                        updateTotalScansCount();
                    } else {
                        OpenView.showErrorAlert("Error: file inside of ZIP is not an image or crashed");
                    }
                } else {
                    OpenView.showErrorAlert("Error: ZIP-Archive is empty");
                }
            }
        } catch (Exception e) {
            OpenView.showErrorAlert("Error: " + e);
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadUserProfiles();
    }

    private void loadUserProfiles() {
        try {
            if (currentUser != null) {
                List<Profile> userProfiles = logic.getProfilesForUser(currentUser.getId());
                int standardProfileId = 11;
                boolean hasStandard = false;
                for (Profile p : userProfiles) {
                    if (p.getId() == standardProfileId) {
                        hasStandard = true;
                        break;
                    }
                }
                if (!hasStandard) {
                    List<Profile> allProfiles = logic.getAllProfiles();
                    for (Profile p : allProfiles) {
                        if (p.getId() == standardProfileId) {
                            userProfiles.add(p);
                            break;
                        }
                    }
                }
                comboProfiles.setItems(FXCollections.observableArrayList(userProfiles));
                for (Profile p : comboProfiles.getItems()) {
                    if (p.getId() == standardProfileId) {
                        comboProfiles.getSelectionModel().select(p);
                        break;
                    }
                }
                if (comboProfiles.getSelectionModel().getSelectedItem() == null && !comboProfiles.getItems().isEmpty()) {
                    comboProfiles.getSelectionModel().selectFirst();
                }
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

        docTreeView.setRoot(rootItem);
        rootItem.setExpanded(true);
        docTreeView.setShowRoot(false);
        docTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isLeaf()) {
                showSelectedPage(newValue);
            }
        });

        comboStatus.setItems(FXCollections.observableArrayList("In Progress", "Waiting for QA", "Completed"));
        comboStatus.setValue("In Progress");
        Platform.runLater(this::initKeyboardShortcuts);
        updateTotalScansCount();
    }

    private void applyBrightness(double value) {
        colorAdjust.setBrightness(value);
    }

    @FXML
    private void moveSelectedUp() {
        TreeItem<String> selected = docTreeView.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getParent() != null) {
            TreeItem<String> parent = selected.getParent();
            int index = parent.getChildren().indexOf(selected);
            if (index > 0) {
                parent.getChildren().remove(selected);
                parent.getChildren().add(index - 1, selected);
                docTreeView.getSelectionModel().select(selected);
            }
        }
    }

    @FXML
    private void moveSelectedDown() {
        TreeItem<String> selected = docTreeView.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getParent() != null) {
            TreeItem<String> parent = selected.getParent();
            int index = parent.getChildren().indexOf(selected);
            if (index >= 0 && index < parent.getChildren().size() - 1) {
                parent.getChildren().remove(selected);
                parent.getChildren().add(index + 1, selected);
                docTreeView.getSelectionModel().select(selected);
            }
        }
    }

    @FXML
    private void deleteSelected() {
        TreeItem<String> selected = docTreeView.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getParent() != null) {
            TreeItem<String> parent = selected.getParent();
            parent.getChildren().remove(selected);
            treeImageMap.remove(selected);
            logic.logActivity(currentUser.getId(), "Deleted item: " + selected.getValue());
            imageView.setImage(null);
            updateTotalScansCount();
        }
    }

    private BufferedImage applyProfileTransformations(BufferedImage src, double rotateDegrees, double brightnessValue) {
        if (src == null) return null;
        int w = src.getWidth();
        int h = src.getHeight();
        double absDegrees = Math.abs(rotateDegrees % 360);
        boolean isSwapped = (absDegrees == 90 || absDegrees == 270);
        int newW = isSwapped ? h : w;
        int newH = isSwapped ? w : h;
        BufferedImage modified = new BufferedImage(newW, newH, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        Graphics2D g2d = modified.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.translate(newW / 2.0, newH / 2.0);
        g2d.rotate(Math.toRadians(rotateDegrees));
        g2d.translate(-w / 2.0, -h / 2.0);
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();

        if (brightnessValue != 0.0) {
            float offset = (float) (brightnessValue * 255.0);
            RescaleOp rescaleOp = new RescaleOp(1.0f, offset, null);
            try {
                modified = rescaleOp.filter(modified, null);
            } catch (Exception e) {
                OpenView.showErrorAlert("Brightness filter warning: " + e.getMessage());
            }
        }
        return modified;
    }

    private void saveMultiPageTiff(List<BufferedImage> images, File outputFile) throws Exception {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("TIFF");
        if (!writers.hasNext()) {
            throw new MyException("TIFF Writer not found in this JVM!", null);
        }

        ImageWriter writer = writers.next();
        try (javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);

            for (BufferedImage img : images) {
                writer.writeToSequence(new IIOImage(img, null, null), null);
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
    }

    @FXML
    private void onExportButtonClick() {
        String boxId = txtBoxId.getText().trim();
        String client = txtClient.getText().trim();
        String caseName = txtCase.getText().trim();
        Profile selectedProfile = comboProfiles.getValue();
        String currentStatus = comboStatus.getValue();
        if (boxId.isEmpty() || client.isEmpty() || caseName.isEmpty() || selectedProfile == null) {
            OpenView.showErrorAlert("Validation Error: Please fill in Box ID, Client, Case and select a Profile before exporting!");
            return;
        }
        try {
            File exportFolder = new File("Export");
            File clientFolder = new File(exportFolder, client);
            File caseFolder = new File(clientFolder, caseName);
            File boxFolder = new File(caseFolder, "Export_" + boxId);
            if (!boxFolder.exists()) {
                boxFolder.mkdirs();
            }
            double currentRotation = imageView.getRotate();
            double currentBrightness = brightnessSlider.getValue();
            for (TreeItem<String> docNode : rootItem.getChildren()) {
                String docName = docNode.getValue();
                String cleanDocName = docName.replace("Document [", "").replace("]", "").replace(" ", "_");
                String cleanProfileName = selectedProfile.getName().replace(" ", "_");
                String baseDocumentName = cleanProfileName + "_" + boxId + "_" + cleanDocName;
                List<String> savedFilePaths = new ArrayList<>();
                List<BufferedImage> docImages = new ArrayList<>();
                for (TreeItem<String> pageNode : docNode.getChildren()) {
                    BufferedImage rawImg = treeImageMap.get(pageNode);
                    if (rawImg != null) {
                        BufferedImage affectedImg = applyProfileTransformations(rawImg, currentRotation, currentBrightness);
                        docImages.add(affectedImg);
                    }
                }
                if (docImages.isEmpty()) {
                    continue;
                }
                if (chkMultiPage.isSelected()) {
                    File tiffFile = new File(boxFolder, baseDocumentName + ".tiff");
                    saveMultiPageTiff(docImages, tiffFile);
                    savedFilePaths.add(tiffFile.getPath());
                } else {
                    File docFolder = new File(boxFolder, baseDocumentName);
                    if (!docFolder.exists()) {
                        docFolder.mkdir();
                    }
                    int pageIndex = 1;
                    for (BufferedImage img : docImages) {
                        File pageFile = new File(docFolder, "Page_" + pageIndex + ".tiff");
                        ImageIO.write(img, "TIFF", pageFile);
                        savedFilePaths.add(pageFile.getPath());
                        pageIndex++;
                    }
                }
                try {
                    logic.saveDocumentToDb(boxId, client, caseName, selectedProfile.getId(), currentUser.getId(), currentStatus, savedFilePaths);
                    logic.logActivity(currentUser.getId(), "Exported Document: " + baseDocumentName);
                    showSuccessNotification("Export completed successfully!");
                } catch (Exception dbEx) {
                    OpenView.showErrorAlert("Database save failed: " + dbEx.getMessage());
                }
            }

        } catch (Exception e) {
            OpenView.showErrorAlert("Wrong username or password" + e);
        }
    }

    private void initKeyboardShortcuts() {
        Scene scene = docTreeView.getScene();
        if (scene == null) return;

        scene.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            boolean isCtrlDown = event.isControlDown() || event.isMetaDown();

            if (isCtrlDown && event.getCode() == KeyCode.S) {
                onScanButtonClick();
                event.consume();
            }

            else if (isCtrlDown && event.getCode() == KeyCode.E) {
                onExportButtonClick();
                event.consume();
            }

            else if (isCtrlDown && (event.getCode() == KeyCode.EQUALS || event.getCode() == KeyCode.PLUS)) {
                onZoomIn();
                scrollPane.requestFocus();
                event.consume();
            }

            else if (isCtrlDown && event.getCode() == KeyCode.MINUS) {
                onZoomOut();
                scrollPane.requestFocus();
                event.consume();
            }
            double minSlider = brightnessSlider.getMin();
            double maxSlider = brightnessSlider.getMax();
            double step = (maxSlider - minSlider) * 0.05;
            if (isCtrlDown && event.getCode() == KeyCode.M) {
                double current = brightnessSlider.getValue();
                brightnessSlider.setValue(Math.min(maxSlider, current + step));
                event.consume();
            }
            else if (isCtrlDown && event.getCode() == KeyCode.L) {
                double current = brightnessSlider.getValue();
                brightnessSlider.setValue(Math.max(minSlider, current - step));
                event.consume();
            }
        });

        scrollPane.setOnKeyPressed((KeyEvent event) -> {
            double scrollStep = 0.05;
            if (event.getCode() == KeyCode.DOWN) {
                scrollPane.setVvalue(scrollPane.getVvalue() + scrollStep);
                event.consume();
            } else if (event.getCode() == KeyCode.UP) {
                scrollPane.setVvalue(scrollPane.getVvalue() - scrollStep);
                event.consume();
            } else if (event.getCode() == KeyCode.RIGHT) {
                scrollPane.setHvalue(scrollPane.getHvalue() + scrollStep);
                event.consume();
            } else if (event.getCode() == KeyCode.LEFT) {
                scrollPane.setHvalue(scrollPane.getHvalue() - scrollStep);
                event.consume();
            }
        });
    }

    private void showSuccessNotification(String message) {
        lblStatus.setText(message);
        lblStatus.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 0 15 0 0;");
        lblStatus.setOpacity(1.0);
        FadeTransition fade = new FadeTransition(Duration.seconds(1), lblStatus);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(Duration.seconds(4));
        fade.setOnFinished(e -> lblStatus.setText(""));
        fade.play();
    }

    private void updateTotalScansCount() {
        if (lblTotalScans != null) {
            int totalScans = treeImageMap.size();
            lblTotalScans.setText("Total Scans: " + totalScans);
        }
    }
}
