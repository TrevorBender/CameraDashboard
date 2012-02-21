import edu.wpi.first.smartdashboard.camera.WPICameraExtension;
import edu.wpi.first.wpijavacv.*;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The camera extension will do the image processing to detect rectangles.
 * It will also be used on the driver station to display a HUD.
 */
public class CameraExtension extends WPICameraExtension {

    public static final double HUE_MAX_THRESHOLD = .18;
    public static final double SATURATION_MIN_THRESHOLD = .17;
    public static final double VALUE_MIN_THRESHOLD = .8;

    final PolygonFinder polygonFinder;

    public CameraExtension() {
        polygonFinder = new PolygonFinder();
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

        detectRectangles(outputImage, binaryImage);
        return new WPIImage(outputImage);
    }

    private void detectRectangles(BufferedImage outputImage, WPIBinaryImage binaryImage) {
        polygonFinder.clear();
        int numPoly = polygonFinder.findPolygons(binaryImage);

        for(int i = 0; i < numPoly; i++) {
            Points polygon = polygonFinder.getPolygon(i);
            polygon.drawPolygonOutline(outputImage.getGraphics());
            calculateDistances(polygon);
        }
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

    private void calculateDistances(Points polygon) {
        //todo: calculate distances using fov
        // the fov of the camera is 56 deg.
    }
}
