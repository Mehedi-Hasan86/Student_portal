package ui;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UserListView extends JFrame {
    public JTable userTable;
    private DefaultTableModel tableModel;

    public UserListView() {
        setTitle("Registered Users");
        setSize(900, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Define the table model and columns
        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Email");
        tableModel.addColumn("Username");
        tableModel.addColumn("City");
        tableModel.addColumn("Gender");
        tableModel.addColumn("Phone");
        tableModel.addColumn("Address");
        tableModel.addColumn("Edit");
        tableModel.addColumn("Delete");

        userTable = new JTable(tableModel);

        // Set renderer and editor for Edit button column
        userTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        userTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox(), "Edit", this));

        // Set renderer and editor for Delete button column
        userTable.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        userTable.getColumn("Delete").setCellEditor(new ButtonEditor(new JCheckBox(), "Delete", this));

        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane);

        loadUsers();

        setVisible(true);
    }

    // Load user data from database into table
    private void loadUsers() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Object[] row = new Object[10];
                row[0] = rs.getInt("id");
                row[1] = rs.getString("name");
                row[2] = rs.getString("email");
                row[3] = rs.getString("username");
                row[4] = rs.getString("city");
                row[5] = rs.getString("gender");
                row[6] = rs.getString("phone");
                row[7] = rs.getString("address");
                row[8] = "Edit";   // Text for Edit button
                row[9] = "Delete"; // Text for Delete button

                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    // Update user in DB and table model
    public void updateUser(int userId, String name, String email, String username, String city, String gender, String phone, String address, int rowIndex) {
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
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                tableModel.setValueAt(name, rowIndex, 1);
                tableModel.setValueAt(email, rowIndex, 2);
                tableModel.setValueAt(username, rowIndex, 3);
                tableModel.setValueAt(city, rowIndex, 4);
                tableModel.setValueAt(gender, rowIndex, 5);
                tableModel.setValueAt(phone, rowIndex, 6);
                tableModel.setValueAt(address, rowIndex, 7);
                JOptionPane.showMessageDialog(this, "User updated successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "User not found or update failed.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage());
        }
    }

    // Delete user from DB and remove row from table model
    public void deleteUser(int userId, int rowIndex) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM users WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                tableModel.removeRow(rowIndex);
                JOptionPane.showMessageDialog(this, "User deleted successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "User not found or delete failed.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + e.getMessage());
        }
    }

    // Main method for testing independently
    public static void main(String[] args) {
        SwingUtilities.invokeLater(UserListView::new);
    }
}

// Renderer to display buttons inside table cells
class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        setText(value == null ? "" : value.toString());
        return this;
    }
}

// Editor to handle button clicks inside table cells
class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private String label;
    private boolean isPushed;
    private final String action;
    private int selectedRow;
    private UserListView parent;

    public ButtonEditor(JCheckBox checkBox, String action, UserListView parent) {
        super(checkBox);
        this.action = action;
        this.parent = parent;
        button = new JButton();
        button.setOpaque(true);

        button.addActionListener(e -> fireEditingStopped());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        selectedRow = row;
        label = value == null ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
            JTable table = parent.userTable;
            int userId = (int) table.getValueAt(selectedRow, 0);

            if ("Edit".equals(action)) {
                // Fetch current data
                String name = (String) table.getValueAt(selectedRow, 1);
                String email = (String) table.getValueAt(selectedRow, 2);
                String username = (String) table.getValueAt(selectedRow, 3);
                String city = (String) table.getValueAt(selectedRow, 4);
                String gender = (String) table.getValueAt(selectedRow, 5);
                String phone = (String) table.getValueAt(selectedRow, 6);
                String address = (String) table.getValueAt(selectedRow, 7);

                // Show edit dialog - you need to implement EditUserDialog with these fields accordingly
                EditUserDialog dialog = new EditUserDialog(parent, parent, userId, name, email, username, city, gender, phone, address, selectedRow);
                dialog.setVisible(true);

            } else if ("Delete".equals(action)) {
                int confirm = JOptionPane.showConfirmDialog(button,
                        "Are you sure you want to delete user ID " + userId + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    parent.deleteUser(userId, selectedRow);
                }
            }
        }
        isPushed = false;
        return label;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }
}
