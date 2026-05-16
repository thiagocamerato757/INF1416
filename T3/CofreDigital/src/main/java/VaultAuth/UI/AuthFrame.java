package VaultAuth.UI;

import javax.swing.*;
import java.awt.*;

public class AuthFrame extends JFrame {
    private JPanel panel;

    public AuthFrame(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(560, 360));
        panel = new LoginPanel();
        setPanel(panel);
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
        setContentPane(panel);
        pack();
        Dimension min = getMinimumSize();
        if (getWidth() < min.width || getHeight() < min.height) {
            setSize(Math.max(getWidth(), min.width), Math.max(getHeight(), min.height));
        }
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }
}
