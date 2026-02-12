package ui;

import controller.FileHandler;
import controller.LecturerController;
import model.Lecturer;
import model.User;
import util.FileManager;
import util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class LecturerDashboardFrame extends JFrame {

    private final User user;
    private Lecturer lecturer;
    private LecturerController controller;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel rightPanel = new JPanel(cardLayout);

    private static final String CARD_HOME = "HOME";
    private static final String CARD_PROFILE = "PROFILE";
    private static final String CARD_ASSESSMENT = "ASSESSMENT";
    private static final String CARD_MARKS = "MARKS";
    private static final String CARD_FEEDBACK = "FEEDBACK";

    private String assignedModuleName = "None";
    private LecturerEditProfilePanel profilePanel;
    private LecturerDesignAssessmentPanel assessmentPanel;
    private LecturerKeyInMarksPanel marksPanel;
    private LecturerProvideFeedbackPanel feedbackPanel;

    public LecturerDashboardFrame(User user) {
        this.user = user;

        // Create file handler and controller
        FileHandler fileHandler = new FileHandler();
        this.controller = new LecturerController(fileHandler);

        // Build a Lecturer object directly from the User data
        this.lecturer = new Lecturer();
        this.lecturer.setUserId(user.getUserId());
        this.lecturer.setUsername(user.getUsername());
        this.lecturer.setPassword(user.getPassword());
        this.lecturer.setName(user.getName());
        this.lecturer.setGender(user.getGender());
        this.lecturer.setEmail(user.getEmail());
        this.lecturer.setPhone(user.getPhone());
        this.lecturer.setAge(user.getAge());
        this.lecturer.setRole("LECTURER");

        // Look up the module from modules.txt by matching lecturerId (column 5) to user's userId
        // modules.txt format: moduleId|moduleName|moduleCode|creditHours|leaderId|lecturerId
        List<String> moduleLines = FileManager.readAll(Constants.MODULES_FILE);
        for (String line : moduleLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length >= 6) {
                String lecturerId = parts[5].trim();
                if (lecturerId.equalsIgnoreCase(user.getUserId())) {
                    this.lecturer.setAssignedModuleId(parts[0].trim());
                    this.lecturer.setAcademicLeaderId(parts[4].trim());
                    this.assignedModuleName = parts[1].trim();
                    break;
                }
            }
        }

        setTitle("AFS Lecturer Dashboard");
        setSize(1020, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                goToLogin();
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        setContentPane(root);

        // ===== Sidebar =====
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(260, 620));
        sidebar.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel brand = UIUtils.title("AFS Lecturer");
        brand.setFont(UIUtils.font(16, Font.BOLD));

        String lecName = (lecturer.getName() == null) ? "Lecturer" : lecturer.getName();
        JLabel role = new JLabel("Signed in as: " + lecName);
        role.setForeground(Theme.MUTED);
        role.setFont(UIUtils.font(12, Font.PLAIN));

        JPanel brandBox = new JPanel(new GridLayout(2, 1, 0, 6));
        brandBox.setOpaque(false);
        brandBox.add(brand);
        brandBox.add(role);

        JPanel nav = new JPanel(new GridLayout(12, 1, 10, 10));
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(18, 0, 18, 0));

        JButton btnDashboard = UIUtils.ghostButton("Dashboard");
        JButton btnProfile = UIUtils.ghostButton("Edit Profile");
        JButton btnAssessment = UIUtils.ghostButton("Design Assessment");
        JButton btnMarks = UIUtils.ghostButton("Key-in Marks");
        JButton btnFeedback = UIUtils.ghostButton("Provide Feedback");
        JButton btnLogout = UIUtils.dangerButton("Logout");

        nav.add(btnDashboard);
        nav.add(btnProfile);
        nav.add(btnAssessment);
        nav.add(btnMarks);
        nav.add(btnFeedback);

        sidebar.add(brandBox, BorderLayout.NORTH);
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(btnLogout, BorderLayout.SOUTH);

        root.add(sidebar, BorderLayout.WEST);

        // ===== Right panel =====
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.add(rightPanel, BorderLayout.CENTER);

        // Build panels
        profilePanel = new LecturerEditProfilePanel(lecturer, controller, this);
        assessmentPanel = new LecturerDesignAssessmentPanel(lecturer, controller, this);
        marksPanel = new LecturerKeyInMarksPanel(lecturer, controller, this);
        feedbackPanel = new LecturerProvideFeedbackPanel(lecturer, controller, this);

        rightPanel.add(buildHomePanel(), CARD_HOME);
        rightPanel.add(profilePanel, CARD_PROFILE);
        rightPanel.add(assessmentPanel, CARD_ASSESSMENT);
        rightPanel.add(marksPanel, CARD_MARKS);
        rightPanel.add(feedbackPanel, CARD_FEEDBACK);

        cardLayout.show(rightPanel, CARD_HOME);

        // Actions
        btnDashboard.addActionListener(e -> showDashboard());
        btnProfile.addActionListener(e -> showProfilePanel());
        btnAssessment.addActionListener(e -> showAssessmentPanel());
        btnMarks.addActionListener(e -> showMarksPanel());
        btnFeedback.addActionListener(e -> showFeedbackPanel());
        btnLogout.addActionListener(e -> goToLogin());
    }

    public void showDashboard() {
        cardLayout.show(rightPanel, CARD_HOME);
    }

    private JPanel buildHomePanel() {
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setOpaque(false);
        topbar.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel titles = new JPanel(new GridLayout(3, 1));
        titles.setOpaque(false);
        titles.add(UIUtils.title("Dashboard"));
        titles.add(UIUtils.muted("Choose a lecturer task from the sidebar"));

        String moduleId = lecturer.getAssignedModuleId();
        String moduleDisplay = (moduleId != null && !moduleId.trim().isEmpty())
                ? moduleId + " - " + assignedModuleName
                : "No module assigned";
        JLabel moduleLabel = new JLabel("Assigned Module: " + moduleDisplay);
        moduleLabel.setForeground(Theme.TEXT);
        moduleLabel.setFont(UIUtils.font(13, Font.BOLD));
        titles.add(moduleLabel);

        topbar.add(titles, BorderLayout.WEST);
        content.add(topbar, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setOpaque(false);

        grid.add(statCard("Edit Profile", "Update your personal details", "Open Profile",
                this::showProfilePanel));
        grid.add(statCard("Design Assessment", "Create, update, delete assessments", "Open Assessment",
                this::showAssessmentPanel));
        grid.add(statCard("Key-in Marks", "Enter marks for student assessments", "Open Marks",
                this::showMarksPanel));
        grid.add(statCard("Provide Feedback", "Give feedback to students", "Open Feedback",
                this::showFeedbackPanel));

        content.add(grid, BorderLayout.CENTER);
        return content;
    }

    private JPanel statCard(String title, String desc, String buttonText, Runnable action) {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel t = new JLabel(title);
        t.setForeground(Theme.TEXT);
        t.setFont(UIUtils.font(16, Font.BOLD));

        JLabel d = new JLabel("<html><div style='width:260px;'>" + desc + "</div></html>");
        d.setForeground(Theme.MUTED);
        d.setFont(UIUtils.font(12, Font.PLAIN));

        JButton a = UIUtils.primaryButton(buttonText);
        a.addActionListener(e -> action.run());

        card.add(t, BorderLayout.NORTH);
        card.add(d, BorderLayout.CENTER);
        card.add(a, BorderLayout.SOUTH);

        return card;
    }

    private void goToLogin() {
        new LoginFrame().setVisible(true);
        dispose();
    }

    private void showProfilePanel() {
        cardLayout.show(rightPanel, CARD_PROFILE);
    }

    private void showAssessmentPanel() {
        if (assessmentPanel != null) assessmentPanel.refreshTableAndInputs();
        cardLayout.show(rightPanel, CARD_ASSESSMENT);
    }

    private void showMarksPanel() {
        if (marksPanel != null) marksPanel.refreshAssessments();
        cardLayout.show(rightPanel, CARD_MARKS);
    }

    private void showFeedbackPanel() {
        if (feedbackPanel != null) feedbackPanel.refreshAssessmentsAndStudents();
        cardLayout.show(rightPanel, CARD_FEEDBACK);
    }
}
