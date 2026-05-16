package VaultAuth.UI;

import logger.Logger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AuthFrame extends JFrame {
    private JPanel panel;

    public AuthFrame(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Logger.log(1002);
                System.exit(0);
            }
        });
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
