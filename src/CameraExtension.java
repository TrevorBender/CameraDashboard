import com.googlecode.javacv.JavaCV;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import edu.wpi.first.smartdashboard.camera.WPICameraExtension;
import edu.wpi.first.wpijavacv.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Iterator;

/**
 */
public class CameraExtension extends WPICameraExtension {

    public static final double HUE_MAX_THRESHOLD = .18;
    public static final double SATURATION_MIN_THRESHOLD = .17;
    public static final double VALUE_MIN_THRESHOLD = .8;
    private final PolygonFinder polygonFinder;

    public CameraExtension() {
        polygonFinder = new PolygonFinder();
    }

    @Override
    public WPIImage processImage(WPIColorImage rawImage) {
        BufferedImage bufferedImage = rawImage.getBufferedImage();
        BufferedImage outputImage = new BufferedImage(rawImage.getWidth(),rawImage.getHeight(),BufferedImage.TYPE_INT_RGB);
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
        // I only do this since I can't directly create a binary image.
        WPIColorImage threshholdImage = new WPIColorImage(outputImage);
        WPIBinaryImage binaryImage = threshholdImage.getRedChannel().getThreshold(0);

        polygonFinder.clear();
        int numPoly = polygonFinder.findPolygons(binaryImage);

        for(int i = 0; i < numPoly; i++) {
            Points polygon = polygonFinder.getPolygon(i);
            polygon.drawFilledPolygon(outputImage.getGraphics());
        }
        return new WPIImage(outputImage);
    }

    private WPIBinaryImage threshold(int min,int max,WPIGrayscaleImage channel) {
        WPIBinaryImage threshold = channel.getThreshold(min);
        threshold.and(channel.getThresholdInverted(max));
        return threshold;
    }

    public static void main(String[] args) throws IOException, InvocationTargetException, InterruptedException {
        String[] images = new String[] {
                "RectangleRed4ft.jpg",
                "Rectanglebelow.jpg",
                "rectanglebelow2.jpg",
                "rectanglebelow3.jpg",
                "rectanglebelow4.jpg",
                "rectanglefar4.jpg"
        };
        final CameraExtension cameraExtension = new CameraExtension();
        final CameraPreviewFrame cameraPreviewFrame = new CameraPreviewFrame();
//        URL url = new URL("http://10.14.50.11/mjpg/video.mjpg");
//        try {
//            BufferedImage liveImage = ImageIO.read(url.openStream());
//            cameraPreviewFrame.setLiveImage(liveImage);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        for (final String image : images) {
            cameraExtension.polygonFinder.clear();
            final WPIColorImage originalImage = new WPIColorImage(ImageIO.read(new File(image)));
            final WPIImage thresholdImage = cameraExtension.processImage(originalImage);

            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    JFrame frame = new JFrame("Preview Window: " + image);
                    cameraPreviewFrame.setStillImage(thresholdImage.getBufferedImage());
                    cameraPreviewFrame.setOriginalImage(originalImage.getBufferedImage());
                    frame.getContentPane().add(cameraPreviewFrame.getContentPane());
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.pack();
                    frame.setVisible(true);
                    System.out.println("Polygons for: " + image);
                    Iterator<Points> iterator = cameraExtension.polygonFinder.iterator();
                    while (iterator.hasNext()) {
                        Points poly = iterator.next();
                        System.out.println(poly);
                        poly.drawPolygonOutline(originalImage.getBufferedImage().getGraphics());
                    }
                }
            });
        }
    }
    
}
