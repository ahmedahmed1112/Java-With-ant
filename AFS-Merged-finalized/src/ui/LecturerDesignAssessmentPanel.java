package ui;

import controller.LecturerController;
import controller.ValidationUtil;
import model.Assessment;
import model.Lecturer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LecturerDesignAssessmentPanel extends JPanel {
    private final Lecturer lecturer;
    private final LecturerController controller;
    private final LecturerDashboardFrame parent;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JTextField nameField = UIUtils.modernTextField();
    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{
            "Assignment", "Quiz", "Test", "Project", "Presentation"
    });
    private final JTextField totalMarksField = UIUtils.modernTextField();
    private final JTextField weightageField = UIUtils.modernTextField();

    public LecturerDesignAssessmentPanel(Lecturer lecturer, LecturerController controller, LecturerDashboardFrame parent) {
        this.lecturer = lecturer;
        this.controller = controller;
        this.parent = parent;

        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIUtils.title("Design Assessment"), BorderLayout.NORTH);
        header.add(UIUtils.muted("Create, edit, delete assessments for your module"), BorderLayout.SOUTH);
        JLabel moduleLabel = new JLabel("Module: " + moduleDisplay());
        moduleLabel.setForeground(Theme.TEXT);
        moduleLabel.setFont(UIUtils.font(12, Font.BOLD));
        header.add(moduleLabel, BorderLayout.EAST);

        // Form
        JPanel formCard = UIUtils.cardPanel();
        formCard.setLayout(new GridLayout(4, 2, 8, 8));
        formCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        typeBox.setBackground(Theme.CARD);
        typeBox.setForeground(Theme.TEXT);

        formCard.add(UIUtils.muted("Assessment Name:"));
        formCard.add(nameField);
        formCard.add(UIUtils.muted("Assessment Type:"));
        formCard.add(typeBox);
        formCard.add(UIUtils.muted("Total Marks:"));
        formCard.add(totalMarksField);
        formCard.add(UIUtils.muted("Weightage (%):"));
        formCard.add(weightageField);

        // Table
        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Type", "Marks", "Weightage"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.applyTableStyle(table);
        refreshTable();

        JScrollPane sp = new JScrollPane(table);
        UIUtils.styleScrollPane(sp);

        // Buttons
        JButton addButton = UIUtils.primaryButton("Add");
        JButton editButton = UIUtils.primaryButton("Edit");
        JButton deleteButton = UIUtils.dangerButton("Delete");
        JButton backButton = UIUtils.ghostButton("Back");

        addButton.addActionListener(e -> addAssessment());
        editButton.addActionListener(e -> editAssessment());
        deleteButton.addActionListener(e -> deleteAssessment());
        backButton.addActionListener(e -> parent.showDashboard());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 8, 8));
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);

        JPanel topSection = new JPanel(new BorderLayout(10, 10));
        topSection.setOpaque(false);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(formCard, BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        if (!hasAssignedModule()) return;

        List<Assessment> assessments = controller.getAssessmentsByModuleForLecturer(lecturerId(), lecturer.getAssignedModuleId());
        for (Assessment a : assessments) {
            tableModel.addRow(new Object[]{a.getAssessmentId(), a.getAssessmentName(), a.getAssessmentType(), a.getTotalMarks(), a.getWeightage()});
        }
    }

    public void refreshTableAndInputs() {
        refreshTable();
        nameField.setText("");
        totalMarksField.setText("");
        weightageField.setText("");
    }

    private void addAssessment() {
        if (!hasAssignedModule()) {
            JOptionPane.showMessageDialog(this, "No module assigned. Ask your academic leader to assign your module first.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        String type = (String) typeBox.getSelectedItem();
        if (!ValidationUtil.isNotEmpty(name)) {
            JOptionPane.showMessageDialog(this, "Assessment name required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double totalMarks, weightage;
        try {
            totalMarks = Double.parseDouble(totalMarksField.getText().trim());
            weightage = Double.parseDouble(weightageField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Marks and weightage must be numbers", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (totalMarks <= 0) {
            JOptionPane.showMessageDialog(this, "Total marks must be greater than 0", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (weightage <= 0 || weightage > 100) {
            JOptionPane.showMessageDialog(this, "Weightage must be between 0 and 100", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double currentWeightage = totalWeightageExcluding(null);
        if (currentWeightage + weightage > 100) {
            JOptionPane.showMessageDialog(this, "Total weightage cannot exceed 100", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String assessmentId = "ASS" + System.currentTimeMillis();
        Assessment assessment = new Assessment(assessmentId, lecturer.getAssignedModuleId(), name, type, totalMarks, weightage, lecturer.getUsername());
        controller.addAssessment(assessment);
        refreshTable();
        nameField.setText(""); totalMarksField.setText(""); weightageField.setText("");
    }

    private void editAssessment() {
        if (!hasAssignedModule()) {
            JOptionPane.showMessageDialog(this, "No module assigned. Ask your academic leader to assign your module first.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row to edit", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = tableModel.getValueAt(row, 0).toString();
        if (!ValidationUtil.isNotEmpty(nameField.getText().trim())) nameField.setText(tableModel.getValueAt(row, 1).toString());
        if (!ValidationUtil.isNotEmpty(totalMarksField.getText().trim())) totalMarksField.setText(tableModel.getValueAt(row, 3).toString());
        if (!ValidationUtil.isNotEmpty(weightageField.getText().trim())) weightageField.setText(tableModel.getValueAt(row, 4).toString());

        String name = nameField.getText().trim();
        if (!ValidationUtil.isNotEmpty(name)) {
            JOptionPane.showMessageDialog(this, "Assessment name required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String type = (String) typeBox.getSelectedItem();
        double totalMarks, weightage;
        try {
            totalMarks = Double.parseDouble(totalMarksField.getText().trim());
            weightage = Double.parseDouble(weightageField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Marks and weightage must be numbers", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (totalMarks <= 0) {
            JOptionPane.showMessageDialog(this, "Total marks must be greater than 0", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (weightage <= 0 || weightage > 100) {
            JOptionPane.showMessageDialog(this, "Weightage must be between 0 and 100", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double currentWeightage = totalWeightageExcluding(id);
        if (currentWeightage + weightage > 100) {
            JOptionPane.showMessageDialog(this, "Total weightage cannot exceed 100", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Assessment updated = new Assessment(id, lecturer.getAssignedModuleId(), name, type, totalMarks, weightage, lecturer.getUsername());
        controller.updateAssessment(updated);
        refreshTable();
        nameField.setText(""); totalMarksField.setText(""); weightageField.setText("");
    }

    private void deleteAssessment() {
        if (!hasAssignedModule()) {
            JOptionPane.showMessageDialog(this, "No module assigned. Ask your academic leader to assign your module first.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row to delete", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = tableModel.getValueAt(row, 0).toString();
        controller.deleteAssessment(id);
        refreshTable();
    }

    private double totalWeightageExcluding(String assessmentId) {
        double total = 0;
        for (Assessment a : controller.getAssessmentsByModuleForLecturer(lecturerId(), lecturer.getAssignedModuleId())) {
            if (assessmentId != null && assessmentId.equals(a.getAssessmentId())) continue;
            total += a.getWeightage();
        }
        return total;
    }

    private boolean hasAssignedModule() {
        String moduleId = lecturer.getAssignedModuleId();
        return moduleId != null
                && !moduleId.trim().isEmpty()
                && controller.isLecturerAssignedToModule(lecturerId(), moduleId);
    }

    private String moduleDisplay() {
        return hasAssignedModule() ? lecturer.getAssignedModuleId() : "Not Assigned";
    }

    private String lecturerId() {
        String id = lecturer.getUserId();
        if (id == null || id.trim().isEmpty()) {
            id = lecturer.getUsername();
        }
        return id == null ? "" : id.trim();
    }
}
