package ui;

import db.DBConnection;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Arrays;
import java.util.Vector;

public class Dashboard extends JFrame {

    private final int userId;
    private final String userName;
    private final String userEmail;
    private final String profileImagePath;

    private JTabbedPane mainTabbedPane;

    private JTable trackingTable, libraryTable, assignmentsTable;
    private DefaultTableModel trackingTableModel, libraryTableModel, assignmentsTableModel;

    public Dashboard(int userId, String name, String email, String profileImagePath) {
        this.userId = userId;
        this.userName = name;
        this.userEmail = email;
        this.profileImagePath = profileImagePath;

        setTitle("Student Portal Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setupHeader();
        setupSideMenu();
        setupMainContentPanel();
        setupMenuBar();

        loadTrackingData();
        loadLibraryData();
        loadAssignmentsData();

        setVisible(true);
    }

    private void setupHeader() {
        JLabel headerLabel = new JLabel("Welcome, " + userName + " - Student Portal", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0x3a0057));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void setupSideMenu() {
        JPanel menuPanel = new JPanel(new GridLayout(11, 1, 5, 5));
        menuPanel.setBackground(new Color(0xf5f5f5));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        menuPanel.setPreferredSize(new Dimension(200, getHeight()));

        String[] menuItems = {
                "Routine Tracker", "Attendance", "Courses", "Results", "Notices",
                "Assignments", "Library", "Profile", "Settings", "User List", "Logout"
        };

        for (String item : menuItems) {
            JButton button = new JButton(item);
            button.setFocusPainted(false);
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(0x3a0057));
            button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            button.setBorder(BorderFactory.createLineBorder(new Color(0xcccccc)));

            switch (item) {
                case "Routine Tracker":
                    button.addActionListener(e -> openRoutineTracker());
                    break;
                case "Attendance":
                    button.addActionListener(e -> openAttendance());
                    break;
                case "Courses":
                    button.addActionListener(e -> openAddCSEStudentForm());
                    break;
                case "Results":
                    button.addActionListener(e -> openResults());
                    break;
                case "Notices":
                    button.addActionListener(e -> openNotices());
                    break;
                case "Assignments":
                    button.addActionListener(e -> mainTabbedPane.setSelectedIndex(2));
                    break;
                case "Library":
                    button.addActionListener(e -> mainTabbedPane.setSelectedIndex(1));
                    break;
                case "Profile":
                    button.addActionListener(e -> mainTabbedPane.setSelectedIndex(3));
                    break;
                case "Settings":
                    button.addActionListener(e -> openSettings());
                    break;
                case "User List":
                    button.addActionListener(e -> openUserListView());
                    break;
                case "Logout":
                    button.addActionListener(e -> logout());
                    break;
                default:
                    button.addActionListener(e -> showFeatureNotImplemented(item));
            }
            menuPanel.add(button);
        }
        add(menuPanel, BorderLayout.WEST);
    }

