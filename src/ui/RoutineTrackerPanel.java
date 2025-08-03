package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class RoutineTrackerPanel extends JPanel {
    private int userId;

    private JTextField dailyTaskField;
    private JTextField shortTermGoalField;
    private JTextField longTermGoalField;
    private JTextArea reflectionArea;
    private JButton saveBtn;

    public RoutineTrackerPanel(int userId) {
        this.userId = userId;
        setLayout(new GridBagLayout());
        setBackground(new Color(255, 255, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        // Daily Activity Log
        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Daily Activity Log:"), gbc);
        dailyTaskField = new JTextField(30);
        gbc.gridx = 1;
        add(dailyTaskField, gbc);
        y++;

        // Short-term Goal
        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Short-term Goal:"), gbc);
        shortTermGoalField = new JTextField(30);
        gbc.gridx = 1;
        add(shortTermGoalField, gbc);
        y++;

        // Long-term Goal
        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Long-term Goal:"), gbc);
        longTermGoalField = new JTextField(30);
        gbc.gridx = 1;
        add(longTermGoalField, gbc);
        y++;

        // Reflection
        gbc.gridx = 0; gbc.gridy = y;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Reflection:"), gbc);
        reflectionArea = new JTextArea(5, 30);
        JScrollPane scrollReflection = new JScrollPane(reflectionArea);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollReflection, gbc);
        y++;

        // Save Button
        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        saveBtn = new JButton("Save Entry");
        add(saveBtn, gbc);

        saveBtn.addActionListener(e -> saveEntry());
    }

    private void saveEntry() {
        String dailyTask = dailyTaskField.getText().trim();
        String shortGoal = shortTermGoalField.getText().trim();
        String longGoal = longTermGoalField.getText().trim();
        String reflection = reflectionArea.getText().trim();

        if (dailyTask.isEmpty() || shortGoal.isEmpty() || longGoal.isEmpty() || reflection.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields before saving.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO routine_logs (user_id, task, short_term_goal, long_term_goal, reflection, log_date) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, dailyTask);
                pstmt.setString(3, shortGoal);
                pstmt.setString(4, longGoal);
                pstmt.setString(5, reflection);
                pstmt.setDate(6, java.sql.Date.valueOf(LocalDate.now()));

                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Daily entry saved!", "Success", JOptionPane.INFORMATION_MESSAGE);

                // Clear fields after save
                dailyTaskField.setText("");
                shortTermGoalField.setText("");
                longTermGoalField.setText("");
                reflectionArea.setText("");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
