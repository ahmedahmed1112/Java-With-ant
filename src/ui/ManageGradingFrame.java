package ui;

import model.GradingRule;
import service.GradingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ManageGradingFrame extends JPanel {

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;

    private JComboBox<String> cmbGrade;
    private JTextField txtMin, txtMax;
    private JTextField txtSearch;

    private JLabel lblTotal;

    private JTable table;
    private DefaultTableModel tableModel;

    private List<GradingRule> allRules = new ArrayList<>();

    public ManageGradingFrame() {
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
        titles.add(UIUtils.title("Manage Grading Rules"));
        titles.add(UIUtils.muted("CRUD grading rules stored in data/grading.txt (Grade|Min|Max)"));

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTop.setOpaque(false);

        txtSearch = UIUtils.modernTextField();
        txtSearch.setPreferredSize(new Dimension(260, 38));
        txtSearch.setToolTipText("Search by grade / min / max");

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

        // Stats
        JPanel stats = new JPanel(new GridLayout(1, 1, 14, 14));
        stats.setOpaque(false);

        lblTotal = new JLabel("0");
        stats.add(statCard("Total Rules", "How many grade ranges exist", lblTotal));
        content.add(stats, BorderLayout.NORTH);

        // Split
        JPanel main = new JPanel(new GridLayout(1, 2, 14, 14));
        main.setOpaque(false);
        content.add(main, BorderLayout.CENTER);

        main.add(buildFormCard());
        main.add(buildTableCard());

        // Events
        btnSearch.addActionListener(e -> applySearch());
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            refreshTable(allRules);
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

        JLabel t = new JLabel("Rule Details");
        t.setForeground(Theme.TEXT);
        t.setFont(UIUtils.font(16, Font.BOLD));
        card.add(t, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 0, 8, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;

        cmbGrade = new JComboBox<>();
        cmbGrade.setBackground(Theme.CARD);
        cmbGrade.setForeground(Theme.TEXT);
        cmbGrade.setFont(UIUtils.font(13, Font.BOLD));
        cmbGrade.setPreferredSize(new Dimension(260, 38));

        for (String g : GradingService.getAllowedGradesOrdered()) {
            cmbGrade.addItem(g);
        }

        txtMin = UIUtils.modernTextField();
        txtMax = UIUtils.modernTextField();

        addField(form, gc, 0, "Grade (select from list)", cmbGrade);
        addField(form, gc, 2, "Min Score (0-100)", txtMin);
        addField(form, gc, 4, "Max Score (0-100)", txtMax);

        JLabel hint = UIUtils.muted("Validation: Min ≤ Max, 0–100, grade must be A+..F-, and ranges must not overlap.");
        hint.setBorder(new EmptyBorder(6, 0, 0, 0));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(hint, BorderLayout.SOUTH);

        card.add(center, BorderLayout.CENTER);

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

        btnAdd.addActionListener(e -> addRule());
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

        JLabel t = new JLabel("Rules List");
        t.setForeground(Theme.TEXT);
        t.setFont(UIUtils.font(16, Font.BOLD));
        card.add(t, BorderLayout.NORTH);

        String[] cols = {"Grade", "Min", "Max"};

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
        allRules = GradingService.getAll();
        refreshTable(allRules);
        lblTotal.setText(String.valueOf(allRules.size()));
        clearForm();
    }

    private void refreshTable(List<GradingRule> list) {
        tableModel.setRowCount(0);
        for (GradingRule r : list) {
            tableModel.addRow(new Object[]{r.getGrade(), r.getMin(), r.getMax()});
        }
    }

    private void applySearch() {
        String q = txtSearch.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            refreshTable(allRules);
            return;
        }

        List<GradingRule> filtered = new ArrayList<>();
        for (GradingRule r : allRules) {
            if (r.getGrade().toLowerCase().contains(q)
                    || String.valueOf(r.getMin()).contains(q)
                    || String.valueOf(r.getMax()).contains(q)) {
                filtered.add(r);
            }
        }
        refreshTable(filtered);
    }

    private void addRule() {
        ParsedRule parsed = parseInputs();
        if (parsed == null) return;

        if (GradingService.existsGrade(parsed.grade)) {
            JOptionPane.showMessageDialog(this, "Grade already exists. Use Update.");
            return;
        }

        if (!validateNoOverlap(parsed.grade, parsed.min, parsed.max, null)) return;

        GradingService.add(new GradingRule(parsed.grade, parsed.min, parsed.max));
        reload();
        JOptionPane.showMessageDialog(this, "Rule added successfully.");
    }

    private void updateSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a rule from the table first.");
            return;
        }

        String oldGradeKey = String.valueOf(tableModel.getValueAt(row, 0));

        ParsedRule parsed = parseInputs();
        if (parsed == null) return;

        if (!parsed.grade.equalsIgnoreCase(oldGradeKey)) {
            JOptionPane.showMessageDialog(this, "For Update: keep Grade same as selected row (key).");
            cmbGrade.setSelectedItem(oldGradeKey);
            return;
        }

        if (!validateNoOverlap(parsed.grade, parsed.min, parsed.max, oldGradeKey)) return;

        GradingService.update(oldGradeKey, new GradingRule(parsed.grade, parsed.min, parsed.max));
        reload();
        JOptionPane.showMessageDialog(this, "Rule updated successfully.");
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a rule from the table first.");
            return;
        }

        String grade = String.valueOf(tableModel.getValueAt(row, 0));

        int ok = JOptionPane.showConfirmDialog(this, "Delete rule " + grade + " ?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        GradingService.delete(grade);
        reload();
        JOptionPane.showMessageDialog(this, "Rule deleted.");
    }

    private void fillFormFromSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        String grade = String.valueOf(tableModel.getValueAt(row, 0));
        cmbGrade.setSelectedItem(grade);

        txtMin.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        txtMax.setText(String.valueOf(tableModel.getValueAt(row, 2)));
    }

    private void clearForm() {
        if (cmbGrade.getItemCount() > 0) cmbGrade.setSelectedIndex(0);
        txtMin.setText("");
        txtMax.setText("");
    }

    private static class ParsedRule {
        String grade;
        int min;
        int max;
        ParsedRule(String grade, int min, int max) {
            this.grade = grade;
            this.min = min;
            this.max = max;
        }
    }

    private ParsedRule parseInputs() {
        String grade = String.valueOf(cmbGrade.getSelectedItem()).trim().toUpperCase();
        String minS = txtMin.getText().trim();
        String maxS = txtMax.getText().trim();

        if (!GradingService.isAllowedGrade(grade)) {
            JOptionPane.showMessageDialog(this, "Invalid grade selected.");
            return null;
        }

        if (minS.isEmpty() || maxS.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill Min and Max.");
            return null;
        }

        int min, max;
        try {
            min = Integer.parseInt(minS);
            max = Integer.parseInt(maxS);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Min and Max must be numbers.");
            return null;
        }

        if (min < MIN_SCORE || max > MAX_SCORE) {
            JOptionPane.showMessageDialog(this, "Scores must be between 0 and 100.");
            return null;
        }

        if (min > max) {
            JOptionPane.showMessageDialog(this, "Min must be ≤ Max.");
            return null;
        }

        return new ParsedRule(grade, min, max);
    }

    private boolean validateNoOverlap(String grade, int min, int max, String excludeKey) {
        for (GradingRule r : allRules) {
            if (excludeKey != null && r.getGrade().equalsIgnoreCase(excludeKey)) continue;

            boolean overlap = !(max < r.getMin() || min > r.getMax());
            if (overlap) {
                JOptionPane.showMessageDialog(this,
                        "Range overlaps with grade " + r.getGrade() + " (" + r.getMin() + "-" + r.getMax() + ").");
                return false;
            }
        }
        return true;
    }
}
