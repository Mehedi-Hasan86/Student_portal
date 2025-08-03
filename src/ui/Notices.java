package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Notices extends JFrame {
    private JTextField titleField;
    private JTextArea contentArea;
    private JButton submitBtn, resetBtn;

    public Notices() {
        setTitle("Notices Entry");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Title:"), gbc);
        titleField = new JTextField(25);
        gbc.gridx = 1;
        add(titleField, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Content:"), gbc);

        contentArea = new JTextArea(6, 25);
        contentArea.setLineWrap(true);
        JScrollPane scrollContent = new JScrollPane(contentArea);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(scrollContent, gbc);
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

        submitBtn.addActionListener(e -> saveNotice());
        resetBtn.addActionListener(e -> resetForm());

        setVisible(true);
    }

    private void saveNotice() {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title and Content are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO notices (title, content) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, content);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Notice saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save notice.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        titleField.setText("");
        contentArea.setText("");
    }
}
