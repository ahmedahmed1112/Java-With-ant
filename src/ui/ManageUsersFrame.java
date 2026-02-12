package ui;

import model.User;
import util.FileManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ManageUsersFrame extends JPanel {

    private static final String USERS_FILE = "data/users.txt";

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
        updateGeneratedIdFromRole();
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
        // System-generated ID (A### / L### / T### / S###).
        // Admin can change it only via Update (prompt), not by typing here.
        txtId.setEditable(false);

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

        // Auto-generate ID when role changes (for Add mode)
        cmbRole.addActionListener(e -> updateGeneratedIdFromRole());

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
                continue;
            }

            // backward compatible schema: id|name|username|password|role
            if (p.length >= 5) {
                String id = safe(p, 0);
                String name = safe(p, 1);
                String username = safe(p, 2);
                String password = safe(p, 3);
                String role = safe(p, 4);

                allUsers.add(User.create(id, username, password, name, "", "", "", 0, role));
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
        long admins = allUsers.stream()
                .filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase("ADMIN"))
                .count();
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
        String id = txtId.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String name = txtName.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        int age = (Integer) spnAge.getValue();

        // Auto-generate if not present (read-only field).
        if (id.isEmpty()) {
            id = generateNextIdForRole(role);
            txtId.setText(id);
        }

        if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill: Username, Password, Name.");
            return;
        }

        if (!isValidIdForRole(id, role)) {
            JOptionPane.showMessageDialog(this, "Invalid ID for role. Expected: " + getPrefixForRole(role) + "###");
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

        // IMPORTANT: Do not use toString() here because some subclasses override it for UI text.
        FileManager.append(USERS_FILE, toUserLine(newUser));

        loadUsersFromFile();
        refreshTable(allUsers);
        updateStats();
        clearForm();

        JOptionPane.showMessageDialog(this, "User added successfully.");
    }

    private void updateUser() {
        String oldId = txtId.getText().trim();
        if (oldId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a user from the table first.");
            return;
        }

        User existing = findById(oldId);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "User not found in file. Reload and try again.");
            return;
        }

        // Allow admin to change ID (but keep the field read-only in the form).
        String role = (String) cmbRole.getSelectedItem();
        String newId = JOptionPane.showInputDialog(this,
                "User ID (format: " + getPrefixForRole(role) + "###).\nLeave blank to keep current.",
                oldId);

        if (newId == null) return; // cancelled
        newId = newId.trim();
        if (newId.isEmpty()) newId = oldId;

        if (!newId.equalsIgnoreCase(oldId)) {
            if (!isValidIdForRole(newId, role)) {
                JOptionPane.showMessageDialog(this, "Invalid ID for role. Expected: " + getPrefixForRole(role) + "###");
                return;
            }
            for (User u : allUsers) {
                if (u.getUserId() != null && u.getUserId().equalsIgnoreCase(newId)) {
                    JOptionPane.showMessageDialog(this, "User ID already exists.");
                    return;
                }
            }
        }

        String username = txtUsername.getText().trim();
        String passwordInput = new String(txtPassword.getPassword()).trim();
        String password = passwordInput.isEmpty() ? safeStr(existing.getPassword()) : passwordInput;

        String name = txtName.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
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
            if (u.getUserId() != null && u.getUserId().equalsIgnoreCase(oldId)) continue;
            if (u.getUsername() != null && u.getUsername().equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists.");
                return;
            }
        }

        User updated = User.create(newId, username, password, name, gender, email, phone, age, role);

        // IMPORTANT: Do not use toString() here because some subclasses override it for UI text.
        FileManager.updateById(USERS_FILE, oldId, toUserLine(updated));

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

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete selected user: " + id + " ?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        FileManager.deleteById(USERS_FILE, id);

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
        txtId.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtName.setText("");
        cmbGender.setSelectedIndex(0);
        txtEmail.setText("");
        txtPhone.setText("");
        spnAge.setValue(0);
        cmbRole.setSelectedIndex(0);

        // Regenerate ID based on the role (after resetting role).
        updateGeneratedIdFromRole();
    }

    // ---------- ID / File helpers ----------
    private void updateGeneratedIdFromRole() {
        // If a row is selected, we are editing an existing user; do not overwrite its ID.
        if (table != null && table.getSelectedRow() >= 0) return;

        String role = (String) cmbRole.getSelectedItem();
        if (role == null) role = "";
        txtId.setText(generateNextIdForRole(role));
    }

    private String generateNextIdForRole(String role) {
        String prefix = getPrefixForRole(role);
        int max = 0;

        for (User u : allUsers) {
            String id = u.getUserId();
            if (id == null) continue;
            id = id.trim().toUpperCase();
            if (!id.startsWith(prefix) || id.length() != 4) continue;

            String num = id.substring(1);
            try {
                int n = Integer.parseInt(num);
                if (n > max) max = n;
            } catch (Exception ignored) {}
        }

        return prefix + String.format("%03d", max + 1);
    }

    private String getPrefixForRole(String role) {
        if (role == null) return "U";
        String r = role.trim().toUpperCase();
        switch (r) {
            case "ADMIN": return "A";
            case "LEADER": return "L";
            case "LECTURER": return "T";
            case "STUDENT": return "S";
            default: return "U";
        }
    }

    private boolean isValidIdForRole(String id, String role) {
        if (id == null) return false;
        String prefix = getPrefixForRole(role);
        return id.trim().toUpperCase().matches(prefix + "\\d{3}");
    }

    private String toUserLine(User u) {
        // Always write the full 9-field schema:
        // id|username|password|name|gender|email|phone|age|role
        return safeStr(u.getUserId()) + "|" +
                safeStr(u.getUsername()) + "|" +
                safeStr(u.getPassword()) + "|" +
                safeStr(u.getName()) + "|" +
                safeStr(u.getGender()) + "|" +
                safeStr(u.getEmail()) + "|" +
                safeStr(u.getPhone()) + "|" +
                u.getAge() + "|" +
                safeStr(u.getRole());
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
}
