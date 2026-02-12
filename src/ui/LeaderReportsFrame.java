package ui;

import model.User;
import util.Constants;
import util.FileManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * LeaderReportsFrame
 * ------------------
 * Reusable + leader-scoped analyzed reports.
 *
 * Fixes:
 * 1) Tab header colors (avoid white-on-white in some Look & Feels)
 * 2) Reads from correct .txt files using util.Constants
 * 3) Scopes all reports to the logged-in leader (modules + lecturer assignments)
 *
 * OOP used:
 * - Abstraction: ReportTab interface to define "a report tab"
 * - Encapsulation: helper methods to read/parse/filter data are centralized
 */
public class LeaderReportsFrame extends JFrame {

    private final User loggedInUser;
    private JPanel mainPanel;

    // Small abstraction: each report tab knows how to build itself
    private interface ReportTab {
        String title();
        JPanel build();
    }

    public LeaderReportsFrame() {
        this(null);
    }

    public LeaderReportsFrame(User user) {
        this.loggedInUser = user;
        buildUI();

        setTitle("Leader Reports");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(mainPanel);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void buildUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.BG);
        mainPanel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        header.add(UIUtils.title("Analyzed Reports"), BorderLayout.NORTH);
        header.add(UIUtils.muted("5 data-driven reports from the system files"), BorderLayout.SOUTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        styleTabbedPane(tabbedPane);

        // Define tabs using reusable abstraction
        List<ReportTab> tabs = Arrays.asList(
                new ReportTab() {
                    public String title() { return "Module Overview"; }
                    public JPanel build() { return buildModuleOverview(); }
                },
                new ReportTab() {
                    public String title() { return "Assessment Summary"; }
                    public JPanel build() { return buildAssessmentSummary(); }
                },
                new ReportTab() {
                    public String title() { return "Student Performance"; }
                    public JPanel build() { return buildStudentPerformance(); }
                },
                new ReportTab() {
                    public String title() { return "Grade Distribution"; }
                    public JPanel build() { return buildGradeDistribution(); }
                },
                new ReportTab() {
                    public String title() { return "Feedback Coverage"; }
                    public JPanel build() { return buildFeedbackCoverage(); }
                }
        );

        addTabs(tabbedPane, tabs);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void addTabs(JTabbedPane tabbedPane, List<ReportTab> tabs) {
        for (ReportTab t : tabs) {
            tabbedPane.addTab(t.title(), t.build());
        }
    }

    /**
     * Fix: Tab headers sometimes ignore background/foreground in Windows LAF.
     * We paint our own dark background + white text.
     */
    private void styleTabbedPane(JTabbedPane tabbedPane) {
        tabbedPane.setOpaque(false);
        tabbedPane.setFont(UIUtils.font(13, Font.BOLD));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        tabbedPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement,
                                              int tabIndex, int x, int y, int w, int h,
                                              boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setColor(isSelected ? new Color(60, 120, 180) : new Color(35, 40, 52));
                    g2.fillRoundRect(x + 1, y + 2, w - 2, h - 2, 10, 10);
                } finally {
                    g2.dispose();
                }
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font,
                                     FontMetrics metrics, int tabIndex,
                                     String title, Rectangle textRect, boolean isSelected) {
                g.setFont(font);
                g.setColor(Color.WHITE);
                g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // remove default bright border
            }
        });
    }

    // -------------------- Leader scoping helpers (encapsulation) --------------------

    private String leaderId() {
        if (loggedInUser == null) return "";
        String id = loggedInUser.getUserId();
        return id == null ? "" : id.trim();
    }

    private List<String[]> readDelimited(String filePath) {
        List<String[]> out = new ArrayList<>();
        List<String> lines = FileManager.readAll(filePath);

        for (String line : lines) {
            if (line == null) continue;
            line = line.trim();
            if (line.isEmpty()) continue;

            // skip header lines like "LeaderID|LecturerID"
            if (line.toLowerCase().startsWith("leaderid")) continue;

            out.add(line.split("\\Q" + Constants.DELIMITER + "\\E"));
        }
        return out;
    }

    private Set<String> lecturersUnderLeader(String leaderId) {
        Set<String> ids = new HashSet<>();
        if (leaderId.isEmpty()) return ids;

        for (String[] p : readDelimited(Constants.LEADER_LECTURER_FILE)) {
            if (p.length < 2) continue;
            String lId = safe(p, 0);
            String lecId = safe(p, 1);
            if (lId.equalsIgnoreCase(leaderId) && !lecId.isEmpty()) {
                ids.add(lecId.toUpperCase());
            }
        }
        return ids;
    }

    /**
     * modules.txt format (based on your project):
     * moduleId|name|code|credits|leaderId|lecturerId
     */
    private List<String[]> modulesForLeader(String leaderId) {
        List<String[]> result = new ArrayList<>();
        if (leaderId.isEmpty()) return result;

        Set<String> lecSet = lecturersUnderLeader(leaderId);

        for (String[] p : readDelimited(Constants.MODULES_FILE)) {
            if (p.length < 6) continue;

            String mLeader = safe(p, 4);
            String mLecturer = safe(p, 5);

            boolean ok = mLeader.equalsIgnoreCase(leaderId);
            if (!ok && !lecSet.isEmpty() && !mLecturer.isEmpty()) {
                ok = lecSet.contains(mLecturer.toUpperCase());
            }

            if (ok) result.add(p);
        }
        return result;
    }

    private Set<String> leaderModuleIds() {
        Set<String> ids = new HashSet<>();
        for (String[] m : modulesForLeader(leaderId())) {
            ids.add(safe(m, 0));
        }
        return ids;
    }

    // -------------------- Reports --------------------

    private JPanel buildModuleOverview() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Module ID", "Name", "Code", "Credits", "Lecturer", "Students"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Set<String> allowedModules = leaderModuleIds();
        List<String[]> students = readDelimited(Constants.STUDENTS_FILE);

        for (String[] m : modulesForLeader(leaderId())) {
            String moduleId = safe(m, 0);
            String name = safe(m, 1);
            String code = safe(m, 2);
            String credits = safe(m, 3);
            String lecturerId = safe(m, 5);

            int studentCount = 0;
            for (String[] s : students) {
                // students.txt in your system is 10 fields, moduleId is at index 9.
                if (s.length >= 10 && safe(s, 9).equalsIgnoreCase(moduleId)) studentCount++;

                // fallback legacy format (3 fields)
                if (s.length >= 3 && s.length < 10 && safe(s, 2).equalsIgnoreCase(moduleId)) studentCount++;
            }

            // extra safety: ensure module is leader-owned
            if (allowedModules.contains(moduleId)) {
                model.addRow(new Object[]{moduleId, name, code, credits, lecturerId, studentCount});
            }
        }

        return wrapTable(model);
    }

    private JPanel buildAssessmentSummary() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Assessment", "Type", "Module", "Total Marks", "Weightage", "Avg Score", "Pass Rate"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Set<String> allowedModules = leaderModuleIds();
        List<String[]> assessments = readDelimited(Constants.ASSESSMENTS_FILE);
        List<String[]> grades = readDelimited(Constants.GRADES_FILE);

        for (String[] a : assessments) {
            // some data has 7 fields; we only need first 6 safely
            if (a.length < 6) continue;

            String assessmentId = safe(a, 0);
            String moduleId = safe(a, 1);
            if (!allowedModules.contains(moduleId)) continue;

            String name = safe(a, 2);
            String type = safe(a, 3);
            String totalMarksStr = safe(a, 4);
            String weightage = safe(a, 5);

            double totalMarksVal = parseDoubleSafe(totalMarksStr, 0);

            double sum = 0;
            int count = 0;
            int passed = 0;

            for (String[] g : grades) {
                if (g.length < 4) continue;
                if (!safe(g, 1).equalsIgnoreCase(assessmentId)) continue;

                count++;
                double marks = parseDoubleSafe(safe(g, 3), 0);
                sum += marks;

                if (totalMarksVal > 0 && (marks / totalMarksVal * 100.0) >= 50.0) {
                    passed++;
                }
            }

            String avgScore = count > 0 ? String.format("%.1f", (sum / count)) : "-";
            String passRate = count > 0 ? String.format("%.0f%%", (passed * 100.0 / count)) : "-";

            model.addRow(new Object[]{name, type, moduleId, totalMarksStr, weightage, avgScore, passRate});
        }

        return wrapTable(model);
    }

    private JPanel buildStudentPerformance() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Student ID", "Name", "Module", "Avg Marks", "Assessments Graded"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Set<String> allowedModules = leaderModuleIds();
        List<String[]> students = readDelimited(Constants.STUDENTS_FILE);
        List<String[]> grades = readDelimited(Constants.GRADES_FILE);

        for (String[] s : students) {
            String studentId, name, moduleId;

            if (s.length >= 10) {
                // your students.txt: name index 2, studentId index 7, moduleId index 9
                name = safe(s, 2);
                studentId = safe(s, 7);
                moduleId = safe(s, 9);
            } else if (s.length >= 3) {
                // legacy: studentId|name|moduleId
                studentId = safe(s, 0);
                name = safe(s, 1);
                moduleId = safe(s, 2);
            } else {
                continue;
            }

            if (!allowedModules.contains(moduleId)) continue;

            double sum = 0;
            int count = 0;

            for (String[] g : grades) {
                if (g.length < 4) continue;
                if (!safe(g, 2).equalsIgnoreCase(studentId)) continue;

                count++;
                sum += parseDoubleSafe(safe(g, 3), 0);
            }

            String avg = count > 0 ? String.format("%.1f", (sum / count)) : "-";
            model.addRow(new Object[]{studentId, name, moduleId, avg, count});
        }

        return wrapTable(model);
    }

    private JPanel buildGradeDistribution() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Grade", "Count", "Percentage"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Set<String> allowedModules = leaderModuleIds();

        // Map assessmentId -> moduleId so grades can be filtered properly
        Map<String, String> assessmentToModule = new HashMap<>();
        for (String[] a : readDelimited(Constants.ASSESSMENTS_FILE)) {
            if (a.length < 2) continue;
            assessmentToModule.put(safe(a, 0), safe(a, 1));
        }

        Map<String, Integer> gradeCounts = new LinkedHashMap<>();
        String[] order = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F+", "F", "F-"};
        for (String g : order) gradeCounts.put(g, 0);

        int total = 0;
        for (String[] g : readDelimited(Constants.GRADES_FILE)) {
            if (g.length < 5) continue;

            String assessmentId = safe(g, 1);
            String moduleId = assessmentToModule.getOrDefault(assessmentId, "");
            if (!allowedModules.contains(moduleId)) continue;

            String grade = safe(g, 4);
            total++;
            gradeCounts.put(grade, gradeCounts.getOrDefault(grade, 0) + 1);
        }

        for (Map.Entry<String, Integer> e : gradeCounts.entrySet()) {
            int count = e.getValue();
            if (count == 0) continue;

            String pct = total > 0 ? String.format("%.1f%%", (count * 100.0 / total)) : "0%";
            model.addRow(new Object[]{e.getKey(), count, pct});
        }

        return wrapTable(model);
    }

    private JPanel buildFeedbackCoverage() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Assessment", "Total Graded", "With Feedback", "Coverage %"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Set<String> allowedModules = leaderModuleIds();

        Map<String, String> assessmentIdToModule = new HashMap<>();
        Map<String, String> assessmentIdToName = new HashMap<>();

        for (String[] a : readDelimited(Constants.ASSESSMENTS_FILE)) {
            if (a.length < 3) continue;
            assessmentIdToModule.put(safe(a, 0), safe(a, 1));
            assessmentIdToName.put(safe(a, 0), safe(a, 2));
        }

        List<String[]> grades = readDelimited(Constants.GRADES_FILE);
        List<String[]> feedback = readDelimited(Constants.FEEDBACK_FILE);

        for (Map.Entry<String, String> entry : assessmentIdToModule.entrySet()) {
            String assessmentId = entry.getKey();
            String moduleId = entry.getValue();
            if (!allowedModules.contains(moduleId)) continue;

            String name = assessmentIdToName.getOrDefault(assessmentId, assessmentId);

            Set<String> gradedStudents = new HashSet<>();
            for (String[] g : grades) {
                if (g.length < 3) continue;
                if (safe(g, 1).equalsIgnoreCase(assessmentId)) {
                    gradedStudents.add(safe(g, 2));
                }
            }

            Set<String> feedbackStudents = new HashSet<>();
            for (String[] f : feedback) {
                if (f.length < 3) continue;
                if (safe(f, 1).equalsIgnoreCase(assessmentId)) {
                    feedbackStudents.add(safe(f, 2));
                }
            }

            int totalGraded = gradedStudents.size();
            int withFeedback = feedbackStudents.size();
            String coverage = totalGraded > 0 ? String.format("%.0f%%", (withFeedback * 100.0 / totalGraded)) : "-";

            model.addRow(new Object[]{name, totalGraded, withFeedback, coverage});
        }

        return wrapTable(model);
    }

    // -------------------- Reusable UI helper --------------------

    private JPanel wrapTable(DefaultTableModel model) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG);

        JTable table = new JTable(model);
        UIUtils.applyTableStyle(table);

        JScrollPane sp = new JScrollPane(table);
        UIUtils.styleScrollPane(sp);
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    // -------------------- Small safe helpers --------------------

    private String safe(String[] arr, int idx) {
        if (arr == null || idx < 0 || idx >= arr.length || arr[idx] == null) return "";
        return arr[idx].trim();
    }

    private double parseDoubleSafe(String s, double def) {
        try { return Double.parseDouble(s.trim()); }
        catch (Exception e) { return def; }
    }
}
