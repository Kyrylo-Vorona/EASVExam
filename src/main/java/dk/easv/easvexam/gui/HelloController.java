package dk.easv.easvexam.gui;

import dk.easv.easvexam.be.Document;
import dk.easv.easvexam.be.MyException;
import dk.easv.easvexam.be.Profile;
import dk.easv.easvexam.be.User;
import dk.easv.easvexam.bll.ApiService;
import dk.easv.easvexam.bll.BarcodeService;
import dk.easv.easvexam.bll.Logic;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javax.imageio.IIOImage;
import javafx.util.StringConverter;

import java.awt.image.RescaleOp;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
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
            imageView.setImage(null);
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
                System.err.println("Brightness filter warning: " + e.getMessage());
            }
        }
        return modified;
    }

    private void saveMultiPageTiff(List<BufferedImage> images, File outputFile) throws Exception {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("TIFF");
        if (!writers.hasNext()) {
            throw new RuntimeException("TIFF Writer not found in this JVM!");
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
        if (boxId.isEmpty() || client.isEmpty() || caseName.isEmpty() || selectedProfile == null || currentUser == null) {
            System.err.println("Error: Missing required fields for export (Box ID, Client, Case, Profile or User)!");
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
                String baseDocumentName = selectedProfile.getName() + "_" + boxId + "_" + cleanDocName;
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
                } catch (Exception dbEx) {
                    System.err.println("Database save failed: " + dbEx.getMessage());
                }
            }
            System.out.println("Export with structured Client/Case metadata completed successfully!");

        } catch (Exception e) {
            System.err.println("Export failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
