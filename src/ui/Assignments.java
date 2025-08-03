package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Assignments extends JFrame {
    private JTextField userIdField;
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JTextField deadlineField;
    private JCheckBox submittedCheckbox;
    private JButton submitBtn, resetBtn;

    public Assignments() {
        setTitle("Assignments Entry");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("User ID:"), gbc);
        userIdField = new JTextField(20);
        gbc.gridx = 1;
        add(userIdField, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Title:"), gbc);
        titleField = new JTextField(25);
        gbc.gridx = 1;
        add(titleField, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Description:"), gbc);

        descriptionArea = new JTextArea(5, 25);
        descriptionArea.setLineWrap(true);
        JScrollPane scrollDesc = new JScrollPane(descriptionArea);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollDesc, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Deadline (YYYY-MM-DD):"), gbc);
        deadlineField = new JTextField(20);
        deadlineField.setText(LocalDate.now().toString());
        gbc.gridx = 1;
        add(deadlineField, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Submitted:"), gbc);
        submittedCheckbox = new JCheckBox();
        gbc.gridx = 1;
        add(submittedCheckbox, gbc);
        y++;

        JPanel buttonsPanel = new JPanel();
        submitBtn = new JButton("Submit");
        resetBtn = new JButton("Reset");
        buttonsPanel.add(submitBtn);
        buttonsPanel.add(resetBtn);

        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        add(buttonsPanel, gbc);

        submitBtn.addActionListener(e -> saveAssignment());
        resetBtn.addActionListener(e -> resetForm());

        setVisible(true);
    }

    private void saveAssignment() {
        String userIdText = userIdField.getText().trim();
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String deadlineText = deadlineField.getText().trim();
        boolean submitted = submittedCheckbox.isSelected();

        if (userIdText.isEmpty() || title.isEmpty() || deadlineText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "User ID, Title, and Deadline are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "User ID must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LocalDate.parse(deadlineText);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid deadline date format. Use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO assignments (user_id, title, description, deadline, submitted) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setDate(4, java.sql.Date.valueOf(deadlineText));
            pstmt.setBoolean(5, submitted);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Assignment saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save assignment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        userIdField.setText("");
        titleField.setText("");
        descriptionArea.setText("");
        deadlineField.setText(LocalDate.now().toString());
        submittedCheckbox.setSelected(false);
    }
}
