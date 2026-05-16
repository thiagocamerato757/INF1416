package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Utility class for standardizing and managing UI components in the Digital Vault application.
 */
public class UIUtils {

    // Color Palette Constants
    public static final Color COLOR_PRIMARY = new Color(41, 128, 185);
    public static final Color COLOR_PRIMARY_DARK = new Color(31, 97, 141);
    public static final Color COLOR_ACCENT = new Color(230, 126, 34);
    public static final Color COLOR_SUCCESS = new Color(46, 204, 113);
    public static final Color COLOR_DANGER = new Color(231, 76, 60);
    public static final Color COLOR_WARNING = new Color(241, 196, 15);
    public static final Color COLOR_BACKGROUND = new Color(236, 240, 241);
    public static final Color COLOR_TEXT = new Color(44, 62, 80);
    public static final Color COLOR_LIGHT = new Color(255, 255, 255);
    public static final Color COLOR_BORDER = new Color(189, 195, 199);

    // Font Definitions
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.PLAIN, 12);

    // Padding Constants
    public static final int PADDING_SMALL = 5;
    public static final int PADDING_MEDIUM = 10;
    public static final int PADDING_LARGE = 15;
    public static final int PADDING_XLARGE = 20;

    /**
     * Creates a standardized button with the default primary color.
     *
     * @param text The label text for the button.
     * @return A styled JButton instance.
     */
    public static JButton createButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, COLOR_PRIMARY);
        return button;
    }

    /**
     * Creates a standardized button with a custom background color.
     *
     * @param text The label text for the button.
     * @param color The background color for the button.
     * @return A styled JButton instance.
     */
    public static JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        styleButton(button, color);
        return button;
    }

    /**
     * Internal method to apply standard styling to a JButton.
     *
     * @param button The button to style.
     * @param color The background color to apply.
     */
    private static void styleButton(JButton button, Color color) {
        button.setFont(FONT_BUTTON);
        button.setBackground(color);
        button.setForeground(COLOR_LIGHT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(PADDING_MEDIUM, PADDING_LARGE, PADDING_MEDIUM, PADDING_LARGE));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(darkenColor(color, 0.1f));
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }

    /**
     * Creates a standardized single-line text field.
     *
     * @param columns The number of columns for the text field.
     * @return A styled JTextField instance.
     */
    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        field.setMargin(new Insets(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM));
        field.setBackground(COLOR_LIGHT);
        field.setForeground(COLOR_TEXT);
        return field;
    }

    /**
     * Creates a standardized password field for secure text input.
     *
     * @param columns The number of columns for the password field.
     * @return A styled JPasswordField instance.
     */
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        field.setMargin(new Insets(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM));
        field.setBackground(COLOR_LIGHT);
        field.setForeground(COLOR_TEXT);
        return field;
    }

    /**
     * Creates a standardized multi-line text area.
     *
     * @param rows The number of rows for the text area.
     * @param columns The number of columns for the text area.
     * @return A styled JTextArea instance.
     */
    public static JTextArea createTextArea(int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns);
        area.setFont(FONT_BODY);
        area.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        area.setMargin(new Insets(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM));
        area.setBackground(COLOR_LIGHT);
        area.setForeground(COLOR_TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    /**
     * Creates a standardized label with body font.
     *
     * @param text The text to display.
     * @return A styled JLabel instance.
     */
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(COLOR_TEXT);
        return label;
    }

    /**
     * Creates a title label with bold large font.
     *
     * @param text The title text to display.
     * @return A styled JLabel instance for headings.
     */
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(COLOR_PRIMARY_DARK);
        return label;
    }

    /**
     * Creates a subtitle label with bold font.
     *
     * @param text The subtitle text to display.
     * @return A styled JLabel instance for subheadings.
     */
    public static JLabel createSubtitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBTITLE);
        label.setForeground(COLOR_PRIMARY);
        return label;
    }

    /**
     * Creates a standardized combo box.
     *
     * @param items The items to populate the combo box.
     * @return A styled JComboBox instance.
     */
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.setFont(FONT_BODY);
        comboBox.setBackground(COLOR_LIGHT);
        comboBox.setForeground(COLOR_TEXT);
        return comboBox;
    }

    /**
     * Creates a panel with standard padding and layout.
     *
     * @param layout The layout manager for the panel.
     * @param padding The padding size in pixels.
     * @return A styled JPanel instance.
     */
    public static JPanel createPanel(LayoutManager layout, int padding) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(COLOR_BACKGROUND);
        panel.setBorder(new EmptyBorder(padding, padding, padding, padding));
        return panel;
    }

    /**
     * Creates a panel with white background and standard padding.
     *
     * @param layout The layout manager for the panel.
     * @param padding The padding size in pixels.
     * @return A styled JPanel instance with white background.
     */
    public static JPanel createWhitePanel(LayoutManager layout, int padding) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(COLOR_LIGHT);
        panel.setBorder(new EmptyBorder(padding, padding, padding, padding));
        return panel;
    }

    /**
     * Shows a success message dialog.
     *
     * @param parent The parent component.
     * @param title The dialog title.
     * @param message The success message.
     */
    public static void showSuccess(Component parent, String title, String message) {
        showMessage(parent, title, message, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows an error message dialog.
     *
     * @param parent The parent component.
     * @param title The dialog title.
     * @param message The error message.
     */
    public static void showError(Component parent, String title, String message) {
        showMessage(parent, title, message, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows a warning message dialog.
     *
     * @param parent The parent component.
     * @param title The dialog title.
     * @param message The warning message.
     */
    public static void showWarning(Component parent, String title, String message) {
        showMessage(parent, title, message, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Shows a confirmation dialog with Yes/No buttons.
     *
     * @param parent The parent component.
     * @param title The dialog title.
     * @param message The confirmation question.
     * @return True if the user clicks Yes, false otherwise.
     */
    public static boolean showConfirmation(Component parent, String title, String message) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
        Window owner = getParentWindow(parent);
        JDialog dialog = pane.createDialog(owner, title);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        Object value = pane.getValue();
        return value instanceof Integer && ((Integer) value) == JOptionPane.YES_OPTION;
    }

    private static void showMessage(Component parent, String title, String message, int messageType) {
        JOptionPane pane = new JOptionPane(message, messageType);
        JDialog dialog = pane.createDialog(getParentWindow(parent), title);
        dialog.setLocationRelativeTo(getParentWindow(parent));
        dialog.setVisible(true);
    }

    public static void showComponentInfo(Component parent, String title, Component content) {
        JOptionPane pane = new JOptionPane(content, JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog(getParentWindow(parent), title);
        dialog.setLocationRelativeTo(getParentWindow(parent));
        dialog.setVisible(true);
    }

    private static Window getParentWindow(Component parent) {
        if (parent == null) return KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (parent instanceof Window) return (Window) parent;
        Window window = SwingUtilities.getWindowAncestor(parent);
        return window != null ? window : KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    }
    public static Color darkenColor(Color color, float factor) {
        return new Color(
                Math.max((int) (color.getRed() * (1 - factor)), 0),
                Math.max((int) (color.getGreen() * (1 - factor)), 0),
                Math.max((int) (color.getBlue() * (1 - factor)), 0)
        );
    }

    /**
     * Creates a horizontal separator.
     *
     * @return A styled JSeparator instance.
     */
    public static JSeparator createSeparator() {
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setForeground(COLOR_BORDER);
        return separator;
    }

    public static JPanel createCenteredContainer() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(COLOR_BACKGROUND);
        outer.setBorder(new EmptyBorder(PADDING_XLARGE, PADDING_XLARGE, PADDING_XLARGE, PADDING_XLARGE));
        return outer;
    }

    public static JPanel createContentCard(final int width) {
        JPanel card = new JPanel(new BorderLayout(PADDING_MEDIUM, PADDING_MEDIUM)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.width = width;
                return size;
            }
        };
        card.setBackground(COLOR_LIGHT);
        card.setBorder(new EmptyBorder(PADDING_XLARGE, PADDING_XLARGE, PADDING_XLARGE, PADDING_XLARGE));
        card.setMinimumSize(new Dimension(Math.min(width, 360), 1));
        return card;
    }

    public static void addCenteredCard(JPanel container, JPanel card) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        container.add(card, gbc);
    }

    public static JPanel createFormPanel() {
        return createWhitePanel(new GridBagLayout(), PADDING_MEDIUM);
    }

    public static void addFormRow(JPanel panel, int row, String labelText, JComponent field) {
        JLabel label = createLabel(labelText);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setPreferredSize(new Dimension(190, 28));

        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = row;
        labelGbc.insets = new Insets(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_MEDIUM);
        labelGbc.anchor = GridBagConstraints.EAST;
        panel.add(label, labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = row;
        fieldGbc.weightx = 1.0;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.insets = new Insets(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL);
        panel.add(field, fieldGbc);
    }

    public static JPanel createPathFieldWithButton(JTextField field, JButton button) {
        JPanel panel = new JPanel(new BorderLayout(PADDING_SMALL, 0));
        panel.setOpaque(false);
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }

    public static JButton createBrowseFileButton(Component parent, JTextField target) {
        JButton button = createButton("Selecionar", COLOR_PRIMARY);
        button.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                target.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        return button;
    }

    public static JButton createBrowseDirectoryButton(Component parent, JTextField target) {
        JButton button = createButton("Selecionar", COLOR_PRIMARY);
        button.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                target.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        return button;
    }

    public static JPanel createButtonRow(JButton... buttons) {
        JPanel row = createPanel(new FlowLayout(FlowLayout.CENTER, PADDING_MEDIUM, PADDING_SMALL), 0);
        row.setOpaque(false);
        for (JButton button : buttons) {
            row.add(button);
        }
        return row;
    }

    public static String htmlWrap(String text, int widthPx) {
        return "<html><div style='width:" + widthPx + "px'>" + text + "</div></html>";
    }
}
