package ui;

import db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Arrays;

public class SignInForm extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton signInButton, registerButton;

    public SignInForm() {
        setTitle("Sign In");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.decode("#232622"));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Sign In", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.decode("#81f542"));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.decode("#232622"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Username label
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.decode("#81f542"));
        formPanel.add(userLabel, gbc);

        // Username input
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        usernameField.setBackground(Color.decode("#81f542"));
        usernameField.setForeground(Color.decode("#232622"));
        formPanel.add(usernameField, gbc);

        // Password label
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.decode("#81f542"));
        formPanel.add(passLabel, gbc);

        // Password input
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setBackground(Color.decode("#81f542"));
        passwordField.setForeground(Color.decode("#232622"));
        formPanel.add(passwordField, gbc);

        // Key navigation
        usernameField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    passwordField.requestFocus();
            }
        });
        passwordField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    performLogin();
            }
        });

        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(Color.decode("#232622"));

        signInButton = new JButton("Sign In");
        signInButton.setBackground(Color.decode("#81f542"));
        signInButton.setForeground(Color.decode("#232622"));
        signInButton.addActionListener(e -> performLogin());
        buttonPanel.add(signInButton);

        registerButton = new JButton("Create Account");
        registerButton.setBackground(Color.decode("#81f542"));
        registerButton.setForeground(Color.decode("#232622"));
        registerButton.addActionListener(e -> {
            dispose();
            new RegistrationForm().setVisible(true);
        });
        buttonPanel.add(registerButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
        setVisible(true);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, name, email, profile_picture, password FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                // SAFEGUARD: Avoid exception if stored hash is plain text or corrupted
                if (storedHash != null && storedHash.startsWith("$2") && BCrypt.checkpw(password, storedHash)) {
                    int userId = rs.getInt("id");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    String profilePic = rs.getString("profile_picture");

                    JOptionPane.showMessageDialog(this, "Welcome, " + name + "!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    dispose();
                    new Dashboard(userId, name, email, profilePic).setVisible(true);
                } else {
                    showError("Invalid username or password.");
                }
            } else {
                showError("Invalid username or password.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(passwordChars, '\0'); // Clear password from memory
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Login Failed", JOptionPane.ERROR_MESSAGE);
        passwordField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SignInForm::new);
    }
}
