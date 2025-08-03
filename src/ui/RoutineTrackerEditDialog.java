package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RoutineTrackerEditDialog extends JDialog {
    private JTextField readingField, prayerField, bodybuildingField, friendField, giftField, newspaperField;
    private JTextArea remarksArea;
    private JButton saveButton, cancelButton;

    private ui.RoutineTrackerListView parentListView;
    private int routineId;
    private int rowIndex;

    public RoutineTrackerEditDialog(ui.RoutineTrackerListView parent, int routineId,
                                    float reading, float prayer, float bodybuilding,
                                    float friend, float gift, float newspaper,
                                    String remarks, int rowIndex) {
        super(parent, "Edit Routine Entry", true);
        this.parentListView = parent;
        this.routineId = routineId;
        this.rowIndex = rowIndex;

        setSize(450, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.decode("#5e017d"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Edit Routine Entry", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;
        addField(formPanel, gbc, y++, "Reading Hours:", readingField = createStyledTextField(String.valueOf(reading)));
        addField(formPanel, gbc, y++, "Prayer Hours:", prayerField = createStyledTextField(String.valueOf(prayer)));
        addField(formPanel, gbc, y++, "Bodybuilding Hours:", bodybuildingField = createStyledTextField(String.valueOf(bodybuilding)));
        addField(formPanel, gbc, y++, "Friend Connection Hours:", friendField = createStyledTextField(String.valueOf(friend)));
        addField(formPanel, gbc, y++, "Gift Giving Hours:", giftField = createStyledTextField(String.valueOf(gift)));
        addField(formPanel, gbc, y++, "Newspaper Reading Hours:", newspaperField = createStyledTextField(String.valueOf(newspaper)));

        gbc.gridx = 0;
        gbc.gridy = y++;
        gbc.gridwidth = 2;
        formPanel.add(new JLabel("Remarks:"), gbc);

        remarksArea = new JTextArea(4, 25);
        remarksArea.setText(remarks);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(remarksArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        gbc.gridy = y++;
        formPanel.add(scrollPane, gbc);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(Color.WHITE);
        saveButton = createStyledButton("Save", Color.decode("#5e017d"));
        cancelButton = createStyledButton("Cancel", Color.decode("#932fba"));

        saveButton.addActionListener(e -> saveChanges());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public RoutineTrackerEditDialog() {

    }

    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text, 15);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int y, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(jLabel, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void saveChanges() {
        try {
            float reading = Float.parseFloat(readingField.getText().trim());
            float prayer = Float.parseFloat(prayerField.getText().trim());
            float bodybuilding = Float.parseFloat(bodybuildingField.getText().trim());
            float friend = Float.parseFloat(friendField.getText().trim());
            float gift = Float.parseFloat(giftField.getText().trim());
            float newspaper = Float.parseFloat(newspaperField.getText().trim());
            String remarks = remarksArea.getText().trim();

            if (reading < 0 || prayer < 0 || bodybuilding < 0 ||
                    friend < 0 || gift < 0 || newspaper < 0) {
                JOptionPane.showMessageDialog(this, "Hours cannot be negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            parentListView.updateRoutine(routineId, reading, prayer, bodybuilding, friend, gift, newspaper, remarks, rowIndex);
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for hours.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}