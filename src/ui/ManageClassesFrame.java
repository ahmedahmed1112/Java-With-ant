package ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import model.ClassRecord;
import service.ClassService;

public class ManageClassesFrame extends JPanel {

    private JTextField txtClassId, txtClassName, txtModuleId;
    private JTextField txtSearch;

    private JLabel lblTotal;

    private JTable table;
    private DefaultTableModel tableModel;

    private List<ClassRecord> allClasses = new ArrayList<>();

    public ManageClassesFrame() {
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
        titles.add(UIUtils.title("Manage Classes"));
        titles.add(UIUtils.muted("CRUD classes stored in data/classes.txt"));

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTop.setOpaque(false);

        txtSearch = UIUtils.modernTextField();
        txtSearch.setPreferredSize(new Dimension(260, 38));
        txtSearch.setToolTipText("Search by classId / className / moduleId");

        JButton btnSearch = UIUtils.primaryButton("Search");
        JButton btnClearSearch = UIUtils.ghostButton("Clear");

        rightTop.add(txtSearch);
        rightTop.add(btnSearch);
        rightTop.add(btnClearSearch);

        header.add(titles, BorderLayout.WEST);
        header.add(rightTop, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // ===== Content =====
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(14, 0, 0, 0));
        root.add(content, BorderLayout.CENTER);

        JPanel stats = new JPanel(new GridLayout(1, 1, 14, 14));
        stats.setOpaque(false);

        lblTotal = new JLabel("0");
        stats.add(statCard("Total Classes", "All classes in classes.txt", lblTotal));

        content.add(stats, BorderLayout.NORTH);

        JPanel main = new JPanel(new GridLayout(1, 2, 14, 14));
        main.setOpaque(false);
        content.add(main, BorderLayout.CENTER);

        main.add(buildFormCard());
        main.add(buildTableCard());

        // Events
        btnSearch.addActionListener(e -> applySearch());
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            refreshTable(allClasses);
        });

        // load
        reload();
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

        JLabel t = new JLabel("Class Details");
        t.setForeground(Theme.TEXT);
        t.setFont(UIUtils.font(16, Font.BOLD));
        card.add(t, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        card.add(form, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 0, 8, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;

        txtClassId = UIUtils.modernTextField();
        txtClassName = UIUtils.modernTextField();
        txtModuleId = UIUtils.modernTextField();

        addField(form, gc, 0, "Class ID (select from table to update)", txtClassId);
        addField(form, gc, 2, "Class Name", txtClassName);
        addField(form, gc, 4, "Module ID", txtModuleId);

        JPanel btnRow = new JPanel(new GridLayout(2, 2, 10, 10));
        btnRow.setOpaque(false);

        JButton btnAdd = UIUtils.primaryButton("Add");
        JButton btnUpdate = UIUtils.primaryButton("Update Selected");
        JButton btnDelete = UIUtils.dangerButton("Delete Selected");
        JButton btnRefresh = UIUtils.ghostButton("Refresh");

        btnRow.add(btnAdd);
        btnRow.add(btnUpdate);
        btnRow.add(btnDelete);
        btnRow.add(btnRefresh);

        card.add(btnRow, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> addClass());
        btnUpdate.addActionListener(e -> updateSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> reload());

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

        JLabel t = new JLabel("Classes List");
        t.setForeground(Theme.TEXT);
        t.setFont(UIUtils.font(16, Font.BOLD));
        card.add(t, BorderLayout.NORTH);

        String[] cols = {"Class ID", "Class Name", "Module ID"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        UIUtils.applyTableStyle(table);

        JScrollPane sp = new JScrollPane(table);
        UIUtils.styleScrollPane(sp);
        card.add(sp, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> fillFormFromSelected());

        return card;
    }


    private void reload() {
        allClasses = ClassService.getAll();
        refreshTable(allClasses);
        lblTotal.setText(String.valueOf(allClasses.size()));
        clearForm();
    }

    private void refreshTable(List<ClassRecord> list) {
        tableModel.setRowCount(0);
        for (ClassRecord c : list) {
            tableModel.addRow(new Object[]{c.getClassId(), c.getClassName(), c.getModuleId()});
        }
    }

    private void applySearch() {
        String q = txtSearch.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            refreshTable(allClasses);
            return;
        }

        List<ClassRecord> filtered = new ArrayList<>();
        for (ClassRecord c : allClasses) {
            if (c.getClassId().toLowerCase().contains(q)
                    || c.getClassName().toLowerCase().contains(q)
                    || c.getModuleId().toLowerCase().contains(q)) {
                filtered.add(c);
            }
        }
        refreshTable(filtered);
    }

    private void addClass() {
        String id = txtClassId.getText().trim();
        String name = txtClassName.getText().trim();
        String module = txtModuleId.getText().trim();

        if (id.isEmpty() || name.isEmpty() || module.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        if (ClassService.existsClassId(id)) {
            JOptionPane.showMessageDialog(this, "Class ID already exists.");
            return;
        }

        ClassService.add(new ClassRecord(id, name, module));
        reload();
        JOptionPane.showMessageDialog(this, "Class added successfully.");
    }

    private void updateSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a class from the table first.");
            return;
        }

        String id = txtClassId.getText().trim();
        String name = txtClassName.getText().trim();
        String module = txtModuleId.getText().trim();

        if (id.isEmpty() || name.isEmpty() || module.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        ClassService.update(id, new ClassRecord(id, name, module));
        reload();
        JOptionPane.showMessageDialog(this, "Class updated successfully.");
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a class from the table first.");
            return;
        }

        String id = String.valueOf(tableModel.getValueAt(row, 0));
        int ok = JOptionPane.showConfirmDialog(this, "Delete class " + id + " ?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        ClassService.delete(id);
        reload();
        JOptionPane.showMessageDialog(this, "Class deleted.");
    }

    private void fillFormFromSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        txtClassId.setText(String.valueOf(tableModel.getValueAt(row, 0)));
        txtClassName.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        txtModuleId.setText(String.valueOf(tableModel.getValueAt(row, 2)));
    }

    private void clearForm() {
        txtClassId.setText("");
        txtClassName.setText("");
        txtModuleId.setText("");
    }
}
