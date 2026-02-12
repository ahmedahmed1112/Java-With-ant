package ui;

import model.Student;
import service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class StudentViewResultsPanel extends JPanel {
    private final Student student;
    private final StudentDashboardFrame parent;

    private final DefaultTableModel tableModel;
    private final JTable table;

    public StudentViewResultsPanel(Student student, StudentDashboardFrame parent) {
        this.student = student;
        this.parent = parent;

        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIUtils.title("View Results"), BorderLayout.NORTH);
        header.add(UIUtils.muted("View your grades and feedback per assessment"), BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(new Object[]{"Module", "Assessment", "Marks", "Grade", "Feedback"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.applyTableStyle(table);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;

                int viewRow = table.rowAtPoint(e.getPoint());
                int viewCol = table.columnAtPoint(e.getPoint());
                if (viewRow < 0 || viewCol != 4) return; // Feedback column only.

                int modelRow = table.convertRowIndexToModel(viewRow);
                String module = safeCell(modelRow, 0);
                String assessment = safeCell(modelRow, 1);
                String feedback = safeCell(modelRow, 4);
                showFullFeedback(module, assessment, feedback);
            }
        });

        JScrollPane sp = new JScrollPane(table);
        UIUtils.styleScrollPane(sp);

        JButton refreshBtn = UIUtils.primaryButton("Refresh");
        JButton backBtn = UIUtils.ghostButton("Back");

        refreshBtn.addActionListener(e -> loadResults());
        backBtn.addActionListener(e -> parent.showDashboard());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(backBtn);
        buttonPanel.add(refreshBtn);

        add(header, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadResults();
    }

    private void loadResults() {
        tableModel.setRowCount(0);
        String stuId = student.getStudentId() == null ? "" : student.getStudentId();
        if (stuId.isEmpty()) return;

        List<String[]> results = StudentService.getMyResults(stuId);
        for (String[] r : results) {
            tableModel.addRow(r);
        }
    }

    private String safeCell(int row, int col) {
        Object value = tableModel.getValueAt(row, col);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private void showFullFeedback(String module, String assessment, String feedback) {
        JTextArea area = new JTextArea(feedback.isEmpty() ? "(No feedback provided)" : feedback);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(Theme.INPUT_BG);
        area.setForeground(Theme.TEXT);
        area.setCaretColor(Theme.TEXT);
        area.setFont(UIUtils.font(13, Font.PLAIN));
        area.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane sp = new JScrollPane(area);
        UIUtils.styleScrollPane(sp);
        sp.setPreferredSize(new Dimension(560, 260));

        String title = "Feedback - " + module + " / " + assessment;
        JOptionPane.showMessageDialog(this, sp, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
