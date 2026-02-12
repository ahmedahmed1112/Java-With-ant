package ui;

import model.User;
import util.FileManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ManageUsersFrame extends JPanel {

    private static final String USERS_FILE = "data/users.txt";
    private static final String STUDENTS_FILE = "data/students.txt";
    private static final String LECTURERS_FILE = "data/lecturers.txt";
    private static final String MODULES_FILE = "data/modules.txt";
    private static final String LEADER_LECTURER_FILE = "data/leader_lecturer.txt";
    private static final String STUDENT_CLASSES_FILE = "data/student_classes.txt";
    private static final String GRADES_FILE = "data/grades.txt";
    private static final String FEEDBACK_FILE = "data/feedback.txt";
    private static final String COMMENTS_FILE = "data/comments.txt";

    private DefaultTableModel tableModel;
    private JTable table;

    private JTextField txtId, txtUsername, txtName, txtEmail, txtPhone;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbGender;
    private JComboBox<String> cmbRole;
    private JSpinner spnAge;

    private JTextField txtSearch;

    private JLabel lblTotal;
    private JLabel lblAdmins;

    private final List<User> allUsers = new ArrayList<>();

    public ManageUsersFrame() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        add(root, BorderLayout.CENTER);

        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(UIUtils.title("Manage Users"));
        titles.add(UIUtils.muted("CRUD users stored in data/users.txt (full schema)"));

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTop.setOpaque(false);

        txtSearch = UIUtils.modernTextField();
        txtSearch.setPreferredSize(new Dimension(340, 38));
        txtSearch.setToolTipText("Search by id / username / name / role / email / phone");

        JButton btnSearch = UIUtils.primaryButton("Search");
        JButton btnClearSearch = UIUtils.ghostButton("Clear");

        rightTop.add(txtSearch);
        rightTop.add(btnSearch);
        rightTop.add(btnClearSearch);

        header.add(titles, BorderLayout.WEST);
        header.add(rightTop, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // ===== Main Content =====
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(14, 0, 0, 0));
        root.add(content, BorderLayout.CENTER);

        // Stats
        JPanel stats = new JPanel(new GridLayout(1, 2, 14, 0));
        stats.setOpaque(false);

        lblTotal = new JLabel("0");
        lblAdmins = new JLabel("0");
        stats.add(statCard("Total Users", "All roles in users.txt", lblTotal));
        stats.add(statCard("Admins", "Users with role ADMIN", lblAdmins));

        content.add(stats, BorderLayout.NORTH);

        // Split
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setOpaque(false);
        split.setBorder(null);
        split.setDividerSize(10);
        split.setContinuousLayout(true);

        split.setLeftComponent(buildFormCard());
        split.setRightComponent(buildTableCard());
        split.setResizeWeight(0.40);

        content.add(split, BorderLayout.CENTER);

        // Actions
        btnSearch.addActionListener(e -> applySearch());
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            refreshTable(allUsers);
        });

        // Load
        loadUsersFromFile();
        refreshTable(allUsers);
        updateStats();
    }

    private JPanel statCard(String title, String desc, JLabel valueLabel) {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 8));

        JLabel t = new JLabel(title);
        t.setForeground(Theme.TEXT);
        t.setFont(UIUtils.font(14, Font.BOLD));

        JLabel d = new JLabel(desc);
        d.setForeground(Theme.MUTED);
        d.setFont(UIUtils.font(12, Font.PLAIN));

        valueLabel.setForeground(Theme.TEXT);
        valueLabel.setFont(UIUtils.font(26, Font.BOLD));

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setOpaque(false);
        top.add(t);
        top.add(d);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFormCard() {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JLabel t = new JLabel("User Details");
        t.setForeground(Theme.TEXT);
        t.setFont(UIUtils.font(16, Font.BOLD));
        card.add(t, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(6, 0, 6, 0);

        txtId = UIUtils.modernTextField();
        txtId.setEditable(false);
        txtId.setToolTipText("Auto-generated based on role");
        txtName = UIUtils.modernTextField();
        txtUsername = UIUtils.modernTextField();
        txtPassword = UIUtils.modernPasswordField();
        txtEmail = UIUtils.modernTextField();
        txtPhone = UIUtils.modernTextField();

        cmbGender = new JComboBox<>(new String[]{"Male", "Female"});
        cmbGender.setBackground(new Color(18, 22, 30));
        cmbGender.setForeground(Color.WHITE);

        cmbRole = new JComboBox<>(new String[]{"ADMIN", "STUDENT", "LECTURER", "LEADER"});
        cmbRole.setBackground(new Color(18, 22, 30));
        cmbRole.setForeground(Color.WHITE);
        cmbRole.addActionListener(e -> {
            // Auto-generate ID when role changes (only for new users)
            if (txtId.getText().trim().isEmpty() || !isExistingUser(txtId.getText().trim())) {
                txtId.setText(generateNextId((String) cmbRole.getSelectedItem()));
            }
        });

        spnAge = new JSpinner(new SpinnerNumberModel(0, 0, 120, 1));
        spnAge.setPreferredSize(new Dimension(100, 32));

        addField(form, gc, 0, "User ID (auto-generated by role)", txtId);
        addField(form, gc, 2, "Username", txtUsername);
        addField(form, gc, 4, "Password (leave empty to keep old on update)", txtPassword);
        addField(form, gc, 6, "Full Name", txtName);
        addField(form, gc, 8, "Gender", cmbGender);
        addField(form, gc, 10, "Email", txtEmail);
        addField(form, gc, 12, "Phone", txtPhone);
        addField(form, gc, 14, "Age", spnAge);
        addField(form, gc, 16, "Role", cmbRole);

        JPanel btnRow = new JPanel(new GridLayout(2, 2, 10, 10));
        btnRow.setOpaque(false);

        JButton btnAdd = UIUtils.primaryButton("Add");
        JButton btnUpdate = UIUtils.primaryButton("Update");
        JButton btnDelete = UIUtils.dangerButton("Delete");
        JButton btnClear = UIUtils.ghostButton("Clear Form");

        btnRow.add(btnAdd);
        btnRow.add(btnUpdate);
        btnRow.add(btnDelete);
        btnRow.add(btnClear);

        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnClear.addActionListener(e -> clearForm());

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        card.add(formScroll, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);

        // Auto-generate initial ID
        txtId.setText(generateNextId((String) cmbRole.getSelectedItem()));

        return card;
    }

    private void addField(JPanel panel, GridBagConstraints gc, int y, String labelText, JComponent field) {
        JLabel label = UIUtils.muted(labelText);
        gc.gridy = y;
        panel.add(label, gc);

        gc.gridy = y + 1;
        panel.add(field, gc);
    }

    private JPanel buildTableCard() {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel t = new JLabel("Users List");
        t.setForeground(Theme.TEXT);
        t.setFont(UIUtils.font(16, Font.BOLD));
        card.add(t, BorderLayout.NORTH);

        String[] cols = {"ID", "Username", "Name", "Gender", "Email", "Phone", "Age", "Role"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        UIUtils.applyTableStyle(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        applyColumnWidths();

        table.setDefaultRenderer(Object.class, usersCellRenderer());

        JScrollPane sp = new JScrollPane(table);
        UIUtils.styleScrollPane(sp);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        card.add(sp, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> fillFormFromSelected());

        return card;
    }

    private void applyColumnWidths() {
        if (table.getColumnModel().getColumnCount() < 8) return;
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(260);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(140);
    }

    private TableCellRenderer usersCellRenderer() {
        return (tbl, value, isSelected, hasFocus, row, col) -> {
            String text = value == null ? "" : value.toString();
            JLabel cell = new JLabel(text);
            cell.setOpaque(true);
            cell.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            cell.setFont(UIUtils.font(13, Font.PLAIN));

            if (isSelected) {
                cell.setBackground(new Color(70, 130, 180));
                cell.setForeground(Color.WHITE);
                return cell;
            }

            cell.setBackground(row % 2 == 0 ? new Color(18, 22, 30) : new Color(24, 29, 40));

            if (col == 0) {
                cell.setForeground(new Color(200, 220, 255));
            } else if (col == 7) {
                String role = text.toUpperCase();
                if (role.equals("ADMIN")) cell.setForeground(new Color(120, 160, 255));
                else if (role.equals("LEADER")) cell.setForeground(new Color(95, 200, 140));
                else if (role.equals("LECTURER")) cell.setForeground(new Color(240, 190, 85));
                else cell.setForeground(Color.WHITE);
            } else {
                cell.setForeground(Color.WHITE);
            }

            return cell;
        };
    }

    // ---------- Data ----------
    private void loadUsersFromFile() {
        allUsers.clear();
        List<String> lines = FileManager.readAll(USERS_FILE);

        for (String line : lines) {
            if (line == null) continue;
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] p = line.split("\\|");

            if (p.length >= 9) {
                String id = safe(p, 0);
                String username = safe(p, 1);
                String password = safe(p, 2);
                String name = safe(p, 3);
                String gender = safe(p, 4);
                String email = safe(p, 5);
                String phone = safe(p, 6);
                int age = parseIntSafe(safe(p, 7), 0);
                String role = safe(p, 8);

                allUsers.add(User.create(id, username, password, name, gender, email, phone, age, role));
            }
        }
    }

    private void refreshTable(List<User> list) {
        tableModel.setRowCount(0);
        for (User u : list) {
            tableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getName(),
                    u.getGender(),
                    u.getEmail(),
                    u.getPhone(),
                    u.getAge(),
                    u.getRole()
            });
        }
        applyColumnWidths();
    }

    private void updateStats() {
        lblTotal.setText(String.valueOf(allUsers.size()));
        long admins = allUsers.stream().filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase("ADMIN")).count();
        lblAdmins.setText(String.valueOf(admins));
    }

    // ---------- Actions ----------
    private void applySearch() {
        String q = txtSearch.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            refreshTable(allUsers);
            return;
        }

        List<User> filtered = new ArrayList<>();
        for (User u : allUsers) {
            if (contains(u.getUserId(), q) ||
                contains(u.getUsername(), q) ||
                contains(u.getName(), q) ||
                contains(u.getRole(), q) ||
                contains(u.getEmail(), q) ||
                contains(u.getPhone(), q)) {
                filtered.add(u);
            }
        }
        refreshTable(filtered);
    }

    private void addUser() {
        String role = (String) cmbRole.getSelectedItem();
        String id = generateNextId(role);
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String name = txtName.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        int age = (Integer) spnAge.getValue();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill: Username, Password, Name.");
            return;
        }

        if (!email.isEmpty() && !email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Invalid email.");
            return;
        }

        if (!phone.isEmpty() && !phone.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Phone must be numeric.");
            return;
        }

        for (User u : allUsers) {
            if (u.getUserId() != null && u.getUserId().equalsIgnoreCase(id)) {
                JOptionPane.showMessageDialog(this, "User ID already exists.");
                return;
            }
            if (u.getUsername() != null && u.getUsername().equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists.");
                return;
            }
        }

        User newUser = User.create(id, username, password, name, gender, email, phone, age, role);
        FileManager.append(USERS_FILE, newUser.toString());

        if ("STUDENT".equalsIgnoreCase(role)) upsertStudentFromUser(newUser);
        if ("LECTURER".equalsIgnoreCase(role)) upsertLecturerFromUser(newUser);

        loadUsersFromFile();
        refreshTable(allUsers);
        updateStats();
        clearForm();

        JOptionPane.showMessageDialog(this, "User added successfully (ID: " + id + ").");
    }

    private void updateUser() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a user from the table first.");
            return;
        }

        User existing = findById(id);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "User not found in file. Reload and try again.");
            return;
        }

        String username = txtUsername.getText().trim();
        String passwordInput = new String(txtPassword.getPassword()).trim();
        String password = passwordInput.isEmpty() ? safeStr(existing.getPassword()) : passwordInput;

        String name = txtName.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String role = (String) cmbRole.getSelectedItem();
        int age = (Integer) spnAge.getValue();

        if (username.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill: Username, Name.");
            return;
        }

        if (!email.isEmpty() && !email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Invalid email.");
            return;
        }

        if (!phone.isEmpty() && !phone.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Phone must be numeric.");
            return;
        }

        for (User u : allUsers) {
            if (u.getUserId() != null && u.getUserId().equalsIgnoreCase(id)) continue;
            if (u.getUsername() != null && u.getUsername().equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists.");
                return;
            }
        }

        // Check if anything actually changed
        boolean changed = !username.equals(safeStr(existing.getUsername()))
                || !password.equals(safeStr(existing.getPassword()))
                || !name.equals(safeStr(existing.getName()))
                || !gender.equals(safeStr(existing.getGender()))
                || !email.equals(safeStr(existing.getEmail()))
                || !phone.equals(safeStr(existing.getPhone()))
                || age != existing.getAge()
                || !role.equalsIgnoreCase(safeStr(existing.getRole()));

        if (!changed) {
            JOptionPane.showMessageDialog(this, "No changes detected.");
            return;
        }

        boolean roleChanging = !safeStr(existing.getRole()).equalsIgnoreCase(role);
        if (roleChanging) {
            String dependencyError = buildDependencyBlock(existing);
            if (!dependencyError.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Cannot change role for user " + id + ".\n" + dependencyError,
                        "Blocked: Linked Records",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        User updated = User.create(id, username, password, name, gender, email, phone, age, role);
        FileManager.updateById(USERS_FILE, id, updated.toString());
        syncRoleFilesOnUpdate(existing, updated);

        loadUsersFromFile();
        refreshTable(allUsers);
        updateStats();
        clearForm();

        JOptionPane.showMessageDialog(this, "User updated successfully.");
    }

    private void deleteUser() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a user from the table first.");
            return;
        }

        User existing = findById(id);
        if (existing != null) {
            String dependencyError = buildDependencyBlock(existing);
            if (!dependencyError.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Cannot delete user " + id + ".\n" + dependencyError,
                        "Blocked: Linked Records",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete selected user: " + id + " ?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        FileManager.deleteById(USERS_FILE, id);

        if (existing != null) {
            if ("STUDENT".equalsIgnoreCase(existing.getRole())) removeStudentByIdentity(existing.getUsername(), existing.getUserId());
            if ("LECTURER".equalsIgnoreCase(existing.getRole())) removeLecturerByUsername(existing.getUsername());
        }

        loadUsersFromFile();
        refreshTable(allUsers);
        updateStats();
        clearForm();

        JOptionPane.showMessageDialog(this, "User deleted successfully.");
    }

    private void fillFormFromSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        txtId.setText(String.valueOf(tableModel.getValueAt(row, 0)));
        txtUsername.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        txtName.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        cmbGender.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 3)));
        txtEmail.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        txtPhone.setText(String.valueOf(tableModel.getValueAt(row, 5)));
        try {
            spnAge.setValue(Integer.parseInt(String.valueOf(tableModel.getValueAt(row, 6))));
        } catch (Exception ex) {
            spnAge.setValue(0);
        }
        cmbRole.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 7)));
        txtPassword.setText("");
    }

    private void clearForm() {
        txtUsername.setText("");
        txtPassword.setText("");
        txtName.setText("");
        cmbGender.setSelectedIndex(0);
        txtEmail.setText("");
        txtPhone.setText("");
        spnAge.setValue(0);
        cmbRole.setSelectedIndex(0);
        txtId.setText(generateNextId((String) cmbRole.getSelectedItem()));
    }

    // ---------- ID Generation ----------
    private String generateNextId(String role) {
        if (role == null) return "";
        String prefix;
        switch (role.trim().toUpperCase()) {
            case "ADMIN": prefix = "A"; break;
            case "LEADER": prefix = "L"; break;
            case "LECTURER": prefix = "T"; break;
            case "STUDENT": prefix = "S"; break;
            default: return "";
        }

        int max = 0;
        for (User u : allUsers) {
            String uid = safeStr(u.getUserId()).toUpperCase();
            if (uid.startsWith(prefix) && uid.length() > 1) {
                try {
                    int val = Integer.parseInt(uid.substring(prefix.length()));
                    if (val > max) max = val;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("%s%03d", prefix, max + 1);
    }

    private boolean isExistingUser(String id) {
        for (User u : allUsers) {
            if (u.getUserId() != null && u.getUserId().equalsIgnoreCase(id)) return true;
        }
        return false;
    }

    private String buildDependencyBlock(User user) {
        if (user == null) return "";

        String role = safeStr(user.getRole()).toUpperCase();
        String userId = safeStr(user.getUserId());
        String username = safeStr(user.getUsername());
        String studentId = findStudentIdForUser(user);
        List<String> dependencies = new ArrayList<>();

        if ("LEADER".equals(role)) {
            if (fileHasValueInColumn(MODULES_FILE, 4, userId, false)) {
                dependencies.add("data/modules.txt -> leaderId references this user");
            }
            if (fileHasValueInColumn(LEADER_LECTURER_FILE, 0, userId, true)) {
                dependencies.add("data/leader_lecturer.txt -> leader assignments exist");
            }
        }

        if ("LECTURER".equals(role)) {
            if (fileHasValueInColumn(MODULES_FILE, 5, userId, false)) {
                dependencies.add("data/modules.txt -> lecturer is assigned to module(s)");
            }
            if (fileHasValueInColumn(LEADER_LECTURER_FILE, 1, userId, true)) {
                dependencies.add("data/leader_lecturer.txt -> lecturer is assigned to leader");
            }
            if (fileHasAnyValueInColumn(GRADES_FILE, 5, userId, username, false)) {
                dependencies.add("data/grades.txt -> lecturer has keyed-in marks");
            }
            if (fileHasAnyValueInColumn(FEEDBACK_FILE, 3, userId, username, false)) {
                dependencies.add("data/feedback.txt -> lecturer has feedback records");
            }
            if (fileHasAnyValueInColumn(COMMENTS_FILE, 2, userId, username, false)) {
                dependencies.add("data/comments.txt -> lecturer has student comments");
            }
        }

        if ("STUDENT".equals(role)) {
            if (!studentId.isEmpty() && fileHasValueInColumn(STUDENT_CLASSES_FILE, 0, studentId, false)) {
                dependencies.add("data/student_classes.txt -> class registrations exist");
            }
            if (!studentId.isEmpty() && fileHasValueInColumn(GRADES_FILE, 2, studentId, false)) {
                dependencies.add("data/grades.txt -> result records exist");
            }
            if (!studentId.isEmpty() && fileHasValueInColumn(FEEDBACK_FILE, 2, studentId, false)) {
                dependencies.add("data/feedback.txt -> feedback records exist");
            }
            if (!studentId.isEmpty() && fileHasValueInColumn(COMMENTS_FILE, 1, studentId, false)) {
                dependencies.add("data/comments.txt -> submitted comments exist");
            }
        }

        if (dependencies.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("Resolve these dependencies first:\n");
        for (String dep : dependencies) {
            sb.append("- ").append(dep).append("\n");
        }
        return sb.toString().trim();
    }

    private boolean fileHasValueInColumn(String filePath, int columnIndex, String targetValue, boolean skipHeader) {
        String value = safeStr(targetValue);
        if (value.isEmpty()) return false;

        List<String> lines = FileManager.readAll(filePath);
        boolean first = true;
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            if (skipHeader && first) {
                first = false;
                continue;
            }
            first = false;

            String[] p = line.split("\\|", -1);
            if (columnIndex < p.length && safe(p, columnIndex).equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean fileHasAnyValueInColumn(String filePath, int columnIndex, String value1, String value2, boolean skipHeader) {
        Set<String> targets = new LinkedHashSet<>();
        if (!safeStr(value1).isEmpty()) targets.add(safeStr(value1).toUpperCase());
        if (!safeStr(value2).isEmpty()) targets.add(safeStr(value2).toUpperCase());
        if (targets.isEmpty()) return false;

        List<String> lines = FileManager.readAll(filePath);
        boolean first = true;
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            if (skipHeader && first) {
                first = false;
                continue;
            }
            first = false;

            String[] p = line.split("\\|", -1);
            if (columnIndex >= p.length) continue;
            String cell = safe(p, columnIndex).toUpperCase();
            if (targets.contains(cell)) return true;
        }
        return false;
    }

    private String findStudentIdForUser(User user) {
        if (user == null) return "";
        String userId = safeStr(user.getUserId());
        String username = safeStr(user.getUsername());
        if (userId.isEmpty() && username.isEmpty()) return "";

        for (String line : FileManager.readAll(STUDENTS_FILE)) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length >= 9 && !username.isEmpty() && username.equalsIgnoreCase(safe(p, 0))) {
                return safe(p, 7);
            }
            if (p.length >= 2 && p.length < 9 && !userId.isEmpty() && userId.equalsIgnoreCase(safe(p, 1))) {
                return safe(p, 0);
            }
        }
        return "";
    }

    private void syncRoleFilesOnUpdate(User oldUser, User newUser) {
        String oldRole = safeStr(oldUser.getRole()).toUpperCase();
        String newRole = safeStr(newUser.getRole()).toUpperCase();
        boolean usernameChanged = !safeStr(oldUser.getUsername()).equalsIgnoreCase(safeStr(newUser.getUsername()));

        if (!oldRole.equals(newRole)) {
            if ("STUDENT".equals(oldRole)) removeStudentByIdentity(oldUser.getUsername(), oldUser.getUserId());
            if ("LECTURER".equals(oldRole)) removeLecturerByUsername(oldUser.getUsername());
        }

        if ("STUDENT".equals(newRole) && usernameChanged) {
            removeStudentByIdentity(oldUser.getUsername(), oldUser.getUserId());
        }
        if ("LECTURER".equals(newRole) && usernameChanged) {
            removeLecturerByUsername(oldUser.getUsername());
        }

        if ("STUDENT".equals(newRole)) upsertStudentFromUser(newUser);
        if ("LECTURER".equals(newRole)) upsertLecturerFromUser(newUser);
    }

    private void upsertStudentFromUser(User user) {
        if (user == null) return;
        String username = safeStr(user.getUsername());
        String userId = safeStr(user.getUserId());
        if (username.isEmpty() || userId.isEmpty()) return;

        String existingStudentId = "";
        String existingModuleId = "";
        boolean matchedExtended = false;
        boolean matched = false;

        List<String> lines = FileManager.readAll(STUDENTS_FILE);
        List<String> out = new ArrayList<>();

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);

            boolean isExtended = p.length >= 9 && username.equalsIgnoreCase(safe(p, 0));
            boolean isCompact = p.length >= 2 && p.length < 9 && userId.equalsIgnoreCase(safe(p, 1));

            if (!isExtended && !isCompact) {
                out.add(line);
                continue;
            }

            // If duplicates exist, keep only one normalized row.
            if (matched) continue;

            matched = true;
            if (isExtended) {
                matchedExtended = true;
                existingStudentId = safe(p, 7);
                existingModuleId = (p.length >= 10) ? safe(p, 9) : safe(p, 8);
                if (existingStudentId.isEmpty()) existingStudentId = generateNextStudentRecordId();
                out.add(buildStudentExtendedLine(user, existingStudentId, existingModuleId));
            } else {
                existingStudentId = safe(p, 0);
                if (existingStudentId.isEmpty()) existingStudentId = generateNextStudentRecordId();
                out.add(buildStudentCompactLine(existingStudentId, userId));
            }
        }

        if (!matched) {
            String studentId = generateNextStudentRecordId();
            out.add(buildStudentCompactLine(studentId, userId));
        } else if (!matchedExtended && existingStudentId.isEmpty()) {
            // Safety: compact schema still needs a stable studentId.
            out.add(buildStudentCompactLine(generateNextStudentRecordId(), userId));
        }

        FileManager.writeAll(STUDENTS_FILE, out);
    }

    private String buildStudentExtendedLine(User user, String studentId, String moduleId) {
        String sid = safeStr(studentId).isEmpty() ? safeStr(user.getUserId()) : safeStr(studentId);
        return safeStr(user.getUsername()) + "|" +
                safeStr(user.getPassword()) + "|" +
                safeStr(user.getName()) + "|" +
                safeStr(user.getGender()) + "|" +
                safeStr(user.getEmail()) + "|" +
                safeStr(user.getPhone()) + "|" +
                user.getAge() + "|" +
                sid + "|" +
                safeStr(moduleId);
    }

    private String buildStudentCompactLine(String studentId, String userId) {
        return safeStr(studentId) + "|" + safeStr(userId);
    }

    private void upsertLecturerFromUser(User user) {
        if (user == null) return;
        String username = safeStr(user.getUsername());
        if (username.isEmpty()) return;

        String existingModuleId = "";
        String existingLeaderId = "";

        List<String> lines = FileManager.readAll(LECTURERS_FILE);
        List<String> out = new ArrayList<>();
        boolean updated = false;

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 9 && username.equalsIgnoreCase(safe(p, 0))) {
                existingModuleId = safe(p, 7);
                existingLeaderId = safe(p, 8);
                out.add(buildLecturerLine(user, existingModuleId, existingLeaderId));
                updated = true;
            } else {
                out.add(line);
            }
        }

        if (!updated) {
            out.add(buildLecturerLine(user, "", ""));
        }

        FileManager.writeAll(LECTURERS_FILE, out);
    }

    private String buildLecturerLine(User user, String moduleId, String leaderId) {
        return safeStr(user.getUsername()) + "|" +
                safeStr(user.getPassword()) + "|" +
                safeStr(user.getName()) + "|" +
                safeStr(user.getGender()) + "|" +
                safeStr(user.getEmail()) + "|" +
                safeStr(user.getPhone()) + "|" +
                user.getAge() + "|" +
                safeStr(moduleId) + "|" +
                safeStr(leaderId);
    }

    private void removeStudentByIdentity(String username, String userId) {
        String u = safeStr(username);
        String uid = safeStr(userId);
        if (u.isEmpty() && uid.isEmpty()) return;

        List<String> lines = FileManager.readAll(STUDENTS_FILE);
        List<String> out = new ArrayList<>();
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length >= 9 && !u.isEmpty() && u.equalsIgnoreCase(safe(p, 0))) continue;
            if (p.length >= 2 && p.length < 9 && !uid.isEmpty() && uid.equalsIgnoreCase(safe(p, 1))) continue;
            out.add(line);
        }
        FileManager.writeAll(STUDENTS_FILE, out);
    }

    private void removeLecturerByUsername(String username) {
        String u = safeStr(username);
        if (u.isEmpty()) return;

        List<String> lines = FileManager.readAll(LECTURERS_FILE);
        List<String> out = new ArrayList<>();
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 9 && u.equalsIgnoreCase(safe(p, 0))) continue;
            out.add(line);
        }
        FileManager.writeAll(LECTURERS_FILE, out);
    }

    // ---------- Helpers ----------
    private User findById(String id) {
        for (User u : allUsers) {
            if (u.getUserId() != null && u.getUserId().equalsIgnoreCase(id)) return u;
        }
        return null;
    }

    private boolean contains(String s, String q) {
        if (s == null) return false;
        return s.toLowerCase().contains(q);
    }

    private String safe(String[] arr, int idx) {
        if (arr == null || idx < 0 || idx >= arr.length || arr[idx] == null) return "";
        return arr[idx].trim();
    }

    private String safeStr(String s) {
        return s == null ? "" : s.trim();
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return def; }
    }

    private String generateNextStudentRecordId() {
        int max = 0;
        List<String> lines = FileManager.readAll(STUDENTS_FILE);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            String sid = "";
            if (p.length >= 9) sid = safe(p, 7);
            else if (p.length >= 1) sid = safe(p, 0);

            String upper = sid.toUpperCase();
            if (!upper.startsWith("TP") || upper.length() <= 2) continue;
            try {
                int n = Integer.parseInt(upper.substring(2));
                if (n > max) max = n;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("TP%06d", max + 1);
    }
}
