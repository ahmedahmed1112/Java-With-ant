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
import java.util.List;

public class LecturerKeyInMarksPanel extends JPanel {
    private final Lecturer lecturer;
    private final LecturerController controller;
    private final LecturerDashboardFrame parent;

    private final JComboBox<Assessment> assessmentBox = new JComboBox<>();
    private final DefaultTableModel tableModel;
    private final JTable table;

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

        tableModel = new DefaultTableModel(new Object[]{"Student ID", "Name", "Marks", "Grade"}, 0);
        table = new JTable(tableModel);
        UIUtils.applyTableStyle(table);

        assessmentBox.setBackground(Theme.CARD);
        assessmentBox.setForeground(Theme.TEXT);

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
        for (Assessment a : controller.getAssessmentsByModule(lecturer.getAssignedModuleId())) {
            assessmentBox.addItem(a);
        }
    }

    private void loadStudents() {
        tableModel.setRowCount(0);
        List<Student> students = controller.getStudentsByModule(lecturer.getAssignedModuleId());
        for (Student s : students) {
            tableModel.addRow(new Object[]{s.getStudentId(), s.getName(), "", ""});
        }
    }

    private void saveGrades() {
        Assessment assessment = (Assessment) assessmentBox.getSelectedItem();
        if (assessment == null) {
            JOptionPane.showMessageDialog(this, "Select an assessment", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Grade> grades = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String studentId = tableModel.getValueAt(i, 0).toString();
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
            Grade grade = new Grade("GRD" + System.currentTimeMillis() + i, assessment.getAssessmentId(), studentId, marks, gradeLabel, lecturer.getUsername(), controller.today());
            grades.add(grade);
        }
        if (grades.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No marks entered", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        controller.saveGrades(grades);
        JOptionPane.showMessageDialog(this, "Grades saved", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
