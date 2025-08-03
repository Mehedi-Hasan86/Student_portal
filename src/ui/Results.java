package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Results extends JFrame {
    private JTextField userIdField;
    private JTextField courseNameField;
    private JTextField examDateField;
    private JTextField gradeField;
    private JButton submitBtn, resetBtn;

    public Results() {
        setTitle("Results Entry");
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
        add(new JLabel("Course Name:"), gbc);
        courseNameField = new JTextField(20);
        gbc.gridx = 1;
        add(courseNameField, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Exam Date (YYYY-MM-DD):"), gbc);
        examDateField = new JTextField(20);
        examDateField.setText(LocalDate.now().toString());
        gbc.gridx = 1;
        add(examDateField, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Grade:"), gbc);
        gradeField = new JTextField(20);
        gbc.gridx = 1;
        add(gradeField, gbc);
        y++;

        JPanel buttonsPanel = new JPanel();
        submitBtn = new JButton("Submit");
        resetBtn = new JButton("Reset");
        buttonsPanel.add(submitBtn);
        buttonsPanel.add(resetBtn);

        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        add(buttonsPanel, gbc);

        submitBtn.addActionListener(e -> saveResult());
        resetBtn.addActionListener(e -> resetForm());

        setVisible(true);
    }

    private void saveResult() {
        String userIdText = userIdField.getText().trim();
        String courseName = courseNameField.getText().trim();
        String examDateText = examDateField.getText().trim();
        String grade = gradeField.getText().trim();

        if (userIdText.isEmpty() || courseName.isEmpty() || examDateText.isEmpty() || grade.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
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
            LocalDate.parse(examDateText);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO results (user_id, course_name, exam_date, grade) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, courseName);
            pstmt.setDate(3, java.sql.Date.valueOf(examDateText));
            pstmt.setString(4, grade);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Result record saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save result.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        userIdField.setText("");
        courseNameField.setText("");
        examDateField.setText(LocalDate.now().toString());
        gradeField.setText("");
    }
}
