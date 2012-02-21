import edu.wpi.first.wpijavacv.WPIColorImage;
import edu.wpi.first.wpijavacv.WPIImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

/**
 * This requires IntelliJ to build.
 */
public class CameraPreviewFrame {
    private JLabel originalImageIcon;
    private JLabel stillImageIcon;
    private JPanel main;

    public CameraPreviewFrame() {
    }

    public Component getContentPane() {
        return main;
    }

    public void setOriginalImage(BufferedImage liveImage) {
        originalImageIcon.setIcon(new ImageIcon(liveImage));
    }

    public void setStillImage(BufferedImage bufferedImage) {
        stillImageIcon.setIcon(new ImageIcon(bufferedImage));
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
