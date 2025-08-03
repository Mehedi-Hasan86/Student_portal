package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Attendance extends JFrame {
    private JTextField userIdField;
    private JTextField attendanceDateField;
    private JComboBox<String> statusCombo;
    private JTextArea remarksArea;
    private JButton submitBtn, resetBtn;

    public Attendance() {
        setTitle("Attendance Entry");
        setSize(400, 350);
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
        add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        attendanceDateField = new JTextField(20);
        attendanceDateField.setText(LocalDate.now().toString());
        gbc.gridx = 1;
        add(attendanceDateField, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Status:"), gbc);
        statusCombo = new JComboBox<>(new String[]{"Present", "Absent", "Leave"});
        gbc.gridx = 1;
        add(statusCombo, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Remarks:"), gbc);
        remarksArea = new JTextArea(4, 20);
        JScrollPane scrollRemarks = new JScrollPane(remarksArea);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollRemarks, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        y++;

        JPanel buttonsPanel = new JPanel();
        submitBtn = new JButton("Submit");
        resetBtn = new JButton("Reset");
        buttonsPanel.add(submitBtn);
        buttonsPanel.add(resetBtn);

        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        add(buttonsPanel, gbc);

        submitBtn.addActionListener(e -> saveAttendance());
        resetBtn.addActionListener(e -> resetForm());

        setVisible(true);
    }

    private void saveAttendance() {
        String userIdText = userIdField.getText().trim();
        String dateText = attendanceDateField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();
        String remarks = remarksArea.getText().trim();

        if (userIdText.isEmpty() || dateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "User ID and Date are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
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
            LocalDate.parse(dateText);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO attendance (user_id, attendance_date, status, remarks) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, java.sql.Date.valueOf(dateText));
            pstmt.setString(3, status);
            pstmt.setString(4, remarks);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Attendance record saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save attendance.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        userIdField.setText("");
        attendanceDateField.setText(LocalDate.now().toString());
        statusCombo.setSelectedIndex(0);
        remarksArea.setText("");
    }
}
