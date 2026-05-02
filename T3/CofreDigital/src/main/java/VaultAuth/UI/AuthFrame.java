package VaultAuth.UI;

import javax.swing.*;


public class AuthFrame extends JFrame {
    private JPanel panel;

    public AuthFrame(String title) {
        super(title);
        panel = new LoginPanel();
        setPanel(panel);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
        setContentPane(panel);
        repaint();
        revalidate();
    }
}
