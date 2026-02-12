package ui;

import controller.LecturerController;
import controller.ValidationUtil;
import model.Assessment;
import model.Feedback;
import model.Grade;
import model.Lecturer;
import model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class LecturerProvideFeedbackPanel extends JPanel {
    private final Lecturer lecturer;
    private final LecturerController controller;
    private final LecturerDashboardFrame parent;

    private final JComboBox<Assessment> assessmentBox = new JComboBox<>();
    private final JComboBox<Student> studentBox = new JComboBox<>();
    private final JTextArea feedbackArea = new JTextArea(6, 30);
    private final JLabel marksLabel = new JLabel("Student Marks: -");
    private final JLabel gradeLabel = new JLabel("Grade: -");

    public LecturerProvideFeedbackPanel(Lecturer lecturer, LecturerController controller, LecturerDashboardFrame parent) {
        this.lecturer = lecturer;
        this.controller = controller;
        this.parent = parent;

        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIUtils.title("Provide Feedback"), BorderLayout.NORTH);
        header.add(UIUtils.muted("Give feedback to individual students"), BorderLayout.SOUTH);

        assessmentBox.setBackground(Theme.CARD);
        assessmentBox.setForeground(Theme.TEXT);
        studentBox.setBackground(Theme.CARD);
        studentBox.setForeground(Theme.TEXT);
        marksLabel.setForeground(Theme.TEXT);
        marksLabel.setFont(UIUtils.font(13, Font.PLAIN));
        gradeLabel.setForeground(Theme.TEXT);
        gradeLabel.setFont(UIUtils.font(13, Font.PLAIN));

        feedbackArea.setBackground(Theme.INPUT_BG);
        feedbackArea.setForeground(Theme.TEXT);
        feedbackArea.setCaretColor(Theme.TEXT);
        feedbackArea.setFont(UIUtils.font(13, Font.PLAIN));
        feedbackArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel selectionCard = UIUtils.cardPanel();
        selectionCard.setLayout(new GridLayout(2, 2, 8, 8));
        selectionCard.add(UIUtils.muted("Select Assessment:"));
        selectionCard.add(assessmentBox);
        selectionCard.add(UIUtils.muted("Select Student:"));
        selectionCard.add(studentBox);

        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 8, 8));
        infoPanel.setOpaque(false);
        infoPanel.add(marksLabel);
        infoPanel.add(gradeLabel);

        JButton loadButton = UIUtils.primaryButton("Load Students");
        JButton saveButton = UIUtils.primaryButton("Save Feedback");
        JButton backButton = UIUtils.ghostButton("Back");

        loadButton.addActionListener(e -> loadStudents());
        saveButton.addActionListener(e -> saveFeedback());
        backButton.addActionListener(e -> parent.showDashboard());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 8, 8));
        buttonPanel.setOpaque(false);
        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);

        JPanel topSection = new JPanel(new BorderLayout(8, 8));
        topSection.setOpaque(false);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(selectionCard, BorderLayout.CENTER);
        topSection.add(infoPanel, BorderLayout.SOUTH);

        JScrollPane sp = new JScrollPane(feedbackArea);
        UIUtils.styleScrollPane(sp);

        add(topSection, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        populateAssessments();
        assessmentBox.addActionListener(e -> updateStudentInfo());
        studentBox.addActionListener(e -> updateStudentInfo());
    }

    private void populateAssessments() {
        assessmentBox.removeAllItems();
        for (Assessment a : controller.getAssessmentsByModule(lecturer.getAssignedModuleId())) {
            assessmentBox.addItem(a);
        }
    }

    private void loadStudents() {
        studentBox.removeAllItems();
        List<Student> students = controller.getStudentsByModule(lecturer.getAssignedModuleId());
        for (Student s : students) {
            studentBox.addItem(s);
        }
        updateStudentInfo();
    }

    private void saveFeedback() {
        Assessment assessment = (Assessment) assessmentBox.getSelectedItem();
        Student student = (Student) studentBox.getSelectedItem();
        String feedbackText = feedbackArea.getText().trim();
        if (assessment == null || student == null) {
            JOptionPane.showMessageDialog(this, "Select assessment and student", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!ValidationUtil.isNotEmpty(feedbackText)) {
            JOptionPane.showMessageDialog(this, "Feedback is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Feedback feedback = new Feedback("FDB" + System.currentTimeMillis(), assessment.getAssessmentId(), student.getStudentId(), lecturer.getUsername(), feedbackText, controller.today());
        controller.saveFeedback(feedback);
        JOptionPane.showMessageDialog(this, "Feedback saved", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStudentInfo() {
        Assessment assessment = (Assessment) assessmentBox.getSelectedItem();
        Student student = (Student) studentBox.getSelectedItem();
        if (assessment == null || student == null) {
            marksLabel.setText("Student Marks: -");
            gradeLabel.setText("Grade: -");
            return;
        }
        Grade matched = null;
        for (Grade g : controller.getGradesByAssessment(assessment.getAssessmentId())) {
            if (student.getStudentId().equals(g.getStudentId())) {
                matched = g;
                break;
            }
        }
        if (matched != null) {
            marksLabel.setText("Student Marks: " + matched.getMarks() + "/" + assessment.getTotalMarks());
            gradeLabel.setText("Grade: " + matched.getGrade());
        } else {
            marksLabel.setText("Student Marks: -");
            gradeLabel.setText("Grade: -");
        }
        Feedback existing = controller.getFeedback(assessment.getAssessmentId(), student.getStudentId());
        if (existing != null) {
            feedbackArea.setText(existing.getFeedbackText());
        } else {
            feedbackArea.setText("");
        }
    }
}
