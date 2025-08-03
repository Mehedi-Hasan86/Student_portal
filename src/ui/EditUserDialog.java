package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class EditUserDialog extends JDialog {
    private JTextField nameField;
    private JTextField emailField;
    private JTextField usernameField;
    private JTextField cityField;
    private JComboBox<String> genderCombo;
    private JTextField phoneField;
    private JTextArea addressArea;

    private JButton saveButton;
    private JButton cancelButton;

    private int userId;
    private int rowIndex;
    private UserListView parent;

    public EditUserDialog(Frame owner, UserListView parent, int userId,
                          String name, String email, String username,
                          String city, String gender, String phone,
                          String address, int rowIndex) {
        super(owner, "Edit User", true);
        this.userId = userId;
        this.rowIndex = rowIndex;
        this.parent = parent;

        setSize(400, 400);
        setLocationRelativeTo(owner);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        nameField = new JTextField(name, 20);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Email:"), gbc);
        emailField = new JTextField(email, 20);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(username, 20);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        // City
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("City:"), gbc);
        cityField = new JTextField(city, 20);
        gbc.gridx = 1;
        panel.add(cityField, gbc);

        // Gender
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Gender:"), gbc);
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderCombo.setSelectedItem(gender);
        gbc.gridx = 1;
        panel.add(genderCombo, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Phone:"), gbc);
        phoneField = new JTextField(phone, 20);
        gbc.gridx = 1;
        panel.add(phoneField, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Address:"), gbc);
        addressArea = new JTextArea(address, 4, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(addressArea);
        gbc.gridx = 1;
        panel.add(scrollPane, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        saveButton.addActionListener(e -> saveChanges());
        cancelButton.addActionListener(e -> dispose());

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void saveChanges() {
        // Validate inputs (simple example)
        if (nameField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                usernameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Name, Email, and Username cannot be empty.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String city = cityField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String phone = phoneField.getText().trim();
        String address = addressArea.getText().trim();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE users SET name=?, email=?, username=?, city=?, gender=?, phone=?, address=? WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, username);
            stmt.setString(4, city);
            stmt.setString(5, gender);
            stmt.setString(6, phone);
            stmt.setString(7, address);
            stmt.setInt(8, userId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                // Update table in parent view
                parent.updateUser(userId, name, email, username, city, gender, phone, address, rowIndex);
                JOptionPane.showMessageDialog(this, "User updated successfully.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }
}
