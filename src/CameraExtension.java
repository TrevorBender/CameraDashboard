import edu.wpi.first.smartdashboard.camera.WPICameraExtension;
import edu.wpi.first.wpijavacv.*;
import edu.wpi.first.wpilibj.networking.NetworkTable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The camera extension will do the image processing to detect rectangles.
 * It will also be used on the driver station to display a HUD.
 */
public class CameraExtension extends WPICameraExtension {

    public static double HUE_MAX_THRESHOLD = .1255;
    public static double SATURATION_MIN_THRESHOLD = .17;
    public static double VALUE_MIN_THRESHOLD = .8;
    
    public static int doubleToInt(double val) {
        return (int) (val * 255);
    }
    public static double intToDouble(int value) {
        return (double) value / 255.0;
    }

    final PolygonFinder polygonFinder;

    public CameraExtension() {
        polygonFinder = new PolygonFinder();
    }

    @Override
    public void init() {
        super.init();
        {
            final DefaultBoundedRangeModel brm = new DefaultBoundedRangeModel(doubleToInt(HUE_MAX_THRESHOLD), 0, 0, 255);
            brm.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    HUE_MAX_THRESHOLD = intToDouble(brm.getValue());
                }
            });
            JSlider hueMaxSlider = new JSlider(brm);
            add(hueMaxSlider);
        }
    }

    /**
     * This method is called to process a new image from the camera.
     *
     * @param rawImage the original image
     * @return the processed image
     */
    @Override
    public WPIImage processImage(WPIColorImage rawImage) {
        BufferedImage bufferedImage = rawImage.getBufferedImage();
        BufferedImage outputImage = new BufferedImage(rawImage.getWidth(),rawImage.getHeight(),BufferedImage.TYPE_INT_RGB);
        WPIBinaryImage binaryImage = threshold(bufferedImage, outputImage);

        List<Polygon> rectangles = detectRectangles(outputImage, binaryImage);
        // test network table communication
        NetworkTable networkTable = NetworkTable.getTable("Camera");
        try {
            String s = networkTable.getString("hello");
            outputImage.getGraphics().drawString(s,10,10);
        } catch (NoSuchElementException ignored) {
        }
        return new WPIImage(outputImage);
    }

    private List<Polygon> detectRectangles(BufferedImage outputImage, WPIBinaryImage binaryImage) {
        polygonFinder.clear();
        List<Polygon> polygons = polygonFinder.findPolygons(binaryImage);

        List<Polygon> independentPolygons = new ArrayList<Polygon>();
        for (Polygon polygon : polygons) {
            boolean insideOtherPoly = false;
            for (Polygon otherPolygon : polygons) {
                insideOtherPoly = otherPolygon.contains(polygon.getBounds2D());
                if (insideOtherPoly) {
                    System.out.println("detected poly inside other");
                    break;
                }
            }
            Graphics graphics = outputImage.getGraphics();
            if (!insideOtherPoly) {
                independentPolygons.add(polygon);
                graphics.setColor(Color.RED);
                graphics.drawPolygon(polygon);
                double distance = calculateDistance(polygon);
                graphics.setColor(Color.BLUE);
                graphics.drawString(String.format("distance = %.2f", distance), 20, 20);
            } else {
                graphics.setColor(Color.GREEN);
                graphics.drawPolygon(polygon);
            }
        }
        return independentPolygons;
    }

    private WPIBinaryImage threshold(BufferedImage bufferedImage, BufferedImage outputImage) {
        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
                int rgb = bufferedImage.getRGB(i,j);
                Color color = new Color(rgb);
                float[] hsv = new float[3];
                Color.RGBtoHSB(color.getRed(),color.getGreen(),color.getBlue(),hsv);
                if (hsv[0] < HUE_MAX_THRESHOLD && hsv[1] > SATURATION_MIN_THRESHOLD && hsv[2] > VALUE_MIN_THRESHOLD) {
                    outputImage.setRGB(i,j,0xffffff);
                } else {
                    outputImage.setRGB(i,j,0x000000);
                }
            }
        }
        // I only do this since I can't directly create a binary image; it has no public constructor.
        WPIColorImage threshholdImage = new WPIColorImage(outputImage);
        return threshholdImage.getRedChannel().getThreshold(0);
    }

    private static final double fovInDegrees = 56.0 / 2.0; // we only consider half the fov to make the math simpler
    private static final double fovInRadians = fovInDegrees * Math.PI / 180.0;
    private static final double cameraImageWidthInPx = 640.0;

    private double calculateDistance(Polygon polygon) {
        // the fov of the camera is 56 deg.
        // the resolution of the camera is 640x480 px
        // the rectangle target width is 2 ft
        // the rest is math and geometry!

        // target width in px
        int width = (int) polygon.getBounds2D().getWidth();
        double distance = cameraImageWidthInPx / (width * Math.tan(fovInRadians));
        System.out.printf("distance = %.2f ft\n", distance);
        return distance;
    }
}
