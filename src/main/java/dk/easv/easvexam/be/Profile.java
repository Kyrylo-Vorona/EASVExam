package dk.easv.easvexam.be;

public class Profile {
    private int id;
    private String name;
    private int rotateDegrees;
    private int brightness;
    private boolean splitByBarcode;

    public Profile(int id, String name, int rotateDegrees, int brightness, boolean splitByBarcode) {
        this.id = id;
        this.name = name;
        this.rotateDegrees = rotateDegrees;
        this.brightness = brightness;
        this.splitByBarcode = splitByBarcode;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getRotateDegrees() {
        return rotateDegrees;
    }
    public void setRotateDegrees(int rotateDegrees) {
        this.rotateDegrees = rotateDegrees;
    }
    public int getBrightness() {
        return brightness;
    }
    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }
    public boolean isSplitByBarcode() {
        return splitByBarcode;
    }
    public void setSplitByBarcode(boolean splitByBarcode) {
        this.splitByBarcode = splitByBarcode;
    }
}
