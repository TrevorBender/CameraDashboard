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
import java.net.URL;

/**
 */
public class CameraExtension extends WPICameraExtension {

    @Override
    public WPIImage processImage(WPIColorImage rawImage) {
//        IplImage iplImage = new WpiImageWrapper(rawImage).getImage();
//        cvConvertImage(iplImage, iplImage, CV_RGB2HSV_FULL);
//        return new WPIImage(iplImage);
//        BufferedImage bufferedImage = rawImage.getBufferedImage();

//        threshholdImage = image.thresholdRGB(25, 255, 0, 45, 0, 47);
        BufferedImage bufferedImage = rawImage.getBufferedImage();
        BufferedImage outputImage = new BufferedImage(rawImage.getWidth(),rawImage.getHeight(),BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
                int rgb = bufferedImage.getRGB(i,j);
                Color color = new Color(rgb);
                float[] hsv = new float[3];
                Color.RGBtoHSB(color.getRed(),color.getGreen(),color.getBlue(),hsv);
                if (hsv[0] < .15 && hsv[2] > .9) {
                    outputImage.setRGB(i,j,-1);
                } else {
                   outputImage.setRGB(i,j,0);
                }
            }
        }
        return new WPIColorImage(outputImage);

//        WPIBinaryImage redThresh = threshold(200, 255,rawImage.getRedChannel());
//        WPIBinaryImage greenThresh = threshold(0, 45, rawImage.getGreenChannel());
//        WPIBinaryImage blueThresh = threshold(0,47, rawImage.getBlueChannel());
//        redThresh.and(greenThresh);
//        redThresh.and(blueThresh);
//
//        return redThresh;
    }

    private WPIBinaryImage threshold(int min,int max,WPIGrayscaleImage channel) {
        WPIBinaryImage threshold = channel.getThreshold(min);
        threshold.and(channel.getThresholdInverted(max));
        return threshold;
    }

    public static void main(String[] args) throws IOException {
        String[] images = new String[] {
                "RectangleRed4ft.jpg",
                "Rectanglebelow.jpg",
                "rectanglebelow2.jpg",
                "rectanglebelow3.jpg",
                "rectanglebelow4.jpg",
                "rectanglefar4.jpg"
        };
        CameraExtension cameraExtension = new CameraExtension();
        CameraPreviewFrame cameraPreviewFrame = new CameraPreviewFrame();
//        URL url = new URL("http://10.14.50.11/mjpg/video.mjpg");
//        try {
//            BufferedImage liveImage = ImageIO.read(url.openStream());
//            cameraPreviewFrame.setLiveImage(liveImage);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        for (String image : images) {
            WPIColorImage originalImage = new WPIColorImage(ImageIO.read(new File(image)));
            WPIImage thresholdImage = cameraExtension.processImage(originalImage);

            JFrame frame = new JFrame("Preview Window: " + image);
            cameraPreviewFrame.setStillImage(thresholdImage.getBufferedImage());
            cameraPreviewFrame.setOriginalImage(originalImage.getBufferedImage());
            frame.getContentPane().add(cameraPreviewFrame.getContentPane());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        }
    }
    
}
