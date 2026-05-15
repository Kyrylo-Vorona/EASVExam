package dk.easv.easvexam.be;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Document {
    private int id;
    private String status = "In Progress";
    private List<BufferedImage> pages = new ArrayList<>();
    private String barcode;

    public void addPage(BufferedImage img) { this.pages.add(img); }

    public List<BufferedImage> getPages() { return this.pages; }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBarcode() {
        return barcode;
    }
}
