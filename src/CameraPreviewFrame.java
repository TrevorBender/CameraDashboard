import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
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
}
