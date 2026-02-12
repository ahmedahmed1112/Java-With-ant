package ui;

import model.Student;
import model.User;
import service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class StudentDashboardFrame extends JFrame {

    private final User user;
    private Student student;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel rightPanel = new JPanel(cardLayout);

    private static final String CARD_HOME = "HOME";
    private static final String CARD_PROFILE = "PROFILE";
    private static final String CARD_REGISTER = "REGISTER";
    private static final String CARD_RESULTS = "RESULTS";
    private static final String CARD_COMMENTS = "COMMENTS";

    public StudentDashboardFrame(User user) {
        this.user = user;

        // Look up student in students.txt
        this.student = StudentService.getStudentProfile(user.getUsername());
        if (this.student == null) {
            this.student = new Student();
            this.student.setUserId(user.getUserId());
            this.student.setUsername(user.getUsername());
            this.student.setPassword(user.getPassword());
            this.student.setName(user.getName());
            this.student.setGender(user.getGender());
            this.student.setEmail(user.getEmail());
            this.student.setPhone(user.getPhone());
            this.student.setAge(user.getAge());
            this.student.setRole("STUDENT");
            if (user instanceof Student) {
                this.student.setStudentId(((Student) user).getStudentId());
                this.student.setModuleId(((Student) user).getModuleId());
            }
        }

        setTitle("AFS Student Dashboard");
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

        // Sidebar
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(260, 620));
        sidebar.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel brand = UIUtils.title("AFS Student");
        brand.setFont(UIUtils.font(16, Font.BOLD));

        String stuName = (student.getName() == null) ? "Student" : student.getName();
        JLabel role = new JLabel("Signed in as: " + stuName);
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
        JButton btnRegister = UIUtils.ghostButton("Register Classes");
        JButton btnResults = UIUtils.ghostButton("View Results");
        JButton btnComments = UIUtils.ghostButton("Comments");
        JButton btnLogout = UIUtils.dangerButton("Logout");

        nav.add(btnDashboard);
        nav.add(btnProfile);
        nav.add(btnRegister);
        nav.add(btnResults);
        nav.add(btnComments);

        sidebar.add(brandBox, BorderLayout.NORTH);
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(btnLogout, BorderLayout.SOUTH);

        root.add(sidebar, BorderLayout.WEST);

        // Right panel
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.add(rightPanel, BorderLayout.CENTER);

        rightPanel.add(buildHomePanel(), CARD_HOME);
        rightPanel.add(new StudentEditProfilePanel(student, this), CARD_PROFILE);
        rightPanel.add(new StudentRegisterClassesPanel(student, this), CARD_REGISTER);
        rightPanel.add(new StudentViewResultsPanel(student, this), CARD_RESULTS);
        rightPanel.add(new StudentCommentsPanel(student, this), CARD_COMMENTS);

        cardLayout.show(rightPanel, CARD_HOME);

        btnDashboard.addActionListener(e -> showDashboard());
        btnProfile.addActionListener(e -> cardLayout.show(rightPanel, CARD_PROFILE));
        btnRegister.addActionListener(e -> cardLayout.show(rightPanel, CARD_REGISTER));
        btnResults.addActionListener(e -> cardLayout.show(rightPanel, CARD_RESULTS));
        btnComments.addActionListener(e -> cardLayout.show(rightPanel, CARD_COMMENTS));
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

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(UIUtils.title("Dashboard"));
        titles.add(UIUtils.muted("Choose a student task from the sidebar"));
        topbar.add(titles, BorderLayout.WEST);
        content.add(topbar, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setOpaque(false);

        grid.add(statCard("Edit Profile", "Update your personal details", "Open Profile",
                () -> cardLayout.show(rightPanel, CARD_PROFILE)));
        grid.add(statCard("Register Classes", "Register for available classes", "Open Register",
                () -> cardLayout.show(rightPanel, CARD_REGISTER)));
        grid.add(statCard("View Results", "View your grades and feedback", "Open Results",
                () -> cardLayout.show(rightPanel, CARD_RESULTS)));
        grid.add(statCard("Comments", "Submit comments about modules", "Open Comments",
                () -> cardLayout.show(rightPanel, CARD_COMMENTS)));

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
}
