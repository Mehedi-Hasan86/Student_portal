package ui;

import db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;

public class RegistrationForm extends JFrame {

    private JTextField nameField, emailField, usernameField, cityField, phoneField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> genderCombo;
    private JTextArea addressArea;
    private JLabel profilePicLabel;
    private String profileImagePath = null;

    // Colors for styling
    private final Color primaryColor = Color.decode("#b00588");  // Main highlight color
    private final Color bgColor = Color.decode("#c2f0ee");       // Background color

    public RegistrationForm() {
        setTitle("User Registration");
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(false);

        // Main panel using GridBagLayout for flexible form layout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(bgColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title label
        JLabel title = new JLabel("Create New Account");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(primaryColor);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(title, gbc);

        // Create and add form fields
        nameField = createField("Name", 1, gbc, mainPanel);
        emailField = createField("Email", 2, gbc, mainPanel);
        usernameField = createField("Username", 3, gbc, mainPanel);

        // Password field label and input
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        passwordField.setBackground(Color.white);
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);

        // Confirm Password field label and input
        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Confirm Password:"), gbc);
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setBackground(Color.white);
        gbc.gridx = 1;
        mainPanel.add(confirmPasswordField, gbc);

        cityField = createField("City", 6, gbc, mainPanel);

        // Gender selection combo box
        gbc.gridy = 7; gbc.gridx = 0;
        mainPanel.add(new JLabel("Gender:"), gbc);
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderCombo.setBackground(Color.white);
        gbc.gridx = 1;
        mainPanel.add(genderCombo, gbc);

        phoneField = createField("Phone", 8, gbc, mainPanel);

        // Address text area label and input
        gbc.gridy = 9; gbc.gridx = 0;
        mainPanel.add(new JLabel("Address:"), gbc);
        addressArea = new JTextArea(3, 20);
        addressArea.setBackground(Color.white);
        addressArea.setLineWrap(true);
        gbc.gridx = 1;
        mainPanel.add(new JScrollPane(addressArea), gbc);

        // Profile Picture label and clickable label for choosing file
        gbc.gridy = 10; gbc.gridx = 0;
        mainPanel.add(new JLabel("Profile Picture:"), gbc);
        profilePicLabel = new JLabel("Choose File");
        profilePicLabel.setOpaque(true);
        profilePicLabel.setBackground(Color.LIGHT_GRAY);
        profilePicLabel.setPreferredSize(new Dimension(150, 25));
        profilePicLabel.setHorizontalAlignment(SwingConstants.CENTER);
        profilePicLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profilePicLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    profileImagePath = file.getAbsolutePath();
                    profilePicLabel.setText(file.getName());
                }
            }
        });
        gbc.gridx = 1;
        mainPanel.add(profilePicLabel, gbc);

        // Submit button to register user
        gbc.gridy = 11;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 0, 15, 0);
        JButton submitButton = new JButton("Register");
        submitButton.setBackground(primaryColor);
        submitButton.setForeground(Color.white);
        submitButton.setFocusPainted(false);
        submitButton.addActionListener(e -> registerUser());
        mainPanel.add(submitButton, gbc);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    // Helper method: Creates a label + text field, adds them to the panel
    private JTextField createField(String label, int yPos, GridBagConstraints gbc, JPanel panel) {
        gbc.gridy = yPos;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lbl = new JLabel(label + "   :");
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        JTextField field = new JTextField(20);
        field.setBackground(Color.white);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);

        gbc.fill = GridBagConstraints.NONE;  // Reset fill for next component
        return field;
    }

    // The main registration logic: input validation, password hashing, DB insert
    private void registerUser() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String city = cityField.getText().trim();
        String gender = (String) genderCombo.getSelectedItem();
        String phone = phoneField.getText().trim();
        String address = addressArea.getText().trim();

        // Check all fields filled
        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty() || city.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check passwords match
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Hash password securely before storing
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (name, email, username, password, city, gender, phone, address, profile_picture) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, username);
                stmt.setString(4, hashedPassword);  // Store hashed password
                stmt.setString(5, city);
                stmt.setString(6, gender);
                stmt.setString(7, phone);
                stmt.setString(8, address);
                stmt.setString(9, profileImagePath);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Registration successful! Please sign in.");
                dispose();
                new SignInForm().setVisible(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Main method to run RegistrationForm independently
    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegistrationForm::new);
    }
}
