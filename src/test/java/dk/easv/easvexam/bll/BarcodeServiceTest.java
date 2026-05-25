package dk.easv.easvexam.bll;

import dk.easv.easvexam.bll.BarcodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class BarcodeServiceTest {
    private BarcodeService barcodeService;

    @BeforeEach
    void setUp() {
        barcodeService = new BarcodeService();
    }

    @Test
    void testDetectBarcodeReturnsNullWhenNoBarcodePresent() {
        BufferedImage blankImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        String result = barcodeService.detectBarcode(blankImage);
        assertNull(result, "If there is no barcode, the method should return null.");
    }
}