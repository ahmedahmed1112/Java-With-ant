package ui;

import controller.LecturerController;
import controller.ValidationUtil;
import model.Assessment;
import model.Grade;
import model.Lecturer;
import model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LecturerKeyInMarksPanel extends JPanel {
    private final Lecturer lecturer;
    private final LecturerController controller;
    private final LecturerDashboardFrame parent;

    private final JComboBox<Assessment> assessmentBox = new JComboBox<>();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Student> loadedStudents = new ArrayList<>();

    public LecturerKeyInMarksPanel(Lecturer lecturer, LecturerController controller, LecturerDashboardFrame parent) {
        this.lecturer = lecturer;
        this.controller = controller;
        this.parent = parent;

        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIUtils.title("Key-in Marks"), BorderLayout.NORTH);
        header.add(UIUtils.muted("Enter marks for student assessments"), BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(new Object[]{"Student ID", "Name", "Marks", "Grade"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only marks can be edited by lecturer.
                return column == 2;
            }
        };
        table = new JTable(tableModel);
        UIUtils.applyTableStyle(table);
        table.getTableHeader().setReorderingAllowed(false);
        // Hard-lock all table columns first, then explicitly allow only marks column editor.
        table.setDefaultEditor(Object.class, null);
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()));

        assessmentBox.setBackground(Theme.CARD);
        assessmentBox.setForeground(Theme.TEXT);
        assessmentBox.addActionListener(e -> {
            if (tableModel.getRowCount() > 0) loadStudents();
        });

        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setOpaque(false);
        topPanel.add(header, BorderLayout.NORTH);

        JPanel selectPanel = UIUtils.cardPanel();
        selectPanel.setLayout(new GridLayout(1, 2, 8, 8));
        selectPanel.add(UIUtils.muted("Select Assessment:"));
        selectPanel.add(assessmentBox);
        topPanel.add(selectPanel, BorderLayout.CENTER);

        JButton loadButton = UIUtils.primaryButton("Load Students");
        JButton saveButton = UIUtils.primaryButton("Save All");
        JButton backButton = UIUtils.ghostButton("Back");

        loadButton.addActionListener(e -> loadStudents());
        saveButton.addActionListener(e -> saveGrades());
        backButton.addActionListener(e -> parent.showDashboard());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 8, 8));
        buttonPanel.setOpaque(false);
        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);

        JScrollPane sp = new JScrollPane(table);
        UIUtils.styleScrollPane(sp);

        add(topPanel, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        populateAssessments();
    }

    private void populateAssessments() {
        assessmentBox.removeAllItems();
        if (!hasAssignedModule()) return;
        for (Assessment a : controller.getAssessmentsByModuleForLecturer(lecturerId(), lecturer.getAssignedModuleId())) {
            assessmentBox.addItem(a);
        }
    }

    public void refreshAssessments() {
        Assessment selected = (Assessment) assessmentBox.getSelectedItem();
        String selectedId = selected == null ? "" : selected.getAssessmentId();
        populateAssessments();

        if (!selectedId.isEmpty()) {
            for (int i = 0; i < assessmentBox.getItemCount(); i++) {
                Assessment a = assessmentBox.getItemAt(i);
                if (a != null && selectedId.equalsIgnoreCase(a.getAssessmentId())) {
                    assessmentBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        tableModel.setRowCount(0);
        loadedStudents = new ArrayList<>();
    }

    private void loadStudents() {
        if (!hasAssignedModule()) {
            JOptionPane.showMessageDialog(this, "No valid module assignment found for this lecturer in modules.txt.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Assessment assessment = (Assessment) assessmentBox.getSelectedItem();
        if (assessment != null && !controller.canLecturerAccessAssessment(lecturerId(), lecturer.getAssignedModuleId(), assessment.getAssessmentId())) {
            JOptionPane.showMessageDialog(this, "You can only access assessments under your assigned module.", "Authorization Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);
        loadedStudents = controller.getStudentsByModuleForLecturer(lecturerId(), lecturer.getAssignedModuleId());
        Map<String, Grade> existingByStudentId = new HashMap<>();
        if (assessment != null) {
            for (Grade g : controller.getGradesByAssessment(assessment.getAssessmentId())) {
                existingByStudentId.put(g.getStudentId(), g);
            }
        }

        for (Student s : loadedStudents) {
            Grade existing = existingByStudentId.get(s.getStudentId());
            String marks = (existing == null) ? "" : String.valueOf(existing.getMarks());
            String grade = (existing == null) ? "" : existing.getGrade();
            tableModel.addRow(new Object[]{s.getStudentId(), s.getName(), marks, grade});
        }
    }

    private void saveGrades() {
        if (!hasAssignedModule()) {
            JOptionPane.showMessageDialog(this, "No valid module assignment found for this lecturer in modules.txt.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Assessment assessment = (Assessment) assessmentBox.getSelectedItem();
        if (assessment == null) {
            JOptionPane.showMessageDialog(this, "Select an assessment", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!controller.canLecturerAccessAssessment(lecturerId(), lecturer.getAssignedModuleId(), assessment.getAssessmentId())) {
            JOptionPane.showMessageDialog(this, "You can only key in marks for assessments under your assigned module.", "Authorization Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (table.isEditing() && table.getCellEditor() != null) {
            table.getCellEditor().stopCellEditing();
        }
        List<Grade> grades = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (i >= loadedStudents.size()) continue;
            // Always use authoritative loaded student IDs (never editable table cell value).
            String studentId = loadedStudents.get(i).getStudentId();
            if (!controller.canLecturerAssessStudent(lecturerId(), lecturer.getAssignedModuleId(), studentId)) {
                JOptionPane.showMessageDialog(this, "Unauthorized student found in marks list: " + studentId, "Authorization Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String marksText = tableModel.getValueAt(i, 2).toString();
            if (!ValidationUtil.isNotEmpty(marksText)) continue;
            double marks;
            try { marks = Double.parseDouble(marksText); } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Marks must be numeric", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtil.isValidMarks(marks, assessment.getTotalMarks())) {
                JOptionPane.showMessageDialog(this, "Marks must be between 0 and " + assessment.getTotalMarks(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String gradeLabel = controller.calculateGrade(marks, assessment.getTotalMarks());
            tableModel.setValueAt(gradeLabel, i, 3);
            Grade grade = new Grade("GRD" + System.currentTimeMillis() + i, assessment.getAssessmentId(), studentId, marks, gradeLabel, lecturerId(), controller.today());
            grades.add(grade);
        }
        if (grades.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No marks entered", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        controller.saveGrades(grades);
        JOptionPane.showMessageDialog(this, "Grades saved", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean hasAssignedModule() {
        String moduleId = lecturer.getAssignedModuleId();
        return moduleId != null
                && !moduleId.trim().isEmpty()
                && controller.isLecturerAssignedToModule(lecturerId(), moduleId);
    }

    private String lecturerId() {
        String id = lecturer.getUserId();
        if (id == null || id.trim().isEmpty()) {
            id = lecturer.getUsername();
        }
        return id == null ? "" : id.trim();
    }
}
