package ui;

import model.User;
import util.FileManager;
import util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class LeaderReportsFrame extends JFrame {

    private final User loggedInUser;
    private JPanel mainPanel;

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

    private Set<String> getLeaderModuleIds() {
        Set<String> ids = new HashSet<>();
        if (loggedInUser == null || loggedInUser.getUserId() == null) return ids;
        String leaderId = loggedInUser.getUserId().trim();
        List<String> lines = FileManager.readAll(Constants.MODULES_FILE);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length >= 6 && p[4].trim().equalsIgnoreCase(leaderId)) {
                ids.add(p[0].trim());
            }
        }
        return ids;
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

        tabbedPane.addTab("Module Overview", buildModuleOverview());
        tabbedPane.addTab("Assessment Summary", buildAssessmentSummary());
        tabbedPane.addTab("Student Performance", buildStudentPerformance());
        tabbedPane.addTab("Grade Distribution", buildGradeDistribution());
        tabbedPane.addTab("Feedback Coverage", buildFeedbackCoverage());

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }

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

    private JPanel buildModuleOverview() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Module ID", "Name", "Code", "Credits", "Lecturer", "Students"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Set<String> leaderModules = getLeaderModuleIds();
        List<String> modLines = FileManager.readAll(Constants.MODULES_FILE);
        List<String> scLines = FileManager.readAll(Constants.STUDENT_CLASSES_FILE);
        List<String> classLines = FileManager.readAll(Constants.CLASSES_FILE);

        // Build classId -> moduleId map
        Map<String, String> classToModule = new HashMap<>();
        for (String cLine : classLines) {
            if (cLine == null || cLine.trim().isEmpty()) continue;
            String[] cp = cLine.split("\\|");
            if (cp.length >= 3) classToModule.put(cp[0].trim(), cp[2].trim());
        }

        // Count students per module via student_classes -> classes
        Map<String, Set<String>> moduleStudents = new HashMap<>();
        for (String scLine : scLines) {
            if (scLine == null || scLine.trim().isEmpty()) continue;
            String[] sp = scLine.split("\\|");
            if (sp.length >= 2) {
                String studentId = sp[0].trim();
                String classId = sp[1].trim();
                String modId = classToModule.getOrDefault(classId, "");
                if (!modId.isEmpty()) {
                    moduleStudents.computeIfAbsent(modId, k -> new HashSet<>()).add(studentId);
                }
            }
        }

        for (String line : modLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length < 6) continue;
            String moduleId = p[0].trim();
            if (!isModuleInScope(moduleId, leaderModules)) continue;

            String name = p[1].trim();
            String code = p[2].trim();
            String credits = p[3].trim();
            String lecturerId = p[5].trim();
            int studentCount = moduleStudents.containsKey(moduleId) ? moduleStudents.get(moduleId).size() : 0;

            model.addRow(new Object[]{moduleId, name, code, credits, lecturerId, studentCount});
        }

        return wrapTable(model);
    }

    private JPanel buildAssessmentSummary() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Assessment", "Type", "Module", "Total Marks", "Weightage", "Avg Score", "Pass Rate"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Set<String> leaderModules = getLeaderModuleIds();
        List<String> assLines = FileManager.readAll(Constants.ASSESSMENTS_FILE);
        List<String> gradeLines = FileManager.readAll(Constants.GRADES_FILE);

        for (String line : assLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length < 7) continue;
            String assessmentId = p[0].trim();
            String moduleId = p[1].trim();
            if (!isModuleInScope(moduleId, leaderModules)) continue;

            String name = p[2].trim();
            String type = p[3].trim();
            String totalMarks = p[4].trim();
            String weightage = p[5].trim();

            double sum = 0;
            int count = 0;
            int passed = 0;
            double totalMarksVal = 0;
            try { totalMarksVal = Double.parseDouble(totalMarks); } catch (Exception ignored) {}

            for (String gLine : gradeLines) {
                if (gLine == null || gLine.trim().isEmpty()) continue;
                String[] gp = gLine.split("\\|");
                if (gp.length < 5) continue;
                if (gp[1].trim().equals(assessmentId)) {
                    count++;
                    try {
                        double marks = Double.parseDouble(gp[3].trim());
                        sum += marks;
                        if (totalMarksVal > 0 && (marks / totalMarksVal * 100) >= 50) {
                            passed++;
                        }
                    } catch (Exception ignored) {}
                }
            }

            String avgScore = count > 0 ? String.format("%.1f", sum / count) : "-";
            String passRate = count > 0 ? String.format("%.0f%%", (passed * 100.0 / count)) : "-";

            model.addRow(new Object[]{name, type, moduleId, totalMarks, weightage, avgScore, passRate});
        }

        return wrapTable(model);
    }

    private JPanel buildStudentPerformance() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Student ID", "Name", "Module", "Avg Marks", "Assessments Graded"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Set<String> leaderModules = getLeaderModuleIds();

        // Build userId -> name map from users.txt
        Map<String, String> userIdToName = new HashMap<>();
        List<String> userLines = FileManager.readAll(Constants.USERS_FILE);
        for (String uLine : userLines) {
            if (uLine == null || uLine.trim().isEmpty()) continue;
            String[] up = uLine.split("\\|");
            if (up.length >= 9) userIdToName.put(up[0].trim(), up[3].trim());
        }

        List<String> stuLines = FileManager.readAll(Constants.STUDENTS_FILE);
        List<String> gradeLines = FileManager.readAll(Constants.GRADES_FILE);

        // Build classId -> moduleId and studentId -> moduleIds
        List<String> classLines = FileManager.readAll(Constants.CLASSES_FILE);
        List<String> scLines = FileManager.readAll(Constants.STUDENT_CLASSES_FILE);

        Map<String, String> classToModule = new HashMap<>();
        for (String cLine : classLines) {
            if (cLine == null || cLine.trim().isEmpty()) continue;
            String[] cp = cLine.split("\\|");
            if (cp.length >= 3) classToModule.put(cp[0].trim(), cp[2].trim());
        }

        Map<String, Set<String>> studentModules = new HashMap<>();
        for (String scLine : scLines) {
            if (scLine == null || scLine.trim().isEmpty()) continue;
            String[] sp = scLine.split("\\|");
            if (sp.length >= 2) {
                String sid = sp[0].trim();
                String modId = classToModule.getOrDefault(sp[1].trim(), "");
                if (!modId.isEmpty()) {
                    studentModules.computeIfAbsent(sid, k -> new HashSet<>()).add(modId);
                }
            }
        }

        // Build assessment -> module map for scoping grades
        Map<String, String> assessmentToModule = new HashMap<>();
        List<String> assLines = FileManager.readAll(Constants.ASSESSMENTS_FILE);
        for (String aLine : assLines) {
            if (aLine == null || aLine.trim().isEmpty()) continue;
            String[] ap = aLine.split("\\|");
            if (ap.length >= 2) assessmentToModule.put(ap[0].trim(), ap[1].trim());
        }

        for (String line : stuLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);

            // Supports:
            // 1) username|password|name|gender|email|phone|age|studentId|moduleId
            // 2) legacy username|password|name|gender|email|phone|age|studentId|extra|moduleId
            // 3) studentId|userId
            // 4) legacy studentId|userId|extra
            String studentId;
            String name;
            if (p.length >= 10) {
                studentId = safe(p[7]);
                name = safe(p[2]);
            } else if (p.length >= 9) {
                studentId = safe(p[7]);
                name = safe(p[2]);
            } else if (p.length >= 2) {
                studentId = safe(p[0]);
                String userId = safe(p[1]);
                name = userIdToName.getOrDefault(userId, userId);
            } else {
                continue;
            }
            if (studentId.isEmpty()) continue;
            if (name.isEmpty()) name = studentId;

            Set<String> mods = studentModules.getOrDefault(studentId, new HashSet<>());
            Set<String> scopedModules = new LinkedHashSet<>();
            for (String m : mods) {
                if (isModuleInScope(m, leaderModules)) scopedModules.add(m);
            }

            Set<String> gradedModules = new LinkedHashSet<>();

            double sum = 0;
            int count = 0;
            for (String gLine : gradeLines) {
                if (gLine == null || gLine.trim().isEmpty()) continue;
                String[] gp = gLine.split("\\|");
                if (gp.length < 5) continue;
                if (gp[2].trim().equalsIgnoreCase(studentId)) {
                    String assModId = assessmentToModule.getOrDefault(gp[1].trim(), "");
                    if (!isModuleInScope(assModId, leaderModules)) continue;
                    if (!assModId.isEmpty()) gradedModules.add(assModId);
                    count++;
                    try { sum += Double.parseDouble(gp[3].trim()); } catch (Exception ignored) {}
                }
            }

            if (scopedModules.isEmpty()) {
                scopedModules.addAll(gradedModules);
            }
            if (hasLeaderContext() && scopedModules.isEmpty() && gradedModules.isEmpty()) continue;

            String moduleDisplay = scopedModules.isEmpty() ? "-" : String.join(", ", scopedModules);
            String avg = count > 0 ? String.format("%.1f", sum / count) : "-";
            model.addRow(new Object[]{studentId, name, moduleDisplay, avg, count});
        }

        return wrapTable(model);
    }

    private JPanel buildGradeDistribution() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Grade", "Count", "Percentage"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Set<String> leaderModules = getLeaderModuleIds();

        // Build assessment -> module map
        Map<String, String> assessmentToModule = new HashMap<>();
        List<String> assLines = FileManager.readAll(Constants.ASSESSMENTS_FILE);
        for (String aLine : assLines) {
            if (aLine == null || aLine.trim().isEmpty()) continue;
            String[] ap = aLine.split("\\|");
            if (ap.length >= 2) assessmentToModule.put(ap[0].trim(), ap[1].trim());
        }

        List<String> gradeLines = FileManager.readAll(Constants.GRADES_FILE);
        Map<String, Integer> gradeCounts = new LinkedHashMap<>();
        String[] gradeOrder = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F+", "F", "F-"};
        for (String g : gradeOrder) gradeCounts.put(g, 0);

        int total = 0;
        for (String line : gradeLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length < 5) continue;

            String assessmentId = p[1].trim();
            String modId = assessmentToModule.getOrDefault(assessmentId, "");
            if (!isModuleInScope(modId, leaderModules)) continue;

            String grade = p[4].trim();
            total++;
            gradeCounts.put(grade, gradeCounts.getOrDefault(grade, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : gradeCounts.entrySet()) {
            int count = entry.getValue();
            if (count == 0) continue;
            String pct = total > 0 ? String.format("%.1f%%", count * 100.0 / total) : "0%";
            model.addRow(new Object[]{entry.getKey(), count, pct});
        }

        return wrapTable(model);
    }

    private JPanel buildFeedbackCoverage() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Assessment", "Total Graded", "With Feedback", "Coverage %"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Set<String> leaderModules = getLeaderModuleIds();
        List<String> assLines = FileManager.readAll(Constants.ASSESSMENTS_FILE);
        List<String> gradeLines = FileManager.readAll(Constants.GRADES_FILE);
        List<String> fbLines = FileManager.readAll(Constants.FEEDBACK_FILE);

        for (String line : assLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length < 7) continue;
            String assessmentId = p[0].trim();
            String moduleId = p[1].trim();
            if (!isModuleInScope(moduleId, leaderModules)) continue;

            String name = p[2].trim();

            Set<String> gradedStudents = new HashSet<>();
            for (String gLine : gradeLines) {
                if (gLine == null || gLine.trim().isEmpty()) continue;
                String[] gp = gLine.split("\\|");
                if (gp.length >= 3 && gp[1].trim().equals(assessmentId)) {
                    gradedStudents.add(gp[2].trim());
                }
            }

            Set<String> feedbackStudents = new HashSet<>();
            for (String fLine : fbLines) {
                if (fLine == null || fLine.trim().isEmpty()) continue;
                String[] fp = fLine.split("\\|");
                if (fp.length >= 3 && fp[1].trim().equals(assessmentId)) {
                    feedbackStudents.add(fp[2].trim());
                }
            }

            int totalGraded = gradedStudents.size();
            feedbackStudents.retainAll(gradedStudents);
            int withFeedback = feedbackStudents.size();
            String coverage = totalGraded > 0 ? String.format("%.0f%%", withFeedback * 100.0 / totalGraded) : "-";

            model.addRow(new Object[]{name, totalGraded, withFeedback, coverage});
        }

        return wrapTable(model);
    }

    private JPanel wrapTable(DefaultTableModel model) {
        JPanel panel = UIUtils.cardPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTable table = new JTable(model);
        UIUtils.applyTableStyle(table);

        JScrollPane sp = new JScrollPane(table);
        UIUtils.styleScrollPane(sp);
        panel.add(sp, BorderLayout.CENTER);

        return panel;
    }

    private boolean hasLeaderContext() {
        return loggedInUser != null
                && loggedInUser.getUserId() != null
                && !loggedInUser.getUserId().trim().isEmpty();
    }

    private boolean isModuleInScope(String moduleId, Set<String> leaderModules) {
        String mod = safe(moduleId);
        if (!hasLeaderContext()) return true;
        return leaderModules.contains(mod);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
