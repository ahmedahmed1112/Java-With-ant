package ui;

import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * LeaderDashboardFrame
 * -------------------
 * Academic Leader dashboard with embedded right-panel navigation.
 *
 * - Left sidebar navigation
 * - Right side uses CardLayout to switch between screens
 * - Logout + Window close returns to LoginFrame
 *
 * NOTE:
 * We embed screens by trying:
 * 1) constructor(User, boolean, Runnable) + getMainPanel()
 * 2) constructor(User) + JFrame content pane
 * 3) constructor() + JFrame content pane
 */
public class LeaderDashboardFrame extends JFrame {

    private final User loggedInUser;

    // Right-side swapping system
    private CardLayout cardLayout;
    private JPanel rightHost;
    private final Map<String, JPanel> cachedScreens = new HashMap<>();

    // Card keys
    private static final String CARD_DASHBOARD = "DASHBOARD";
    private static final String CARD_PROFILE   = "PROFILE";
    private static final String CARD_MODULES   = "MODULES";
    private static final String CARD_ASSIGN    = "ASSIGN";
    private static final String CARD_REPORTS   = "REPORTS";

    // Screen class names
    private static final String SCREEN_PROFILE = "ui.LeaderProfileFrame";
    private static final String SCREEN_MODULES = "ui.ManageModulesFrame";
    private static final String SCREEN_ASSIGN  = "ui.AssignLecturersFrame";     // ✅ NEW NAME
    private static final String SCREEN_REPORTS = "ui.LeaderReportsFrame";

    // Keep no-arg constructor for your routeByRole usage
    public LeaderDashboardFrame() {
        this(null);
    }

    public LeaderDashboardFrame(User user) {
        this.loggedInUser = user;

        setTitle("AFS Leader Dashboard");
        setSize(1020, 620);
        setLocationRelativeTo(null);

        // X close -> go login (not exit)
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

        // ---------- Sidebar ----------
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(260, 620));
        sidebar.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel brand = UIUtils.title("AFS Leader");
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

        // ✅ Add Dashboard button
        JButton btnDashboard = UIUtils.ghostButton("Dashboard");
        JButton btnProfile   = UIUtils.ghostButton("Edit Profile");
        JButton btnModules   = UIUtils.ghostButton("Manage Modules");
        JButton btnAssign    = UIUtils.ghostButton("Assign Lecturers");
        JButton btnReports   = UIUtils.ghostButton("Analyzed Reports");
        JButton btnLogout    = UIUtils.dangerButton("Logout");

        nav.add(btnDashboard);
        nav.add(btnProfile);
        nav.add(btnModules);
        nav.add(btnAssign);
        nav.add(btnReports);

        sidebar.add(brandBox, BorderLayout.NORTH);
        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(btnLogout, BorderLayout.SOUTH);

        root.add(sidebar, BorderLayout.WEST);

        // ---------- Right Host (CardLayout) ----------
        cardLayout = new CardLayout();
        rightHost = new JPanel(cardLayout);
        rightHost.setOpaque(false);
        rightHost.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.add(rightHost, BorderLayout.CENTER);

        // Add Dashboard card first
        JPanel dashboardPanel = buildDashboardPanel();
        rightHost.add(dashboardPanel, CARD_DASHBOARD);
        cachedScreens.put(CARD_DASHBOARD, dashboardPanel);

        // Default view
        cardLayout.show(rightHost, CARD_DASHBOARD);

        // ---------- Actions ----------
        btnDashboard.addActionListener(e -> showDashboard());

        btnProfile.addActionListener(e ->
                showOrBuildEmbedded(CARD_PROFILE, SCREEN_PROFILE)
        );

        btnModules.addActionListener(e ->
                showOrBuildEmbedded(CARD_MODULES, SCREEN_MODULES)
        );

        btnAssign.addActionListener(e ->
                showOrBuildEmbedded(CARD_ASSIGN, SCREEN_ASSIGN)
        );

        btnReports.addActionListener(e ->
                showOrBuildEmbedded(CARD_REPORTS, SCREEN_REPORTS)
        );

