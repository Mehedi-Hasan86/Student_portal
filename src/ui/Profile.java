package ui;

import javax.swing.*;
import java.awt.*;

public class Profile extends JFrame {
    public Profile(String userName, String userEmail, String profileImagePath) {
        setTitle("Profile");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            ImageIcon icon = new ImageIcon(profileImagePath);
            Image image = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel picLabel = new JLabel(new ImageIcon(image));
            picLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(picLabel);
        }

        JLabel nameLabel = new JLabel("Name: " + userName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emailLabel = new JLabel("Email: " + userEmail);
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(15));
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(emailLabel);

        add(panel);
        setVisible(true);
    }
}
