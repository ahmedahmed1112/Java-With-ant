package ui;

import model.User;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * LeaderDashboardFrame
 * -------------------
 * Academic Leader dashboard (styled like AdminDashboard).
 *
 * Changes (UI navigation only):
 * - Edit Profile opens inside the same window (right panel swaps)
 * - Manage Modules already opens inside the same window
 * - Sidebar has Dashboard button to return to dashboard view
 *
 * Logic kept:
 * - Logout -> LoginFrame
 * - X close -> LoginFrame
 * - Other screens still open in new windows (unchanged)
 */
public class LeaderDashboardFrame extends JFrame {

    private final User loggedInUser;

    // ✅ Right content area swaps views
    private CardLayout cardLayout;
    private JPanel rightContainer;

    private static final String VIEW_DASHBOARD = "VIEW_DASHBOARD";
    private static final String VIEW_MODULES = "VIEW_MODULES";
    private static final String VIEW_PROFILE = "VIEW_PROFILE";

    // Keep no-arg constructor for your current routeByRole() usage
    public LeaderDashboardFrame() {
        this(null);
    }

    // Optional constructor if later you want to pass the user
    public LeaderDashboardFrame(User user) {
        this.loggedInUser = user;

        setTitle("AFS Leader Dashboard");
        setSize(1020, 620);
        setLocationRelativeTo(null);

        // When user clicks X: go back to login (not exit)
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

        JLabel brand = preventBlueFocus(UIUtils.title("AFS Leader"));
        brand.setForeground(Theme.TEXT);
        brand.setFont(UIUtils.font(16, Font.BOLD));

        String leaderName = (loggedInUser == null || loggedInUser.getName() == null)
                ? "Leader"
                : loggedInUser.getName();

        JLabel role = new JLabel("Signed in as: " + leaderName);
        role.setForeground(Theme.MUTED);
        role.setFont(UIUtils.font(12, Font.PLAIN));

        JPanel brandBox = new JPanel(new GridLayout(2, 1, 0, 6));
        brandBox.setOpaque(false);
        brandBox.add(brand);
        brandBox.add(role);

        JPanel nav = new JPanel(new GridLayout(12, 1, 10, 10));
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(18, 0, 18, 0));

        JButton btnDashboard = UIUtils.ghostButton("🏠  Dashboard");
        JButton btnProfile = UIUtils.ghostButton("👤  Edit Profile");
        JButton btnModules = UIUtils.ghostButton("📚  Manage Modules");
        JButton btnAssign = UIUtils.ghostButton("🔗  Assign Lecturers");
        JButton btnReports = UIUtils.ghostButton("📊  Analyzed Reports");
        JButton btnLogout = UIUtils.dangerButton("Logout");

        nav.add(btnDashboard);
        nav.add(btnProfile);
        nav.add(btnModules);
        nav.add(btnAssign);
        nav.add(btnReports);

        sidebar.add(brandBox, BorderLayout.NORTH);
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(btnLogout, BorderLayout.SOUTH);

        root.add(sidebar, BorderLayout.WEST);

        // ===== Right Content Container (swappable) =====
        cardLayout = new CardLayout();
        rightContainer = new JPanel(cardLayout);
        rightContainer.setOpaque(false);
        rightContainer.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.add(rightContainer, BorderLayout.CENTER);

        // ===== Dashboard View =====
        JPanel dashboardView = buildDashboardView();
        rightContainer.add(dashboardView, VIEW_DASHBOARD);

        // ===== Modules View (embedded) =====
        ManageModulesFrame modulesFrame = new ManageModulesFrame(loggedInUser, true, this::showDashboard);
        rightContainer.add(modulesFrame.getMainPanel(), VIEW_MODULES);

        // ✅ NEW: Profile View (embedded) =====
        LeaderProfileFrame profileFrame = new LeaderProfileFrame(loggedInUser, true, this::showDashboard);
        rightContainer.add(profileFrame.getMainPanel(), VIEW_PROFILE);

        // Default view
        showDashboard();

