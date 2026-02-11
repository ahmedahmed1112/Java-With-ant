package ui;

import model.User;
import util.FileManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * LeaderProfileFrame
 * ------------------
 * Academic Leader feature: Edit personal / individual profile (users.txt)
 *
 * File format (keep 5 fields only):
 * userId|name|username|password|role
 *
 * UI Updates:
 * - Scrollable (can go up/down)
 * - Can be embedded inside LeaderDashboard (right-side swap)
 * - Show/Hide password button (styled like the rest using UIUtils)
 *
 * Logic kept:
 * - Updates line in users.txt using FileManager.updateById(...)
 */
public class LeaderProfileFrame extends JFrame {

    private static final String USERS_FILE = "data/users.txt";
    private static final int FIELD_HEIGHT = 45;

    private final User loggedInUser;

    private JTextField txtUserId;
    private JTextField txtRole;

    private JTextField txtName;
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    private char defaultEcho;

    // Embedded support
    private final boolean embedded;
    private final Runnable onBackToDashboard;
    private JPanel mainPanel;

    public LeaderProfileFrame() {
        this(null);
    }

    // Standalone constructor (old behavior)
    public LeaderProfileFrame(User user) {
        this(user, false, null);

        setTitle("Edit Profile (Leader)");
        setSize(820, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(mainPanel);
    }

    // Embedded constructor (new behavior)
    public LeaderProfileFrame(User user, boolean embedded, Runnable onBackToDashboard) {
        this.loggedInUser = user;
        this.embedded = embedded;
        this.onBackToDashboard = onBackToDashboard;

        buildUI();
        fillFromUser();

        if (loggedInUser == null || loggedInUser.getUserId() == null || loggedInUser.getUserId().trim().isEmpty()) {
            JOptionPane.showMessageDialog(embedded ? mainPanel : this,
                    "No logged-in Leader was provided.\nPlease login again and open Profile from the dashboard.",
                    "Missing User",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // For embedding into the dashboard right side
    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        this.mainPanel = root;

        // ===== Top bar =====
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);

        JLabel title = UIUtils.title("Profile");
        JLabel sub = UIUtils.muted("Update your own details (users.txt)");

        titles.add(title);
        titles.add(sub);

        top.add(titles, BorderLayout.WEST);
        root.add(top, BorderLayout.NORTH);

        // ===== Scrollable content =====
        JPanel scrollContent = new JPanel(new BorderLayout());
        scrollContent.setOpaque(false);

        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        scrollContent.add(card, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(scrollContent);
        UIUtils.styleScrollPane(sp);
        sp.setBorder(null);
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);

        root.add(sp, BorderLayout.CENTER);

        // ===== Form =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(6, 0, 10, 0));
        card.add(form, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 14, 0);

        // Read-only fields
        txtUserId = UIUtils.modernTextField();
        makeFieldTaller(txtUserId);
        txtUserId.setEditable(false);

        txtRole = UIUtils.modernTextField();
        makeFieldTaller(txtRole);
        txtRole.setEditable(false);

        // Editable fields
        txtName = UIUtils.modernTextField();
        makeFieldTaller(txtName);

        txtUsername = UIUtils.modernTextField();
        makeFieldTaller(txtUsername);

        txtPassword = UIUtils.modernPasswordField();
        makeFieldTaller(txtPassword);

        // capture the original echo char once
        defaultEcho = txtPassword.getEchoChar();

        // ===== Password row with Show/Hide button on the right =====
        JPanel passwordRow = new JPanel(new BorderLayout(10, 0));
        passwordRow.setOpaque(false);
        passwordRow.add(txtPassword, BorderLayout.CENTER);

        // ✅ Styled button matching theme (NO toggleShow variable)
        JButton btnShowHide = UIUtils.ghostButton("Show");
        btnShowHide.setPreferredSize(new Dimension(100, FIELD_HEIGHT));
        btnShowHide.setFont(UIUtils.font(12, Font.BOLD));
        btnShowHide.setFocusable(false);

        btnShowHide.addActionListener(e -> {
            boolean hiddenNow = txtPassword.getEchoChar() != (char) 0;

            if (hiddenNow) {
                txtPassword.setEchoChar((char) 0);
                btnShowHide.setText("Hide");
            } else {
                txtPassword.setEchoChar(defaultEcho);
                btnShowHide.setText("Show");
            }
        });

        passwordRow.add(btnShowHide, BorderLayout.EAST);

        gbc.gridy = 0; form.add(labeled("User ID (Read Only)", txtUserId), gbc);
        gbc.gridy = 1; form.add(labeled("Role (Read Only)", txtRole), gbc);
        gbc.gridy = 2; form.add(labeled("Full Name", txtName), gbc);
        gbc.gridy = 3; form.add(labeled("Username", txtUsername), gbc);
        gbc.gridy = 4; form.add(labeled("Password", passwordRow), gbc);

        // ===== Actions =====
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnSave = UIUtils.primaryButton("Save Changes");
        JButton btnClose = UIUtils.ghostButton(embedded ? "Back" : "Close");

        btnSave.addActionListener(e -> saveProfile());
        btnClose.addActionListener(e -> {
            if (embedded) {
                if (onBackToDashboard != null) onBackToDashboard.run();
            } else {
                dispose();
            }
        });

        // If user missing, disable save
        if (loggedInUser == null || loggedInUser.getUserId() == null || loggedInUser.getUserId().trim().isEmpty()) {
            btnSave.setEnabled(false);
        }

        actions.add(btnClose);
        actions.add(btnSave);

        card.add(actions, BorderLayout.SOUTH);
    }

    private void fillFromUser() {
        if (loggedInUser == null) return;

        txtUserId.setText(safe(loggedInUser.getUserId()));
        txtRole.setText(safe(loggedInUser.getRole()));

        txtName.setText(safe(loggedInUser.getName()));
        txtUsername.setText(safe(loggedInUser.getUsername()));
        txtPassword.setText(safe(loggedInUser.getPassword()));
    }

    private void saveProfile() {
        if (loggedInUser == null) return;

        String userId = safe(loggedInUser.getUserId()).trim();
        String role = safe(loggedInUser.getRole()).trim(); // keep as-is (read-only)

        String name = txtName.getText() == null ? "" : txtName.getText().trim();
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(embedded ? mainPanel : this,
                    "Name cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(embedded ? mainPanel : this,
                    "Username cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(embedded ? mainPanel : this,
                    "Password cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Keep exact 5 fields only
        String newLine = userId + "|" + name + "|" + username + "|" + password + "|" + role;

        // Update file by id (first column)
        FileManager.updateById(USERS_FILE, userId, newLine);

        // Update in-memory object too
        loggedInUser.setName(name);
        loggedInUser.setUsername(username);
        loggedInUser.setPassword(password);

        JOptionPane.showMessageDialog(embedded ? mainPanel : this,
                "Profile updated successfully.",
                "Saved",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel l = new JLabel(label);
        l.setForeground(Theme.MUTED);
        l.setFont(UIUtils.font(12, Font.PLAIN));

        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void makeFieldTaller(JComponent field) {
        Dimension d = field.getPreferredSize();
        field.setPreferredSize(new Dimension(d.width, FIELD_HEIGHT));
        field.setMinimumSize(new Dimension(10, FIELD_HEIGHT));
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