    private void setupMainContentPanel() {
        mainTabbedPane = new JTabbedPane();

        // 30-Day Tracking Tab
        trackingTableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        trackingTableModel.setColumnIdentifiers(new String[]{
                "Date", "Reading Hours", "Prayer Hours", "Bodybuilding Hours",
                "Friend Connection Hours", "Gift Giving Hours", "Newspaper Hours", "Remarks"
        });
        trackingTable = new JTable(trackingTableModel);
        setupTable(trackingTable, new int[]{90, 90, 90, 90, 110, 110, 100, 250});
        JScrollPane trackingScroll = new JScrollPane(trackingTable);
        mainTabbedPane.addTab("30-Day Tracking", trackingScroll);

        // Library Tab
        libraryTableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        libraryTableModel.setColumnIdentifiers(new String[]{
                "Title", "Author", "ISBN", "Published Year", "Available Copies"
        });
        libraryTable = new JTable(libraryTableModel);
        setupTable(libraryTable, new int[]{250, 150, 120, 100, 120});
        JScrollPane libraryScroll = new JScrollPane(libraryTable);
        mainTabbedPane.addTab("Library", libraryScroll);

        // Assignments Tab with editable Submitted checkbox
        assignmentsTableModel = new DefaultTableModel(null, new String[]{
                "Title", "Description", "Deadline", "Submitted"
        }) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 3;
            }
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 3 ? Boolean.class : String.class;
            }
        };
        assignmentsTable = new JTable(assignmentsTableModel);
        setupTable(assignmentsTable, new int[]{200, 350, 120, 80});
        assignmentsTableModel.addTableModelListener(e -> {
            if (e.getColumn() == 3 && e.getType() == TableModelEvent.UPDATE) {
                updateAssignmentSubmittedStatus(e.getFirstRow());
            }
        });
        JScrollPane assignmentsScroll = new JScrollPane(assignmentsTable);
        mainTabbedPane.addTab("Assignments", assignmentsScroll);

        // Profile Tab
        JPanel profilePanel = new JPanel();
        profilePanel.setBackground(Color.WHITE);
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));

        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            ImageIcon icon = new ImageIcon(profileImagePath);
            Image image = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel picLabel = new JLabel(new ImageIcon(image));
            picLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            profilePanel.add(picLabel);
        }

        JLabel nameLabel = new JLabel(userName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(new Color(0x3a0057));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.add(nameLabel);

        JLabel emailLabel = new JLabel(userEmail, SwingConstants.CENTER);
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailLabel.setForeground(new Color(0x555555));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profilePanel.add(emailLabel);

        mainTabbedPane.addTab("Profile", profilePanel);

        add(mainTabbedPane, BorderLayout.CENTER);
    }

    private void setupTable(JTable table, int[] colWidths) {
        table.setRowHeight(50);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setGridColor(Color.GRAY);
        table.setShowGrid(true);
        table.getTableHeader().setReorderingAllowed(false);
        TableColumnModel colModel = table.getColumnModel();
        for (int i = 0; i < colWidths.length && i < colModel.getColumnCount(); i++) {
            colModel.getColumn(i).setPreferredWidth(colWidths[i]);
        }
        TableCellRenderer wrapRenderer = new TextAreaRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (!Boolean.class.equals(table.getColumnClass(i))) {
                colModel.getColumn(i).setCellRenderer(wrapRenderer);
            }
        }
    }

    static class TextAreaRenderer extends JTextArea implements TableCellRenderer {
        public TextAreaRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText(value == null ? "" : value.toString());
            setFont(table.getFont());
            setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            setSize(table.getColumnModel().getColumn(column).getWidth(), Short.MAX_VALUE);
            int preferredHeight = getPreferredSize().height;
            if (table.getRowHeight(row) != preferredHeight) {
                table.setRowHeight(row, preferredHeight);
            }
            return this;
        }
    }

    private void loadTrackingData() {
        trackingTableModel.setRowCount(0);
        String sql = "SELECT date, reading_hours, prayer_hours, bodybuilding_hours, " +
                "friend_connection_hours, gift_giving_hours, newspaper_hours, remarks " +
                "FROM routine_tracker WHERE user_id = ? AND date >= CURDATE() - INTERVAL 30 DAY ORDER BY date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getDate("date"));
                    row.add(rs.getFloat("reading_hours"));
                    row.add(rs.getFloat("prayer_hours"));
                    row.add(rs.getFloat("bodybuilding_hours"));
                    row.add(rs.getFloat("friend_connection_hours"));
                    row.add(rs.getFloat("gift_giving_hours"));
                    row.add(rs.getFloat("newspaper_hours"));
                    row.add(rs.getString("remarks"));
                    trackingTableModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            showError("Error loading tracking data: " + ex.getMessage());
        }
    }

    private void loadLibraryData() {
        libraryTableModel.setRowCount(0);
        String sql = "SELECT title, author, isbn, published_year, available_copies FROM library ORDER BY title";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("title"));
                row.add(rs.getString("author"));
                row.add(rs.getString("isbn"));
                row.add(rs.getInt("published_year"));
                row.add(rs.getInt("available_copies"));
                libraryTableModel.addRow(row);
            }
        } catch (SQLException ex) {
            showError("Error loading library data: " + ex.getMessage());
        }
    }

    private void loadAssignmentsData() {
        assignmentsTableModel.setRowCount(0);
        String sql = "SELECT title, description, deadline, submitted FROM assignments WHERE user_id = ? ORDER BY deadline DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString("title"));
                    row.add(rs.getString("description"));
                    row.add(rs.getDate("deadline"));
                    row.add(rs.getBoolean("submitted"));
                    assignmentsTableModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            showError("Error loading assignments data: " + ex.getMessage());
        }
    }

    private void updateAssignmentSubmittedStatus(int row) {
        if (row < 0 || row >= assignmentsTableModel.getRowCount()) return;

        Boolean submitted = (Boolean) assignmentsTableModel.getValueAt(row, 3);
        String title = (String) assignmentsTableModel.getValueAt(row, 0);

        if (title == null) return;

        String sql = "UPDATE assignments SET submitted = ? WHERE user_id = ? AND title = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, submitted);
            pstmt.setInt(2, userId);
            pstmt.setString(3, title);

            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                showError("Failed to update assignment status for " + title);
            }
        } catch (SQLException ex) {
            showError("Database error updating assignment: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Menu bar setup with View/Add/Edit/Delete options
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(0x3a0057).darker());

        String[] menus = {
                "Routine Tracker", "Attendance", "Courses", "Results", "Notices",
                "Assignments", "Library", "Profile", "Settings", "User List", "Logout"
        };

        for (String menuName : menus) {
            JMenu menu = new JMenu(menuName);
            menu.setForeground(Color.WHITE);
            menu.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            JMenuItem viewItem = new JMenuItem("View");
            JMenuItem addItem = new JMenuItem("Add");
            JMenuItem editItem = new JMenuItem("Edit");
            JMenuItem deleteItem = new JMenuItem("Delete");

            switch (menuName) {
                case "Routine Tracker":
                    viewItem.addActionListener(e -> openRoutineTracker());
                    addItem.addActionListener(e -> openAddRoutineForm());
                    editItem.addActionListener(e -> openEditRoutine());
                    deleteItem.addActionListener(e -> openDeleteRoutine());
                    break;

                case "Attendance":
                    viewItem.addActionListener(e -> openAttendance());
                    addItem.addActionListener(e -> openAddAttendance());
                    editItem.addActionListener(e -> openEditAttendance());
                    deleteItem.addActionListener(e -> openDeleteAttendance());
                    break;

                case "Courses":
                    viewItem.addActionListener(e -> openCourses());
                    addItem.addActionListener(e -> openAddCSEStudentForm());
                    editItem.addActionListener(e -> openEditCourse());
                    deleteItem.addActionListener(e -> openDeleteCourse());
                    break;

                case "Results":
                    viewItem.addActionListener(e -> openResults());
                    addItem.addActionListener(e -> openAddResult());
                    editItem.addActionListener(e -> openEditResult());
                    deleteItem.addActionListener(e -> openDeleteResult());
                    break;

                case "Notices":
                    viewItem.addActionListener(e -> openNotices());
                    addItem.addActionListener(e -> openAddNotice());
                    editItem.addActionListener(e -> openEditNotice());
                    deleteItem.addActionListener(e -> openDeleteNotice());
                    break;

                case "Assignments":
                    viewItem.addActionListener(e -> openAssignments());
                    addItem.addActionListener(e -> openAddAssignment());
                    editItem.addActionListener(e -> openEditAssignment());
                    deleteItem.addActionListener(e -> openDeleteAssignment());
                    break;

                case "Library":
                    viewItem.addActionListener(e -> openLibrary());
                    addItem.addActionListener(e -> openAddLibrary());
                    editItem.addActionListener(e -> openEditLibrary());
                    deleteItem.addActionListener(e -> openDeleteLibrary());
                    break;

                case "Profile":
                    viewItem.addActionListener(e -> openProfile());
                    addItem.addActionListener(e -> showFeatureNotImplemented("Add Profile"));
                    editItem.addActionListener(e -> openEditProfile());
                    deleteItem.addActionListener(e -> showFeatureNotImplemented("Delete Profile"));
                    break;

                case "Settings":
                    viewItem.addActionListener(e -> openSettings());
                    addItem.addActionListener(e -> showFeatureNotImplemented("Add Settings"));
                    editItem.addActionListener(e -> showFeatureNotImplemented("Edit Settings"));
                    deleteItem.addActionListener(e -> showFeatureNotImplemented("Delete Settings"));
                    break;

                case "User List":
                    viewItem.addActionListener(e -> openUserListView());
                    addItem.addActionListener(e -> openAddUser());
                    editItem.addActionListener(e -> openEditUser());
                    deleteItem.addActionListener(e -> openDeleteUser());
                    break;

                case "Logout":
                    viewItem.addActionListener(e -> logout());
                    addItem.setEnabled(false);
                    editItem.setEnabled(false);
                    deleteItem.setEnabled(false);
                    break;

                default:
                    viewItem.addActionListener(e -> showFeatureNotImplemented("View " + menuName));
                    addItem.addActionListener(e -> showFeatureNotImplemented("Add " + menuName));
                    editItem.addActionListener(e -> showFeatureNotImplemented("Edit " + menuName));
                    deleteItem.addActionListener(e -> showFeatureNotImplemented("Delete " + menuName));
            }

            menu.add(viewItem);
            menu.add(addItem);
            menu.add(editItem);
            menu.add(deleteItem);

            menuBar.add(menu);
        }

        setJMenuBar(menuBar);
    }

    // Stub or actual methods for Add/Edit/Delete actions:
    private void openAddRoutineForm()            { new AddRoutineForm(userId).setVisible(true); }
    private void openEditRoutine()                { showFeatureNotImplemented("Edit Routine Tracker"); }
    private void openDeleteRoutine()              { showFeatureNotImplemented("Delete Routine Tracker"); }

    private void openAddAttendance()              { new Attendance().setVisible(true); }
    private void openEditAttendance()             { showFeatureNotImplemented("Edit Attendance"); }
    private void openDeleteAttendance()           { showFeatureNotImplemented("Delete Attendance"); }

    private void openCourses()                     { openAddCSEStudentForm(); }
    private void openEditCourse()                  { showFeatureNotImplemented("Edit Courses"); }
    private void openDeleteCourse()                { showFeatureNotImplemented("Delete Courses"); }

    private void openAddResult()                   { new Results().setVisible(true); }
    private void openEditResult()                  { showFeatureNotImplemented("Edit Result"); }
    private void openDeleteResult()                { showFeatureNotImplemented("Delete Result"); }

    private void openAddNotice()                   { new Notices().setVisible(true); }
    private void openEditNotice()                  { showFeatureNotImplemented("Edit Notice"); }
    private void openDeleteNotice()                { showFeatureNotImplemented("Delete Notice"); }

    private void openAddAssignment()               { new Assignments().setVisible(true); }
    private void openEditAssignment()              { showFeatureNotImplemented("Edit Assignment"); }
    private void openDeleteAssignment()            { showFeatureNotImplemented("Delete Assignment"); }

    private void openAddLibrary()                  { new Library().setVisible(true); }
    private void openEditLibrary()                 { showFeatureNotImplemented("Edit Library Entry"); }
    private void openDeleteLibrary()               { showFeatureNotImplemented("Delete Library Entry"); }

    private void openEditProfile()                 { showFeatureNotImplemented("Edit Profile"); }

    private void openAddUser()                     { showFeatureNotImplemented("Add User"); }
    private void openEditUser()                    { showFeatureNotImplemented("Edit User"); }
    private void openDeleteUser()                  { showFeatureNotImplemented("Delete User"); }


    private void openRoutineTracker()              { new AddRoutineForm(userId).setVisible(true); }
    private void openAttendance()                  { new Attendance(); }
    private void openAddCSEStudentForm()           { new AddCSEStudentForm(userId).setVisible(true); }
    private void openResults()                      { new Results(); }
    private void openNotices()                      { new Notices(); }
    private void openAssignments()                  { mainTabbedPane.setSelectedIndex(2); }
    private void openLibrary()                      { mainTabbedPane.setSelectedIndex(1); }
    private void openProfile()                      { mainTabbedPane.setSelectedIndex(3); }
    private void openSettings()                     { new Settings(this); }
    private void openUserListView()                 { new UserListView().setVisible(true); }
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                "Logout Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new SignInForm().setVisible(true);
        }
    }
    private void showFeatureNotImplemented(String feature) {
        JOptionPane.showMessageDialog(this,
                feature + " is not implemented yet.",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Main method for quick testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard(1, "John Doe", "john@example.com", null));
    }
}
