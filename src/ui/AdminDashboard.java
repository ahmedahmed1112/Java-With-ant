package ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.User;

public class AdminDashboard extends JFrame {

    private final User adminUser;

    private final CardLayout centerLayout = new CardLayout();
    private final JPanel centerCards = new JPanel(centerLayout);

    private static final String CARD_HOME = "home";
    private static final String CARD_USERS = "users";
    private static final String CARD_CLASSES = "classes";
    private static final String CARD_GRADING = "grading";
    private static final String CARD_ASSIGN = "assign";

    private JLabel pageTitle;
    private JLabel pageSub;

    public AdminDashboard(User adminUser) {
        this.adminUser = adminUser;

        setTitle("AFS Admin Dashboard");
        setSize(1020, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new LoginFrame().setVisible(true);
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

        JLabel brand = new JLabel("AFS Admin");
        brand.setForeground(Theme.TEXT);
        brand.setFont(UIUtils.font(16, Font.BOLD));

        JLabel role = new JLabel("Signed in as: " + adminUser.getName());
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
        JButton btnUsers = UIUtils.ghostButton("Manage Users");
        JButton btnClasses = UIUtils.ghostButton("Manage Classes");
        JButton btnGrading = UIUtils.ghostButton("Manage Grading");
        JButton btnAssign = UIUtils.ghostButton("Assign Lecturers");
        JButton btnLogout = UIUtils.dangerButton("Logout");

        nav.add(btnDashboard);
        nav.add(btnUsers);
        nav.add(btnClasses);
        nav.add(btnGrading);
        nav.add(btnAssign);

        sidebar.add(brandBox, BorderLayout.NORTH);
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(btnLogout, BorderLayout.SOUTH);

        root.add(sidebar, BorderLayout.WEST);

        // ===== Main Content Wrapper =====
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.add(contentWrapper, BorderLayout.CENTER);

        // ===== Topbar =====
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setOpaque(false);
        topbar.setBorder(new EmptyBorder(0, 0, 14, 0));

        pageTitle = UIUtils.title("Dashboard");
        pageSub = UIUtils.muted("Choose an admin task from the sidebar");

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(pageTitle);
        titles.add(pageSub);

        topbar.add(titles, BorderLayout.WEST);

        contentWrapper.add(topbar, BorderLayout.NORTH);

        // ===== Center Cards =====
        centerCards.setOpaque(false);

        JPanel homePanel = buildHomePanel();
        JPanel usersPanel = new ManageUsersFrame();
        JPanel classesPanel = new ManageClassesFrame();
        JPanel gradingPanel = new ManageGradingFrame();
        JPanel assignPanel = new ManageAssignLecturersFrame();

        centerCards.add(homePanel, CARD_HOME);
        centerCards.add(usersPanel, CARD_USERS);
        centerCards.add(classesPanel, CARD_CLASSES);
        centerCards.add(gradingPanel, CARD_GRADING);
        centerCards.add(assignPanel, CARD_ASSIGN);

        contentWrapper.add(centerCards, BorderLayout.CENTER);

        showCenter(CARD_HOME, "Dashboard", "Choose an admin task from the sidebar");

        // ===== Actions =====
        btnDashboard.addActionListener(e -> showCenter(CARD_HOME, "Dashboard", "Choose an admin task from the sidebar"));
        btnUsers.addActionListener(e -> showCenter(CARD_USERS, "Manage Users", "Create / update / delete users (users.txt)"));
        btnClasses.addActionListener(e -> showCenter(CARD_CLASSES, "Manage Classes", "Manage classes for modules (classes.txt)"));
        btnGrading.addActionListener(e -> showCenter(CARD_GRADING, "Manage Grading", "Define grading rules (grading.txt)"));
        btnAssign.addActionListener(e -> showCenter(CARD_ASSIGN, "Assign Lecturers", "Assign Lecturer to Academic Leader (leader_lecturer.txt)"));

        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private void showCenter(String cardName, String title, String subtitle) {
        pageTitle.setText(title);
        pageSub.setText(subtitle);
        centerLayout.show(centerCards, cardName);
        centerCards.revalidate();
        centerCards.repaint();
    }

    private JPanel buildHomePanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setOpaque(false);

        grid.add(statCard("Users", "Create / update / delete users (users.txt)", "Open Users",
                () -> showCenter(CARD_USERS, "Manage Users", "Create / update / delete users (users.txt)")));
        grid.add(statCard("Classes", "Manage classes for modules (classes.txt)", "Open Classes",
                () -> showCenter(CARD_CLASSES, "Manage Classes", "Manage classes for modules (classes.txt)")));
        grid.add(statCard("Grading", "Define grading rules (grading.txt)", "Open Grading",
                () -> showCenter(CARD_GRADING, "Manage Grading", "Define grading rules (grading.txt)")));
        grid.add(statCard("Assign Lecturers", "Assign Lecturer to Academic Leader (leader_lecturer.txt)", "Open Assign",
                () -> showCenter(CARD_ASSIGN, "Assign Lecturers", "Assign Lecturer to Academic Leader (leader_lecturer.txt)")));

        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel statCard(String title, String desc, String actionText, Runnable action) {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 10));

        JLabel t = new JLabel(title);
        t.setForeground(Theme.TEXT);
        t.setFont(UIUtils.font(16, Font.BOLD));

        JLabel d = new JLabel("<html><div style='width:260px;'>" + desc + "</div></html>");
        d.setForeground(Theme.MUTED);
        d.setFont(UIUtils.font(12, Font.PLAIN));

        JButton a = UIUtils.primaryButton(actionText);
        a.addActionListener(e -> action.run());

        card.add(t, BorderLayout.NORTH);
        card.add(d, BorderLayout.CENTER);
        card.add(a, BorderLayout.SOUTH);

        return card;
    }
}
