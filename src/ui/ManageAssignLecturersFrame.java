package ui;

import model.LeaderLecturerAssignment;
import model.User;
import service.LeaderLecturerService;
import service.UserService;
import service.LeaderLecturerService.AddResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ManageAssignLecturersFrame extends JPanel {

    private final LeaderLecturerService service;
    private final UserService userService;

    private JTable table;
    private DefaultTableModel tableModel;

    private JComboBox<UserItem> leaderCombo;
    private JComboBox<UserItem> lecturerCombo;

    private JButton addBtn;
    private JButton deleteBtn;
    private JButton refreshBtn;

    public ManageAssignLecturersFrame() {
        this(new LeaderLecturerService(), new UserService());
    }

    public ManageAssignLecturersFrame(LeaderLecturerService service, UserService userService) {
        this.service = service;
        this.userService = userService;

        initUI();
        loadCombos();
        loadTable();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.setBackground(Theme.BG);
        add(root, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createTitledBorder("Assign Lecturer to Leader"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        leaderCombo = new JComboBox<>();
        lecturerCombo = new JComboBox<>();

        applyComboStyle(leaderCombo);
        applyComboStyle(lecturerCombo);

        leaderCombo.addActionListener(e -> refreshLecturerComboForSelectedLeader());

        addBtn = UIUtils.primaryButton("Assign");
        deleteBtn = UIUtils.primaryButton("Delete Selected");
        refreshBtn = UIUtils.primaryButton("Refresh");

        c.gridx = 0; c.gridy = 0;
        form.add(UIUtils.muted("Leader:"), c);

        c.gridx = 1; c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(leaderCombo, c);

        c.gridx = 0; c.gridy = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        form.add(UIUtils.muted("Lecturer:"), c);

        c.gridx = 1; c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(lecturerCombo, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(addBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);

        c.gridx = 1; c.gridy = 2;
        form.add(buttons, c);

        root.add(form, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Leader ID", "Leader Name", "Lecturer ID", "Lecturer Name"}, 0) {
            @Override public boolean isCellEditable(int r, int c2) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);

        applyTableStyle();

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(new Color(18, 22, 30));
        sp.setBorder(BorderFactory.createTitledBorder("Current Assignments"));
        root.add(sp, BorderLayout.CENTER);

        addBtn.addActionListener(e -> onAdd());
        deleteBtn.addActionListener(e -> onDeleteSelected());
        refreshBtn.addActionListener(e -> { loadCombos(); loadTable(); });
        
    }

    private void applyTableStyle() {
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        table.setBackground(new Color(18, 22, 30));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 70, 90));

        table.setSelectionBackground(new Color(70, 130, 180));
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                lbl.setOpaque(true);
                lbl.setBackground(new Color(30, 35, 50));
                lbl.setForeground(Color.WHITE);
                lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                return lbl;
            }
        });

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                lbl.setOpaque(true);
                if (isSelected) {
                    lbl.setBackground(new Color(70, 130, 180));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(row % 2 == 0
                            ? new Color(18, 22, 30)
                            : new Color(24, 29, 40));
                    lbl.setForeground((column == 0 || column == 2)
                            ? new Color(200, 220, 255)
                            : Color.WHITE);
                }
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return lbl;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    private void applyComboStyle(JComboBox<UserItem> combo) {
        combo.setBackground(new Color(18, 22, 30));
        combo.setForeground(Color.WHITE);
        combo.setFocusable(false);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value,
                    int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    lbl.setBackground(new Color(70, 130, 180));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(new Color(18, 22, 30));
                    lbl.setForeground(Color.WHITE);
                }
                lbl.setBorder(new EmptyBorder(6, 8, 6, 8));
                return lbl;
            }
        });
    }

    private void loadCombos() {
        leaderCombo.removeAllItems();
        List<User> leaders = userService.getUsersByRole("LEADER");
        for (User u : leaders)
            leaderCombo.addItem(new UserItem(u.getUserId(), u.getName()));

        refreshLecturerComboForSelectedLeader();
    }

    private void refreshLecturerComboForSelectedLeader() {
        lecturerCombo.removeAllItems();

        UserItem leader = (UserItem) leaderCombo.getSelectedItem();
        if (leader == null) return;

        List<User> lecturers = userService.getUsersByRole("LECTURER");
        List<LeaderLecturerAssignment> assignments = service.getAll();

        Set<String> assignedLecturers = new HashSet<>();
        Set<String> assignedToThisLeader = new HashSet<>();
        int countForLeader = 0;

        for (LeaderLecturerAssignment a : assignments) {
            String lId = safe(a.getLeaderId());
            String lecId = safe(a.getLecturerId());

            if (!lecId.isEmpty()) assignedLecturers.add(lecId);

            if (leader.id.equalsIgnoreCase(lId)) {
                assignedToThisLeader.add(lecId);
                countForLeader++;
            }
        }

        for (User u : lecturers) {
            String lecId = safe(u.getUserId());
            boolean isFree = !assignedLecturers.contains(lecId);
            boolean isMine = assignedToThisLeader.contains(lecId);

            if (isFree || isMine)
                lecturerCombo.addItem(new UserItem(u.getUserId(), u.getName()));
        }

        int remaining = 3 - countForLeader;
        if (remaining < 0) remaining = 0;
        lecturerCombo.setToolTipText("Remaining slots: " + remaining);
    }

    private void loadTable() {
        tableModel.setRowCount(0);

        List<LeaderLecturerAssignment> list = service.getAll();
        for (LeaderLecturerAssignment a : list) {
            String leaderId = safe(a.getLeaderId());
            String lecturerId = safe(a.getLecturerId());

            User leader = userService.getById(leaderId);
            User lecturer = userService.getById(lecturerId);

            String leaderName = leader == null ? "(unknown)" : safe(leader.getName());
            String lecturerName = lecturer == null ? "(unknown)" : safe(lecturer.getName());

            tableModel.addRow(new Object[]{
                    leaderId, leaderName, lecturerId, lecturerName
            });
        }
    }

    private void onAdd() {
        UserItem leader = (UserItem) leaderCombo.getSelectedItem();
        UserItem lecturer = (UserItem) lecturerCombo.getSelectedItem();

        if (leader == null || lecturer == null) {
            JOptionPane.showMessageDialog(this, "Please select both a leader and a lecturer.");
            return;
        }

        AddResult result = service.add(leader.id, lecturer.id);

        switch (result) {
            case SUCCESS:
                JOptionPane.showMessageDialog(this, "Assignment added successfully.");
                loadTable();
                refreshLecturerComboForSelectedLeader();
                break;
            case DUPLICATE:
                JOptionPane.showMessageDialog(this, "This assignment already exists.");
                break;
            case LECTURER_ALREADY_ASSIGNED:
                JOptionPane.showMessageDialog(this, "This lecturer is already assigned to another leader.");
                break;
            case LEADER_MAX_LECTURERS:
                JOptionPane.showMessageDialog(this, "This leader already has 3 lecturers (maximum).");
                break;
            default:
                JOptionPane.showMessageDialog(this, "Operation failed.");
                break;
        }
    }

    private void onDeleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first.");
            return;
        }

        String leaderId = safe(tableModel.getValueAt(row, 0));
        String lecturerId = safe(tableModel.getValueAt(row, 2));

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete assignment?\nLeader: " + leaderId + "\nLecturer: " + lecturerId,
                "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = service.deletePair(leaderId, lecturerId);

        if (ok) {
            JOptionPane.showMessageDialog(this, "Assignment deleted.");
            loadTable();
            refreshLecturerComboForSelectedLeader();
        } else {
            JOptionPane.showMessageDialog(this, "Delete failed.");
        }
    }

    private String safe(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    static class UserItem {
        final String id;
        final String name;

        UserItem(String id, String name) {
            this.id = id == null ? "" : id.trim();
            this.name = name == null ? "" : name.trim();
        }

        @Override
        public String toString() {
            if (name.isEmpty()) return id;
            if (id.isEmpty()) return name;
            return name + " (" + id + ")";
        }
    }
}
