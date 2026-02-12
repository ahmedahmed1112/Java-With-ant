package ui;

import model.Module;
import model.User;
import service.ModuleService;
import util.FileManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AssignLecturersFrame extends JFrame {

    private final User loggedInUser;
    private final String leaderId;

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtSelectedModuleId;
    private JComboBox<String> cboLecturerId;
    private DefaultComboBoxModel<String> lecturerModel;

    private JButton btnAssign;
    private JButton btnUnassign;

    private static final String LEADER_LECTURER_FILE = "data/leader_lecturer.txt";
    private static final String USERS_FILE = "data/users.txt";

    public AssignLecturersFrame() {
        this(null);
    }

    public AssignLecturersFrame(User user) {
        this.loggedInUser = user;

        if (loggedInUser != null && loggedInUser.getUserId() != null) {
            this.leaderId = loggedInUser.getUserId().trim();
        } else {
            this.leaderId = "";
        }

        setTitle("Assign Lecturers (Leader)");
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        setContentPane(root);

        // ===== Top bar =====
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(UIUtils.title("Assign Lecturers"));
        titles.add(UIUtils.muted("Assign lecturers to your modules (modules.txt)"));

        JButton btnRefresh = UIUtils.primaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshAll());

        top.add(titles, BorderLayout.WEST);
        top.add(btnRefresh, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        // ===== Center: table + form =====
        JPanel center = new JPanel(new GridLayout(1, 2, 14, 14));
        center.setOpaque(false);

        // ---- Table Card ----
        JPanel tableCard = UIUtils.cardPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel tableTitle = new JLabel("Your Modules (Leader ID: " + (leaderId.isEmpty() ? "Unknown" : leaderId) + ")");
        tableTitle.setForeground(Theme.TEXT);
        tableTitle.setFont(UIUtils.font(14, Font.BOLD));
        tableCard.add(tableTitle, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Module ID", "Name", "Code", "Credits", "Lecturer ID"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        UIUtils.applyTableStyle(table);

        JScrollPane sp = new JScrollPane(table);
        UIUtils.styleScrollPane(sp);
        sp.setBorder(new EmptyBorder(10, 0, 0, 0));
        tableCard.add(sp, BorderLayout.CENTER);

        // ---- Form Card ----
        JPanel formCard = UIUtils.cardPanel();
        formCard.setLayout(new BorderLayout());
        formCard.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel formTitle = new JLabel("Assignment");
        formTitle.setForeground(Theme.TEXT);
        formTitle.setFont(UIUtils.font(14, Font.BOLD));
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(12, 0, 12, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 16, 0);

        txtSelectedModuleId = UIUtils.modernTextField();
        txtSelectedModuleId.setEditable(false);

        lecturerModel = new DefaultComboBoxModel<>();
        cboLecturerId = new JComboBox<>(lecturerModel);
        cboLecturerId.setEditable(true);
        styleComboToMatchUI(cboLecturerId);

        gbc.gridy = 0;
        form.add(labeled("Selected Module ID (Read Only)", txtSelectedModuleId), gbc);

        gbc.gridy = 1;
        form.add(labeled("Lecturer ID (Select or Type)", cboLecturerId), gbc);

        formCard.add(form, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(2, 2, 10, 10));
        actions.setOpaque(false);

        btnAssign = UIUtils.primaryButton("Assign Lecturer");
        btnUnassign = UIUtils.dangerButton("Unassign Lecturer");
        JButton btnClear = UIUtils.ghostButton("Clear");
        JButton btnBack = UIUtils.ghostButton("Back");

        btnAssign.addActionListener(e -> onAssign());
        btnUnassign.addActionListener(e -> onUnassign());
        btnClear.addActionListener(e -> clearForm());
        btnBack.addActionListener(e -> onBack()); // ✅ back clicks dashboard button

        actions.add(btnAssign);
        actions.add(btnUnassign);
        actions.add(btnClear);
        actions.add(btnBack);

        formCard.add(actions, BorderLayout.SOUTH);

        center.add(tableCard);
        center.add(formCard);
        root.add(center, BorderLayout.CENTER);

        // ===== Table selection -> fill form =====
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int r = table.getSelectedRow();
            if (r < 0) return;

            String moduleId = valueAt(r, 0);
            txtSelectedModuleId.setText(moduleId);

            String currentLecturer = valueAt(r, 4).replace(" (INVALID)", "").trim();
            setComboValue(currentLecturer);
        });

        refreshAll();

        if (leaderId.isEmpty()) {
            disableActions("Leader ID is missing.");
        } else {
            setupComboAutoFilter();
        }
    }

    // ✅ Back: directly click Dashboard button in LeaderDashboardFrame (same window)
    private void onBack() {
        Window w = SwingUtilities.getWindowAncestor(getContentPane());

        if (w != null) {
            JButton dashboardBtn = findButtonByText(w, "Dashboard");
            if (dashboardBtn != null) {
                dashboardBtn.doClick();
                return;
            }
        }

        // Fallback: if not embedded / button not found, just close this window
        dispose();
    }

    private JButton findButtonByText(Component root, String containsText) {
        if (root == null) return null;

        if (root instanceof JButton) {
            JButton b = (JButton) root;
            String t = b.getText();
            if (t != null && t.toLowerCase().contains(containsText.toLowerCase())) {
                return b;
            }
        }

        if (root instanceof Container) {
            for (Component c : ((Container) root).getComponents()) {
                JButton found = findButtonByText(c, containsText);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void refreshAll() {
        loadLecturerDropdown();
        loadTable();
        clearForm();
    }

    // =========================
    // Lecturer dropdown logic
    // =========================
    private void loadLecturerDropdown() {
        lecturerModel.removeAllElements();
        if (leaderId.isEmpty()) return;

        Set<String> lecturers = readLecturersForLeaderWithFallback(leaderId);
        for (String id : lecturers) {
            lecturerModel.addElement(id);
        }
    }

    private void setupComboAutoFilter() {
        Component editorComp = cboLecturerId.getEditor().getEditorComponent();
        if (!(editorComp instanceof JTextField)) return;

        JTextField editor = (JTextField) editorComp;

        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String typed = editor.getText().trim().toUpperCase();
                filterLecturerDropdown(typed);
            }
        });
    }

    private void filterLecturerDropdown(String typedUpper) {
        Set<String> all = readLecturersForLeaderWithFallback(leaderId);

        lecturerModel.removeAllElements();
        for (String id : all) {
            if (typedUpper.isEmpty() || id.startsWith(typedUpper)) {
                lecturerModel.addElement(id);
            }
        }

        setComboValue(typedUpper);

        if (lecturerModel.getSize() > 0) {
            cboLecturerId.setPopupVisible(true);
        }
    }

    private Set<String> readLecturersForLeaderWithFallback(String leaderId) {
        Set<String> mapped = readLecturersFromLeaderLecturerFile(leaderId);

        if (mapped.isEmpty()) {
            return readAllLecturersFromUsersFile();
        }

        Set<String> allLecturers = readAllLecturersFromUsersFile();
        Set<String> filtered = new LinkedHashSet<>();
        for (String id : mapped) {
            if (allLecturers.contains(id)) filtered.add(id);
        }
        return filtered;
    }

    private Set<String> readLecturersFromLeaderLecturerFile(String leaderId) {
        Set<String> set = new LinkedHashSet<>();
        List<String> lines = FileManager.readAll(LEADER_LECTURER_FILE);

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;

            String[] p = line.trim().split("\\|", -1);
            if (p.length < 2) continue;

            String leader = p[0].trim();
            String lec = p[1].trim();

            if (leader.equalsIgnoreCase(leaderId) && !lec.isEmpty()) {
                set.add(lec.toUpperCase());
            }
        }
        return set;
    }

    private Set<String> readAllLecturersFromUsersFile() {
        Set<String> set = new LinkedHashSet<>();
        List<String> lines = FileManager.readAll(USERS_FILE);

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;

            String[] p = line.trim().split("\\|", -1);
            if (p.length < 2) continue;

            String id = p[0].trim();
            String role = p[p.length - 1].trim().toUpperCase();

            if (!id.isEmpty() && role.equals("LECTURER")) {
                set.add(id.toUpperCase());
            }
        }
        return set;
    }

    private void setComboValue(String value) {
        cboLecturerId.getEditor().setItem(value == null ? "" : value);
    }

    private String getComboValue() {
        Object item = cboLecturerId.getEditor().getItem();
        return item == null ? "" : item.toString().trim();
    }

    // =========================
    // Table loading + actions
    // =========================
    private void loadTable() {
        model.setRowCount(0);
        if (leaderId.isEmpty()) return;

        Set<String> allowedLecturers = readLecturersForLeaderWithFallback(leaderId);
        List<Module> modules = ModuleService.getByLeader(leaderId);

        for (Module m : modules) {
            String lecturer = safe(m.getLecturerId());
            if (!lecturer.isEmpty() && !allowedLecturers.contains(lecturer.toUpperCase())) {
                lecturer = lecturer + " (INVALID)";
            }

            model.addRow(new Object[]{
                    safe(m.getModuleId()),
                    safe(m.getModuleName()),
                    safe(m.getModuleCode()),
                    String.valueOf(m.getCreditHours()),
                    lecturer
            });
        }
    }

    private void onAssign() {
        if (leaderId.isEmpty()) return;

        String moduleId = txtSelectedModuleId.getText().trim();
        String lecturerId = getComboValue().trim();

        if (moduleId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a module first.", "Missing Module", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (lecturerId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select/type a Lecturer ID.", "Missing Lecturer", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Set<String> allowed = readLecturersForLeaderWithFallback(leaderId);
        if (!allowed.contains(lecturerId.toUpperCase())) {
            JOptionPane.showMessageDialog(this,
                    "Invalid Lecturer ID.\nAvailable: " + String.join(", ", allowed),
                    "Invalid Lecturer", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            ModuleService.assignLecturerToModule(leaderId, moduleId, lecturerId);
            JOptionPane.showMessageDialog(this, "Assigned " + lecturerId + " to " + moduleId);
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Assign failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUnassign() {
        if (leaderId.isEmpty()) return;

        String moduleId = txtSelectedModuleId.getText().trim();
        if (moduleId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a module first.", "Missing Module", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ModuleService.unassignLecturerFromModule(leaderId, moduleId);
            JOptionPane.showMessageDialog(this, "Unassigned lecturer from " + moduleId);
            refreshAll();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Unassign failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtSelectedModuleId.setText("");
        setComboValue("");
        table.clearSelection();
    }

    private void disableActions(String message) {
        btnAssign.setEnabled(false);
        btnUnassign.setEnabled(false);
        JOptionPane.showMessageDialog(this, message, "Missing Leader ID", JOptionPane.WARNING_MESSAGE);
    }

    private String valueAt(int row, int col) {
        Object v = model.getValueAt(row, col);
        return v == null ? "" : String.valueOf(v);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setForeground(Theme.MUTED);
        l.setFont(UIUtils.font(12, Font.PLAIN));

        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }


    private void styleComboToMatchUI(JComboBox<String> combo) {
        combo.setBackground(Theme.BG);
        combo.setForeground(Theme.TEXT);
        combo.setBorder(BorderFactory.createLineBorder(new Color(70, 80, 90)));

        Component editorComp = combo.getEditor().getEditorComponent();
        if (editorComp instanceof JTextField) {
            JTextField editor = (JTextField) editorComp;
            editor.setBackground(Theme.BG);
            editor.setForeground(Theme.TEXT);
            editor.setCaretColor(Theme.TEXT);
            editor.setBorder(new EmptyBorder(10, 10, 10, 10));
        }
    }
}
