package ui;

import db.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AddCSEStudentForm extends JFrame {
    public AddCSEStudentForm(int userId) {
        setTitle("Add CSE Student Data");
        setSize(450, 370);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.decode("#5e017d"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Add CSE Student Data", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 15, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JTextField subjectField = new JTextField(20);
        JTextField gradeField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel subjectLabel = new JLabel("Subject:");
        subjectLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(subjectLabel, gbc);
        gbc.gridx = 1;
        subjectField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subjectField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(subjectField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel gradeLabel = new JLabel("Grade:");
        gradeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(gradeLabel, gbc);
        gbc.gridx = 1;
        gradeField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gradeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(gradeField, gbc);

        JButton saveBtn = new JButton("Save");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setBackground(Color.decode("#5e017d"));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(8, 30, 8, 30));
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(saveBtn, gbc);

        add(formPanel, BorderLayout.CENTER);

        saveBtn.addActionListener(e -> {
            String subject = subjectField.getText().trim();
            String grade = gradeField.getText().trim();

            if (subject.isEmpty() || grade.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO cse_student_data (user_id, subject, grade) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.setString(2, subject);
                stmt.setString(3, grade);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "CSE data saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}