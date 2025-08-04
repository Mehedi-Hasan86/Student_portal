package ui;

import db.DBConnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class RoutineTrackerListView extends JFrame {
    private final int userId;
    private JTable routineTable;
    private DefaultTableModel tableModel;

    public RoutineTrackerListView(int userId) {
        this.userId = userId;

        setTitle("Routine Tracker Records");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.decode("#5e017d"));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel titleLabel = new JLabel("My Routine History", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Table setup
        tableModel = new DefaultTableModel() {
            // Makes all cells non-editable except buttons handled separately
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == getColumnCount() - 2 || column == getColumnCount() - 1;
            }

            // Set proper class for Edit/Delete button columns
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == getColumnCount() - 2 || columnIndex == getColumnCount() - 1) {
                    return JButton.class; // We use buttons in these columns
                }
                return super.getColumnClass(columnIndex);
            }
        };

        routineTable = new JTable(tableModel);
        initializeTableColumns();
        styleTable();

        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(routineTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Load data from DB
        fetchRoutineData();

        // Add button renderers and editors
        routineTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        routineTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox(), "Edit", routineTable, this));

        routineTable.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        routineTable.getColumn("Delete").setCellEditor(new ButtonEditor(new JCheckBox(), "Delete", routineTable, this));

        setVisible(true);
    }

    private void initializeTableColumns() {
        String[] columns = {"ID", "Date", "Reading", "Prayer", "Bodybuilding", "Friend", "Gift", "Newspaper", "Remarks", "Edit", "Delete"};
        tableModel.setColumnIdentifiers(columns);
    }

    private void styleTable() {
        routineTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        routineTable.setRowHeight(35);
        JTableHeader header = routineTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.decode("#d1e4e6"));
        header.setForeground(Color.decode("#5e017d"));
        routineTable.setSelectionBackground(Color.decode("#f0e6ff"));
        routineTable.setShowGrid(false);
        routineTable.setIntercellSpacing(new Dimension(0, 0));
        routineTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Set column preferred widths
        int[] colWidths = {50, 90, 80, 80, 100, 80, 80, 100, 200, 60, 70};
        TableColumnModel colModel = routineTable.getColumnModel();
        for (int i = 0; i < colWidths.length; i++) {
            colModel.getColumn(i).setPreferredWidth(colWidths[i]);
        }
    }

    private void fetchRoutineData() {
        tableModel.setRowCount(0); // Clear existing data
        String sql = "SELECT * FROM routine_tracker WHERE user_id = ? ORDER BY date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
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

                    Vector<Object> row = new Vector<>();
                    row.add(id);
                    row.add(date.toString());
                    row.add(String.format("%.1f hrs", reading));
                    row.add(String.format("%.1f hrs", prayer));
                    row.add(String.format("%.1f hrs", bodybuilding));
                    row.add(String.format("%.1f hrs", friend));
                    row.add(String.format("%.1f hrs", gift));
                    row.add(String.format("%.1f hrs", newspaper));
                    // Shorten remarks if long
                    row.add(remarks != null && remarks.length() > 20 ? remarks.substring(0, 20) + "..." : remarks);
                    row.add("Edit");
                    row.add("Delete");

                    tableModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error while loading routines: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to update routine entry in the DB and table
    public void updateRoutine(int routineId, float reading, float prayer, float bodybuilding,
                              float friend, float gift, float newspaper, String remarks, int rowIndex) {
        String sql = "UPDATE routine_tracker SET reading_hours=?, prayer_hours=?, bodybuilding_hours=?, " +
                "friend_connection_hours=?, gift_giving_hours=?, newspaper_hours=?, remarks=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
                // Update table model with formatted values
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

    // Method to delete routine entry from DB and table
    public void deleteRoutine(int routineId, int rowIndex) {
        String sql = "DELETE FROM routine_tracker WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, routineId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                tableModel.removeRow(rowIndex);
                JOptionPane.showMessageDialog(this, "Routine deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete routine.", "Deletion Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error during deletion: " + ex.getMessage(), "Deletion Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Button Renderer class to render buttons inside table cells
    class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            if ("Edit".equals(value)) {
                setBackground(Color.decode("#5e017d"));
            } else if ("Delete".equals(value)) {
                setBackground(Color.decode("#932fba"));
            } else {
                setBackground(Color.LIGHT_GRAY);
            }
            setForeground(Color.WHITE);
            return this;
        }
    }

    // Button Editor class to handle button clicks inside table cells
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
            label = value == null ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                try {
                    Object idObj = table.getModel().getValueAt(row, 0);
                    if (idObj == null) {
                        throw new NumberFormatException("ID column is null");
                    }
                    int routineId = Integer.parseInt(idObj.toString());
                    String action = button.getActionCommand();

                    if ("Edit".equals(action)) {
                        // Parse hours stripping 'hrs' suffix
                        float reading = parseHours(table.getModel().getValueAt(row, 2).toString());
                        float prayer = parseHours(table.getModel().getValueAt(row, 3).toString());
                        float bodybuilding = parseHours(table.getModel().getValueAt(row, 4).toString());
                        float friend = parseHours(table.getModel().getValueAt(row, 5).toString());
                        float gift = parseHours(table.getModel().getValueAt(row, 6).toString());
                        float newspaper = parseHours(table.getModel().getValueAt(row, 7).toString());

                        // Retrieve full remarks from DB for editing
                        String fullRemarks = fetchFullRemarks(routineId);

                        // Show edit dialog
                        RoutineTrackerEditDialog editDialog = new RoutineTrackerEditDialog(
                                routineListView, routineId, reading, prayer, bodybuilding, friend, gift, newspaper, fullRemarks, row);
                        editDialog.setVisible(true);

                    } else if ("Delete".equals(action)) {
                        int confirm = JOptionPane.showConfirmDialog(button, "Delete this routine entry?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            routineListView.deleteRoutine(routineId, row);
                        }
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(button, "Invalid routine ID format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            isPushed = false;
            return label;
        }

        private float parseHours(String text) {
            if (text == null) return 0;
            return Float.parseFloat(text.replace(" hrs", "").trim());
        }

        private String fetchFullRemarks(int routineId) {
            String remarks = "";
            String sql = "SELECT remarks FROM routine_tracker WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, routineId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        remarks = rs.getString("remarks");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return remarks;
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