        // ===== Actions =====
        btnDashboard.addActionListener(e -> showDashboard());

        // ✅ CHANGED: profile opens inside same window
        btnProfile.addActionListener(e -> showProfile());

        // modules already swap view
        btnModules.addActionListener(e -> showModules());

        // keep others as new windows (unchanged)
        btnAssign.addActionListener(e -> openFrameSafely("ui.AssignLecturersToModulesFrame"));
        btnReports.addActionListener(e -> openFrameSafely("ui.LeaderReportsFrame"));

        btnLogout.addActionListener(e -> goToLogin());
    }

    // ===== Dashboard view builder =====
    private JPanel buildDashboardView() {
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setOpaque(false);
        topbar.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel pageTitle = UIUtils.title("Dashboard");
        JLabel pageSub = UIUtils.muted("Choose a leader task from the sidebar");

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(pageTitle);
        titles.add(pageSub);

        topbar.add(titles, BorderLayout.WEST);
        content.add(topbar, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setOpaque(false);

        // ✅ Profile swaps view now
        grid.add(statCard(
                "Profile",
                "Update your own details (users.txt)",
                "Open Profile",
                this::showProfile
        ));

        grid.add(statCard(
                "Modules",
                "Create / update / delete modules (modules.txt)",
                "Open Modules",
                this::showModules
        ));

        grid.add(statCard(
                "Assign Lecturers",
                "Assign lecturers to modules (modules.txt)",
                "Open Assign",
                () -> openFrameSafely("ui.AssignLecturersToModulesFrame")
        ));

        grid.add(statCard(
                "Reports",
                "View analyzed reports (sample data)",
                "Open Reports",
                () -> openFrameSafely("ui.LeaderReportsFrame")
        ));

        content.add(grid, BorderLayout.CENTER);
        return content;
    }

    // ===== View switching =====
    private void showDashboard() {
        cardLayout.show(rightContainer, VIEW_DASHBOARD);
    }

    private void showModules() {
        cardLayout.show(rightContainer, VIEW_MODULES);
    }

    private void showProfile() {
        cardLayout.show(rightContainer, VIEW_PROFILE);
    }

    // Card helper
    private JPanel statCard(String title, String desc, String buttonText, Runnable action) {
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel h = new JLabel(title);
        h.setForeground(Theme.TEXT);
        h.setFont(UIUtils.font(15, Font.BOLD));

        JLabel d = new JLabel("<html><div style='width:320px;'>" + desc + "</div></html>");
        d.setForeground(Theme.MUTED);
        d.setFont(UIUtils.font(12, Font.PLAIN));

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 8));
        top.setOpaque(false);
        top.add(h);
        top.add(d);

        JButton btn = UIUtils.primaryButton(buttonText);
        btn.addActionListener(e -> action.run());

        card.add(top, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Opens a JFrame by class name safely.
     * Tries constructor(User) first, then no-arg.
     */
    private void openFrameSafely(String fullyQualifiedClassName) {
        try {
            Class<?> clazz = Class.forName(fullyQualifiedClassName);

            // try (User) constructor first
            try {
                Object frameObj = clazz.getConstructor(User.class).newInstance(loggedInUser);
                if (frameObj instanceof JFrame) {
                    ((JFrame) frameObj).setVisible(true);
                    return;
                }
            } catch (NoSuchMethodException ignored) {
                // fall back to no-arg
            }

            Object frameObj = clazz.getConstructor().newInstance();
            if (frameObj instanceof JFrame) {
                ((JFrame) frameObj).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Class exists but is not a JFrame:\n" + fullyQualifiedClassName,
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this,
                    "This screen is not implemented yet:\n" + fullyQualifiedClassName,
                    "Not ready", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open screen:\n" + fullyQualifiedClassName + "\n\nReason: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goToLogin() {
        new LoginFrame().setVisible(true);
        dispose();
    }

    private JLabel preventBlueFocus(JLabel l) {
        l.setFocusable(false);
        return l;
    }
}
