package ui;

import model.Student;
import service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentRegisterClassesPanel extends JPanel {
    private final Student student;
    private final StudentDashboardFrame parent;

    private final DefaultTableModel availableModel;
    private final JTable availableTable;
    private final DefaultTableModel registeredModel;
    private final JTable registeredTable;

    public StudentRegisterClassesPanel(Student student, StudentDashboardFrame parent) {
        this.student = student;
        this.parent = parent;

        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIUtils.title("Register Classes"), BorderLayout.NORTH);
        header.add(UIUtils.muted("Register for available classes and view your registered classes"), BorderLayout.SOUTH);

        // Available classes table
        availableModel = new DefaultTableModel(new Object[]{"Class ID", "Class Name", "Module ID"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        availableTable = new JTable(availableModel);
        styleTable(availableTable);

        // Registered classes table
        registeredModel = new DefaultTableModel(new Object[]{"Student ID", "Class ID"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        registeredTable = new JTable(registeredModel);
        styleTable(registeredTable);

        JButton registerBtn = UIUtils.primaryButton("Register Selected");
        JButton refreshBtn = UIUtils.ghostButton("Refresh");
        JButton backBtn = UIUtils.ghostButton("Back");

        registerBtn.addActionListener(e -> registerForClass());
        refreshBtn.addActionListener(e -> loadData());
        backBtn.addActionListener(e -> parent.showDashboard());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(backBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(registerBtn);

        // Layout
        JPanel topCard = UIUtils.cardPanel();
        topCard.setLayout(new BorderLayout());
        topCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel avLabel = new JLabel("Available Classes");
        avLabel.setForeground(Theme.TEXT);
        avLabel.setFont(UIUtils.font(14, Font.BOLD));
        topCard.add(avLabel, BorderLayout.NORTH);
        JScrollPane sp1 = new JScrollPane(availableTable);
        UIUtils.styleScrollPane(sp1);
        topCard.add(sp1, BorderLayout.CENTER);

        JPanel bottomCard = UIUtils.cardPanel();
        bottomCard.setLayout(new BorderLayout());
        bottomCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel regLabel = new JLabel("My Registered Classes");
        regLabel.setForeground(Theme.TEXT);
        regLabel.setFont(UIUtils.font(14, Font.BOLD));
        bottomCard.add(regLabel, BorderLayout.NORTH);
        JScrollPane sp2 = new JScrollPane(registeredTable);
        UIUtils.styleScrollPane(sp2);
        bottomCard.add(sp2, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topCard, bottomCard);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(8);

        add(header, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loadData();
    }

    private void styleTable(JTable table) {
        UIUtils.applyTableStyle(table);
    }

    private void loadData() {
        // Available classes
        availableModel.setRowCount(0);
        List<String[]> classes = StudentService.getAvailableClasses(null);
        for (String[] c : classes) {
            availableModel.addRow(c);
        }

        // Registered classes
        registeredModel.setRowCount(0);
        String stuId = student.getStudentId() == null ? "" : student.getStudentId();
        if (!stuId.isEmpty()) {
            List<String[]> registered = StudentService.getRegisteredClasses(stuId);
            for (String[] r : registered) {
                registeredModel.addRow(r);
            }
        }
    }

    private void registerForClass() {
        int row = availableTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a class to register.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String classId = availableModel.getValueAt(row, 0).toString();
        String stuId = student.getStudentId() == null ? "" : student.getStudentId();

        if (stuId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Student ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (StudentService.isAlreadyRegistered(stuId, classId)) {
            JOptionPane.showMessageDialog(this, "Already registered for this class.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StudentService.registerForClass(stuId, classId);
        JOptionPane.showMessageDialog(this, "Registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        loadData();
    }
}
