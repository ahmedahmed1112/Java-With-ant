package ui;

import model.Student;
import service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentCommentsPanel extends JPanel {
    private final Student student;
    private final StudentDashboardFrame parent;

    private final JComboBox<String> moduleBox = new JComboBox<>();
    private final Map<String, String> moduleLecturerMap = new HashMap<>();
    private final JTextArea commentArea = new JTextArea(4, 30);
    private final DefaultTableModel tableModel;
    private final JTable table;

    public StudentCommentsPanel(Student student, StudentDashboardFrame parent) {
        this.student = student;
        this.parent = parent;

        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIUtils.title("Comments"), BorderLayout.NORTH);
        header.add(UIUtils.muted("Submit and view comments about modules"), BorderLayout.SOUTH);

        moduleBox.setBackground(Theme.CARD);
        moduleBox.setForeground(Theme.TEXT);

        commentArea.setBackground(Theme.INPUT_BG);
        commentArea.setForeground(Theme.TEXT);
        commentArea.setCaretColor(Theme.TEXT);
        commentArea.setFont(UIUtils.font(13, Font.PLAIN));
        commentArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Input section
        JPanel inputCard = UIUtils.cardPanel();
        inputCard.setLayout(new BorderLayout(8, 8));
        inputCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel selectPanel = new JPanel(new GridLayout(1, 2, 8, 8));
        selectPanel.setOpaque(false);
        selectPanel.add(UIUtils.muted("Select Module:"));
        selectPanel.add(moduleBox);

        JScrollPane commentScroll = new JScrollPane(commentArea);
        UIUtils.styleScrollPane(commentScroll);

        inputCard.add(selectPanel, BorderLayout.NORTH);
        inputCard.add(commentScroll, BorderLayout.CENTER);

        JButton submitBtn = UIUtils.primaryButton("Submit Comment");
        submitBtn.addActionListener(e -> submitComment());
        inputCard.add(submitBtn, BorderLayout.SOUTH);

        // Existing comments table
        tableModel = new DefaultTableModel(new Object[]{"Comment ID", "Module", "Comment", "Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.applyTableStyle(table);

        JPanel tableCard = UIUtils.cardPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel tLabel = new JLabel("My Comments");
        tLabel.setForeground(Theme.TEXT);
        tLabel.setFont(UIUtils.font(14, Font.BOLD));
        tableCard.add(tLabel, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(table);
        UIUtils.styleScrollPane(sp);
        tableCard.add(sp, BorderLayout.CENTER);

        JButton refreshBtn = UIUtils.ghostButton("Refresh");
        JButton backBtn = UIUtils.ghostButton("Back");
        refreshBtn.addActionListener(e -> loadComments());
        backBtn.addActionListener(e -> parent.showDashboard());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(backBtn);
        buttonPanel.add(refreshBtn);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputCard, tableCard);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(0.4);
        splitPane.setDividerSize(8);

        add(header, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadModules();
        loadComments();
    }

    private void loadModules() {
        moduleBox.removeAllItems();
        moduleLecturerMap.clear();
        String stuId = student.getStudentId() == null ? "" : student.getStudentId();
        List<String[]> modules = StudentService.getModulesForComment(stuId);
        for (String[] m : modules) {
            // m[0]=moduleId, m[1]=moduleName, m[2]=lecturerId
            moduleBox.addItem(m[0] + " - " + m[1]);
            moduleLecturerMap.put(m[0], m[2]);
        }
    }

    private void loadComments() {
        tableModel.setRowCount(0);
        String stuId = student.getStudentId() == null ? "" : student.getStudentId();
        if (stuId.isEmpty()) return;
        List<String[]> comments = StudentService.getComments(stuId);
        for (String[] c : comments) {
            tableModel.addRow(c);
        }
    }

    private void submitComment() {
        String selected = (String) moduleBox.getSelectedItem();
        String comment = commentArea.getText().trim();

        if (selected == null || selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a module.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (comment.length() < 10) {
            JOptionPane.showMessageDialog(this, "Comment must be at least 10 characters.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String moduleId = selected.split(" - ")[0].trim();
        String stuId = student.getStudentId() == null ? "" : student.getStudentId();

        if (stuId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Student ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String lecturerId = moduleLecturerMap.getOrDefault(moduleId, "");
        StudentService.submitComment(stuId, lecturerId, moduleId, comment);
        JOptionPane.showMessageDialog(this, "Comment submitted!", "Success", JOptionPane.INFORMATION_MESSAGE);
        commentArea.setText("");
        loadComments();
    }
}
