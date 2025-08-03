package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

public class Settings extends JFrame {

    private JComboBox<String> themeCombo;
    private JComboBox<String> dashboardSizeCombo;
    private JComboBox<String> fontFamilyCombo;
    private JSpinner fontSizeSpinner;
    private JCheckBox boldCheckBox, italicCheckBox;
    private JButton applyBtn, resetBtn;

    private Dashboard dashboard;

    // Default settings (fallback/reset)
    private final String defaultTheme = "Light";
    private final String defaultSize = "Medium";
    private final String defaultFontFamily = "Segoe UI";
    private final int defaultFontSize = 14;
    private final int defaultFontStyle = Font.PLAIN;

    public Settings(Dashboard dashboard) {
        this.dashboard = dashboard;

        setTitle("Settings");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        setResizable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;

        // Get current font from dashboard content pane
        Font currentFont = dashboard.getContentPane().getFont();

        String currentFontFamily = currentFont != null ? currentFont.getFamily() : defaultFontFamily;
        int currentFontSize = currentFont != null ? currentFont.getSize() : defaultFontSize;
        int currentFontStyle = currentFont != null ? currentFont.getStyle() : defaultFontStyle;

        // Infer current theme by panel background brightness (simple heuristic)
        Color bg = dashboard.getContentPane().getBackground();
        String currentTheme = isDark(bg) ? "Dark" : "Light";

        // Infer current dashboard size
        Dimension dashSize = dashboard.getSize();
        String currentSize;
        if (dashSize.width <= 750) currentSize = "Small";
        else if (dashSize.width >= 1100) currentSize = "Large";
        else currentSize = "Medium";

        // Theme selection: Light / Dark
        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Theme:"), gbc);

        themeCombo = new JComboBox<>(new String[]{"Light", "Dark"});
        themeCombo.setSelectedItem(currentTheme);
        gbc.gridx = 1; gbc.gridy = y++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(themeCombo, gbc);

        // Dashboard Size
        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Dashboard Size:"), gbc);

        dashboardSizeCombo = new JComboBox<>(new String[]{"Small", "Medium", "Large"});
        dashboardSizeCombo.setSelectedItem(currentSize);
        gbc.gridx = 1; gbc.gridy = y++;
        gbc.fill = GridBagConstraints.NONE;
        add(dashboardSizeCombo, gbc);

        // Font Family
        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Font Family:"), gbc);

        fontFamilyCombo = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        // Select closest match if exact not found
        if (!Objects.equals(currentFontFamily, null)) {
            boolean found = false;
            for (int i = 0; i < fontFamilyCombo.getItemCount(); i++) {
                if (fontFamilyCombo.getItemAt(i).equalsIgnoreCase(currentFontFamily)) {
                    fontFamilyCombo.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                fontFamilyCombo.setSelectedItem(defaultFontFamily);
            }
        }
        gbc.gridx = 1; gbc.gridy = y++;
        add(fontFamilyCombo, gbc);

        // Font Size
        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Font Size:"), gbc);

        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(currentFontSize, 8, 48, 1));
        gbc.gridx = 1; gbc.gridy = y++;
        add(fontSizeSpinner, gbc);

        // Font Style: Bold / Italic
        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Font Style:"), gbc);

        JPanel stylePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        boldCheckBox = new JCheckBox("Bold");
        italicCheckBox = new JCheckBox("Italic");
        boldCheckBox.setSelected((currentFontStyle & Font.BOLD) != 0);
        italicCheckBox.setSelected((currentFontStyle & Font.ITALIC) != 0);
        stylePanel.add(boldCheckBox);
        stylePanel.add(italicCheckBox);
        gbc.gridx = 1; gbc.gridy = y++;
        add(stylePanel, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        applyBtn = new JButton("Apply");
        resetBtn = new JButton("Reset");
        buttonPanel.add(applyBtn);
        buttonPanel.add(resetBtn);

        gbc.gridx = 0; gbc.gridy = y++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        // Listeners for Apply and Reset
        applyBtn.addActionListener(e -> applySettings());
        resetBtn.addActionListener(e -> resetSettings());

        setVisible(true);
    }

    private void applySettings() {
        String theme = (String) themeCombo.getSelectedItem();
        String size = (String) dashboardSizeCombo.getSelectedItem();
        String fontFamily = (String) fontFamilyCombo.getSelectedItem();
        int fontSize = (int) fontSizeSpinner.getValue();
        int fontStyle = Font.PLAIN;
        if (boldCheckBox.isSelected()) fontStyle |= Font.BOLD;
        if (italicCheckBox.isSelected()) fontStyle |= Font.ITALIC;

        // Apply theme (demo with UIManager properties)
        if ("Dark".equalsIgnoreCase(theme)) {
            setDarkTheme();
        } else {
            setLightTheme();
        }

        // Resize dashboard accordingly
        if (dashboard != null) {
            Dimension dim;
            switch (size) {
                case "Small": dim = new Dimension(700, 450); break;
                case "Large": dim = new Dimension(1200, 800); break;
                case "Medium":
                default: dim = new Dimension(900, 600); break;
            }
            dashboard.setSize(dim);
            dashboard.setLocationRelativeTo(null);
        }

        // Set new font on dashboard and children recursively
        if (dashboard != null) {
            Font newFont = new Font(fontFamily, fontStyle, fontSize);
            updateComponentFont(dashboard.getContentPane(), newFont);
            // Also update menus bars if needed
            if (dashboard.getJMenuBar() != null) {
                updateComponentFont(dashboard.getJMenuBar(), newFont);
            }
        }

        JOptionPane.showMessageDialog(this,
                "Settings applied! Some changes might require restart or focus switch.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetSettings() {
        themeCombo.setSelectedItem(defaultTheme);
        dashboardSizeCombo.setSelectedItem(defaultSize);
        fontFamilyCombo.setSelectedItem(defaultFontFamily);
        fontSizeSpinner.setValue(defaultFontSize);
        boldCheckBox.setSelected(false);
        italicCheckBox.setSelected(false);
    }

    private void setDarkTheme() {
        UIManager.put("Panel.background", new Color(45, 45, 45));
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("Button.background", new Color(70, 70, 70));
        UIManager.put("Button.foreground", Color.WHITE);
        // Update all components in this window and dashboard
        SwingUtilities.updateComponentTreeUI(this);
        if (dashboard != null) {
            SwingUtilities.updateComponentTreeUI(dashboard);
        }
    }

    private void setLightTheme() {
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("Label.foreground", Color.BLACK);
        UIManager.put("Button.background", Color.LIGHT_GRAY);
        UIManager.put("Button.foreground", Color.BLACK);
        SwingUtilities.updateComponentTreeUI(this);
        if (dashboard != null) {
            SwingUtilities.updateComponentTreeUI(dashboard);
        }
    }

    private void updateComponentFont(Component comp, Font font) {
        if (comp == null || font == null) return;
        comp.setFont(font);
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                updateComponentFont(child, font);
            }
        }
    }

    private boolean isDark(Color color) {
        if (color == null) return false;
        // Using luminance to check darkness
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance < 0.5;
    }
}
