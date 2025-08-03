package ui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class RoutineTrackerListView extends JFrame {
    private JTable routineTable;
    private DefaultTableModel tableModel;
    private int userId;

    public RoutineTrackerListView(int userId) {
        this.userId = userId;
        setTitle("Routine Tracker Records");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.decode("#5e017d"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel titleLabel = new JLabel("My Routine History", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        routineTable = new JTable(tableModel);
        styleTable();

        fetchRoutineData();

        routineTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        routineTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox(), "Edit", routineTable, this));
        routineTable.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        routineTable.getColumn("Delete").setCellEditor(new ButtonEditor(new JCheckBox(), "Delete", routineTable, this));

        JScrollPane scrollPane = new JScrollPane(routineTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    private void styleTable() {
        String[] columns = {"ID", "Date", "Reading", "Prayer", "Bodybuilding", "Friend", "Gift", "Newspaper", "Remarks", "Edit", "Delete"};
        tableModel.setColumnIdentifiers(columns);

        routineTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        routineTable.setRowHeight(35);
        routineTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        routineTable.getTableHeader().setBackground(Color.decode("#d1e4e6"));
        routineTable.getTableHeader().setForeground(Color.decode("#5e017d"));
        routineTable.setSelectionBackground(Color.decode("#f0e6ff"));
        routineTable.setShowGrid(false);
        routineTable.setIntercellSpacing(new Dimension(0, 0));
    }

    private void fetchRoutineData() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM routine_tracker WHERE user_id = ? ORDER BY date DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                Date date = rs.getDate("date");
                float reading = rs.getFloat("reading_hours");
                float prayer = rs.getFloat("prayer_hours");
                float bodybuilding = rs.getFloat("bodybuilding_hours");
                float friend = rs.getFloat("friend_connection_hours");
                float gift = rs.getFloat("gift_giving_hours");
                float newspaper = rs.getFloat("newspaper_hours");
                String remarks = rs.getString("remarks");

                tableModel.addRow(new Object[]{
                        id, date.toString(),
                        String.format("%.1f hrs", reading),
                        String.format("%.1f hrs", prayer),
                        String.format("%.1f hrs", bodybuilding),
                        String.format("%.1f hrs", friend),
                        String.format("%.1f hrs", gift),
                        String.format("%.1f hrs", newspaper),
                        remarks.length() > 20 ? remarks.substring(0, 20) + "..." : remarks,
                        "Edit", "Delete"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error while loading routines: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateRoutine(int routineId, float reading, float prayer, float bodybuilding,
                              float friend, float gift, float newspaper, String remarks, int rowIndex) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE routine_tracker SET reading_hours=?, prayer_hours=?, bodybuilding_hours=?, " +
                    "friend_connection_hours=?, gift_giving_hours=?, newspaper_hours=?, remarks=? WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setFloat(1, reading);
            stmt.setFloat(2, prayer);
            stmt.setFloat(3, bodybuilding);
            stmt.setFloat(4, friend);
            stmt.setFloat(5, gift);
            stmt.setFloat(6, newspaper);
            stmt.setString(7, remarks);
            stmt.setInt(8, routineId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                tableModel.setValueAt(String.format("%.1f hrs", reading), rowIndex, 2);
                tableModel.setValueAt(String.format("%.1f hrs", prayer), rowIndex, 3);
                tableModel.setValueAt(String.format("%.1f hrs", bodybuilding), rowIndex, 4);
                tableModel.setValueAt(String.format("%.1f hrs", friend), rowIndex, 5);
                tableModel.setValueAt(String.format("%.1f hrs", gift), rowIndex, 6);
                tableModel.setValueAt(String.format("%.1f hrs", newspaper), rowIndex, 7);
                tableModel.setValueAt(remarks.length() > 20 ? remarks.substring(0, 20) + "..." : remarks, rowIndex, 8);
                JOptionPane.showMessageDialog(this, "Routine updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Could not update routine.", "Update Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error during update: " + ex.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteRoutine(int routineId, int rowIndex) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM routine_tracker WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, routineId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                SwingUtilities.invokeLater(() -> tableModel.removeRow(rowIndex));
                JOptionPane.showMessageDialog(this, "Routine deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete routine.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error during deletion: " + ex.getMessage(), "Deletion Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value.toString());
            if ("Edit".equals(value)) {
                setBackground(Color.decode("#5e017d"));
            } else {
                setBackground(Color.decode("#932fba"));
            }
            setForeground(Color.WHITE);
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int row;
        private JTable table;
        private RoutineTrackerListView routineListView;

        public ButtonEditor(JCheckBox checkBox, String actionCommand, JTable table, RoutineTrackerListView routineListView) {
            super(checkBox);
            this.table = table;
            this.routineListView = routineListView;
            button = new JButton();
            button.setOpaque(true);
            button.setActionCommand(actionCommand);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.row = row;
            label = value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                String action = button.getActionCommand();
                Object idObj = table.getModel().getValueAt(row, 0);
                if (idObj != null) {
                    try {
                        int routineId = Integer.parseInt(idObj.toString());
                        if ("Edit".equals(action)) {
                            float reading = Float.parseFloat(table.getModel().getValueAt(row, 2).toString().replace(" hrs", ""));
                            float prayer = Float.parseFloat(table.getModel().getValueAt(row, 3).toString().replace(" hrs", ""));
                            float bodybuilding = Float.parseFloat(table.getModel().getValueAt(row, 4).toString().replace(" hrs", ""));
                            float friend = Float.parseFloat(table.getModel().getValueAt(row, 5).toString().replace(" hrs", ""));
                            float gift = Float.parseFloat(table.getModel().getValueAt(row, 6).toString().replace(" hrs", ""));
                            float newspaper = Float.parseFloat(table.getModel().getValueAt(row, 7).toString().replace(" hrs", ""));
                            String remarks = getFullRemarks(routineId);

                            RoutineTrackerEditDialog editDialog = new RoutineTrackerEditDialog(
                                    routineListView, routineId, reading, prayer, bodybuilding,
                                    friend, gift, newspaper, remarks, row);
                            editDialog.setVisible(true);
                        } else if ("Delete".equals(action)) {
                            int confirm = JOptionPane.showConfirmDialog(button,
                                    "Delete this routine entry?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                            if (confirm == JOptionPane.YES_OPTION) {
                                routineListView.deleteRoutine(routineId, row);
                            }
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(button, "Invalid routine ID format.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            isPushed = false;
            return label;
        }

        private String getFullRemarks(int routineId) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT remarks FROM routine_tracker WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, routineId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("remarks");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return "";
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}