        btnLogout.addActionListener(e -> goToLogin());
    }

    // =========================
    // Dashboard UI (Right side)
    // =========================
    private JPanel buildDashboardPanel() {
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        // Topbar (NO Open Reports button anymore)
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setOpaque(false);
        topbar.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel pageTitle = UIUtils.title("Dashboard");
        JLabel pageSub   = UIUtils.muted("Choose a leader task from the sidebar");

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(pageTitle);
        titles.add(pageSub);

        topbar.add(titles, BorderLayout.WEST);
        content.add(topbar, BorderLayout.NORTH);

        // Cards grid
        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setOpaque(false);

        grid.add(statCard(
                "Profile",
                "Update your own details (users.txt)",
                "Open Profile",
                () -> showOrBuildEmbedded(CARD_PROFILE, SCREEN_PROFILE)
        ));

        grid.add(statCard(
                "Modules",
                "Create / update / delete modules (modules.txt)",
                "Open Modules",
                () -> showOrBuildEmbedded(CARD_MODULES, SCREEN_MODULES)
        ));

        grid.add(statCard(
                "Assign Lecturers",
                "Assign lecturers to modules (modules.txt)",
                "Open Assign",
                () -> showOrBuildEmbedded(CARD_ASSIGN, SCREEN_ASSIGN)
        ));

        grid.add(statCard(
                "Reports",
                "View analyzed reports (sample data)",
                "Open Reports",
                () -> showOrBuildEmbedded(CARD_REPORTS, SCREEN_REPORTS)
        ));

        content.add(grid, BorderLayout.CENTER);

        return content;
    }

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

    // ====================================
    // Card switching + embedded screen build
    // ====================================
    private void showDashboard() {
        cardLayout.show(rightHost, CARD_DASHBOARD);
    }

    private void showOrBuildEmbedded(String cardKey, String className) {
        if (cachedScreens.containsKey(cardKey)) {
            cardLayout.show(rightHost, cardKey);
            return;
        }

        JPanel embeddedPanel = buildEmbeddedPanel(className);
        if (embeddedPanel == null) return;

        rightHost.add(embeddedPanel, cardKey);
        cachedScreens.put(cardKey, embeddedPanel);
        cardLayout.show(rightHost, cardKey);
    }

    private JPanel buildEmbeddedPanel(String fullyQualifiedClassName) {
        try {
            Class<?> clazz = Class.forName(fullyQualifiedClassName);

            // Back action (returns to dashboard card)
            Runnable back = this::showDashboard;

            // 1) Try (User, boolean, Runnable) and getMainPanel()
            try {
                Object obj = clazz.getConstructor(User.class, boolean.class, Runnable.class)
                        .newInstance(loggedInUser, true, back);

                JPanel p = extractMainPanel(obj);
                if (p != null) return wrapIfNeeded(p);

            } catch (NoSuchMethodException ignored) {
                // fallback
            }

            // 2) Try (User)
            try {
                Object obj = clazz.getConstructor(User.class).newInstance(loggedInUser);
                JPanel p = extractFromFrameOrPanel(obj);
                if (p != null) return wrapIfNeeded(p);
            } catch (NoSuchMethodException ignored) {
                // fallback
            }

            // 3) Try ()
            Object obj = clazz.getConstructor().newInstance();
            JPanel p = extractFromFrameOrPanel(obj);
            if (p != null) return wrapIfNeeded(p);

            JOptionPane.showMessageDialog(this,
                    "Class exists but cannot be embedded:\n" + fullyQualifiedClassName,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return null;

        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this,
                    "This screen is not implemented yet:\n" + fullyQualifiedClassName,
                    "Not ready", JOptionPane.INFORMATION_MESSAGE);
            return null;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open screen:\n" + fullyQualifiedClassName + "\n\nReason: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // If the object has getMainPanel(), use it
    private JPanel extractMainPanel(Object obj) {
        try {
            Method m = obj.getClass().getMethod("getMainPanel");
            Object res = m.invoke(obj);
            if (res instanceof JPanel) return (JPanel) res;
        } catch (Exception ignored) {
        }
        return null;
    }

    // If it’s a JPanel return it, if it’s a JFrame return its content pane as JPanel
    private JPanel extractFromFrameOrPanel(Object obj) {
        if (obj instanceof JPanel) return (JPanel) obj;

        if (obj instanceof JFrame) {
            Container c = ((JFrame) obj).getContentPane();
            if (c instanceof JPanel) return (JPanel) c;

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.add(c, BorderLayout.CENTER);
            return wrapper;
        }

        return null;
    }

    // Some screens already include padding; we keep it safe without breaking layout
    private JPanel wrapIfNeeded(JPanel p) {
        // If it's already a big layout panel, return as-is
        return p;
    }

    // ====================================
    // Navigation / Logout
    // ====================================
    private void goToLogin() {
        new LoginFrame().setVisible(true);
        dispose();
    }
}
