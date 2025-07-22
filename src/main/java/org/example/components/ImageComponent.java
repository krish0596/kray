package org.example.components;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

public class ImageComponent extends JPanel {
    public ImageComponent(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);

            if (image == null) {
                throw new Exception("Image couldn't be loaded or format not supported.");
            }

            JLabel label = new JLabel(new ImageIcon(image));
            this.add(label);
        } catch (Exception e) {
            e.printStackTrace();
            JLabel errorLabel = new JLabel("Failed to load image.");
            this.add(errorLabel);
        }
    }
}
