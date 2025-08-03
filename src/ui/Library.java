package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Library extends JFrame {
    private JTextField titleField, authorField, isbnField, publishedYearField, availableCopiesField;
    private JButton submitBtn, resetBtn;

    public Library() {
        setTitle("Library Entry");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        // Title
        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Title:"), gbc);
        titleField = new JTextField(25);
        gbc.gridx = 1;
        add(titleField, gbc);
        y++;

        // Author
        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Author:"), gbc);
        authorField = new JTextField(25);
        gbc.gridx = 1;
        add(authorField, gbc);
        y++;

        // ISBN
        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("ISBN:"), gbc);
        isbnField = new JTextField(20);
        gbc.gridx = 1;
        add(isbnField, gbc);
        y++;

        // Published Year
        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Published Year:"), gbc);
        publishedYearField = new JTextField(10);
        gbc.gridx = 1;
        add(publishedYearField, gbc);
        y++;

        // Available Copies
        gbc.gridx = 0; gbc.gridy = y;
        add(new JLabel("Available Copies:"), gbc);
        availableCopiesField = new JTextField(5);
        gbc.gridx = 1;
        add(availableCopiesField, gbc);
        y++;

        // Buttons Panel
        JPanel buttonsPanel = new JPanel();
        submitBtn = new JButton("Submit");
        resetBtn = new JButton("Reset");
        buttonsPanel.add(submitBtn);
        buttonsPanel.add(resetBtn);

        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        add(buttonsPanel, gbc);

        submitBtn.addActionListener(e -> saveLibraryEntry());
        resetBtn.addActionListener(e -> resetForm());

        setVisible(true);
    }

    private void saveLibraryEntry() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String isbn = isbnField.getText().trim();
        String publishedYearStr = publishedYearField.getText().trim();
        String availableCopiesStr = availableCopiesField.getText().trim();

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() ||
                publishedYearStr.isEmpty() || availableCopiesStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int publishedYear;
        int availableCopies;
        try {
            publishedYear = Integer.parseInt(publishedYearStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Published Year must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            availableCopies = Integer.parseInt(availableCopiesStr);
            if (availableCopies < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Available Copies must be a non-negative integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO library (title, author, isbn, published_year, available_copies) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, isbn);
            pstmt.setInt(4, publishedYear);
            pstmt.setInt(5, availableCopies);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Library entry saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save library entry.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        titleField.setText("");
        authorField.setText("");
        isbnField.setText("");
        publishedYearField.setText("");
        availableCopiesField.setText("");
    }
}
