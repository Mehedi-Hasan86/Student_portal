package ui;

import db.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AddRoutineForm extends JFrame {
    private JTextField readingField, prayerField, bodybuildingField,
            friendField, giftField, newspaperField;
    private JTextArea remarksArea;
    private int userId;

    public AddRoutineForm(int userId) {
        this.userId = userId;
        setTitle("Add Routine Entry");
        setSize(600, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.decode("#5e017d"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Add Routine Entry", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;
        addField(formPanel, gbc, y++, "Reading Hours:", readingField = createStyledTextField());
        addField(formPanel, gbc, y++, "Prayer Hours:", prayerField = createStyledTextField());
        addField(formPanel, gbc, y++, "Bodybuilding Hours:", bodybuildingField = createStyledTextField());
        addField(formPanel, gbc, y++, "Friend Connection Hours:", friendField = createStyledTextField());
        addField(formPanel, gbc, y++, "Gift Giving Hours:", giftField = createStyledTextField());
        addField(formPanel, gbc, y++, "Newspaper Reading Hours:", newspaperField = createStyledTextField());

        gbc.gridx = 0;
        gbc.gridy = y++;
        gbc.gridwidth = 2;
        formPanel.add(new JLabel("Remarks:"), gbc);

        remarksArea = new JTextArea(4, 25);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(remarksArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        gbc.gridy = y++;
        formPanel.add(scrollPane, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(Color.WHITE);
        JButton saveBtn = createStyledButton("Save", Color.decode("#5e017d"));
        JButton cancelBtn = createStyledButton("Cancel", Color.decode("#932fba"));

        saveBtn.addActionListener(e -> saveRoutine());
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(15);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int y, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(jLabel, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void saveRoutine() {
        try {
            float reading = Float.parseFloat(readingField.getText().trim());
            float prayer = Float.parseFloat(prayerField.getText().trim());
            float bodybuilding = Float.parseFloat(bodybuildingField.getText().trim());
            float friend = Float.parseFloat(friendField.getText().trim());
            float gift = Float.parseFloat(giftField.getText().trim());
            float newspaper = Float.parseFloat(newspaperField.getText().trim());
            String remarks = remarksArea.getText().trim();

            if (reading < 0 || prayer < 0 || bodybuilding < 0 ||
                    friend < 0 || gift < 0 || newspaper < 0) {
                JOptionPane.showMessageDialog(this, "Hours cannot be negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO routine_tracker (user_id, date, reading_hours, prayer_hours, " +
                        "bodybuilding_hours, friend_connection_hours, gift_giving_hours, " +
                        "newspaper_hours, remarks) VALUES (?, CURDATE(), ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.setFloat(2, reading);
                stmt.setFloat(3, prayer);
                stmt.setFloat(4, bodybuilding);
                stmt.setFloat(5, friend);
                stmt.setFloat(6, gift);
                stmt.setFloat(7, newspaper);
                stmt.setString(8, remarks);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(this, "Routine saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save routine.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for hours.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}