package org.example.tests;
import org.example.components.ImageComponent;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Image Viewer Component");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            String url = "https://upload.wikimedia.org/wikipedia/commons/4/47/PNG_transparency_demonstration_1.png";
            ImageComponent imageComponent = new ImageComponent(url);

            frame.add(imageComponent);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}