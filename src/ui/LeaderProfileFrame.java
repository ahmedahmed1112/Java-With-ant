package ui;

import model.User;
import util.FileManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LeaderProfileFrame extends JFrame {

    private static final String USERS_FILE = "data/users.txt";
    private static final int FIELD_HEIGHT = 45;

    private final User loggedInUser;

    private JTextField txtUserId;
    private JTextField txtRole;
    private JTextField txtName;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtGender;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAge;

    private char defaultEcho;

    private final boolean embedded;
    private final Runnable onBackToDashboard;
    private JPanel mainPanel;

    public LeaderProfileFrame() {
        this(null);
    }

    public LeaderProfileFrame(User user) {
        this(user, false, null);
        setTitle("Edit Profile (Leader)");
        setSize(820, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(mainPanel);
    }

    public LeaderProfileFrame(User user, boolean embedded, Runnable onBackToDashboard) {
        this.loggedInUser = user;
        this.embedded = embedded;
        this.onBackToDashboard = onBackToDashboard;

        buildUI();
        fillFromUser();

        if (loggedInUser == null || loggedInUser.getUserId() == null || loggedInUser.getUserId().trim().isEmpty()) {
            JOptionPane.showMessageDialog(embedded ? mainPanel : this,
                    "No logged-in Leader was provided.\nPlease login again and open Profile from the dashboard.",
                    "Missing User", JOptionPane.WARNING_MESSAGE);
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        this.mainPanel = root;

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(UIUtils.title("Profile"));
        titles.add(UIUtils.muted("Update your own details (users.txt)"));

        top.add(titles, BorderLayout.WEST);
        root.add(top, BorderLayout.NORTH);

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

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(6, 0, 10, 0));
        card.add(form, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 14, 0);

        txtUserId = UIUtils.modernTextField();
        makeFieldTaller(txtUserId);
        txtUserId.setEditable(false);

        txtRole = UIUtils.modernTextField();
        makeFieldTaller(txtRole);
        txtRole.setEditable(false);

        txtName = UIUtils.modernTextField();
        makeFieldTaller(txtName);

        txtUsername = UIUtils.modernTextField();
        makeFieldTaller(txtUsername);

        txtPassword = UIUtils.modernPasswordField();
        makeFieldTaller(txtPassword);

        txtGender = UIUtils.modernTextField();
        makeFieldTaller(txtGender);

        txtEmail = UIUtils.modernTextField();
        makeFieldTaller(txtEmail);

        txtPhone = UIUtils.modernTextField();
        makeFieldTaller(txtPhone);

        txtAge = UIUtils.modernTextField();
        makeFieldTaller(txtAge);

        defaultEcho = txtPassword.getEchoChar();

        JPanel passwordRow = new JPanel(new BorderLayout(10, 0));
        passwordRow.setOpaque(false);
        passwordRow.add(txtPassword, BorderLayout.CENTER);

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
        gbc.gridy = 5; form.add(labeled("Gender", txtGender), gbc);
        gbc.gridy = 6; form.add(labeled("Email", txtEmail), gbc);
        gbc.gridy = 7; form.add(labeled("Phone", txtPhone), gbc);
        gbc.gridy = 8; form.add(labeled("Age", txtAge), gbc);

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
        txtGender.setText(safe(loggedInUser.getGender()));
        txtEmail.setText(safe(loggedInUser.getEmail()));
        txtPhone.setText(safe(loggedInUser.getPhone()));
        txtAge.setText(loggedInUser.getAge() > 0 ? String.valueOf(loggedInUser.getAge()) : "");
    }

    private void saveProfile() {
        if (loggedInUser == null) return;

        String userId = safe(loggedInUser.getUserId()).trim();
        String role = safe(loggedInUser.getRole()).trim();

        String name = txtName.getText() == null ? "" : txtName.getText().trim();
        String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String gender = txtGender.getText() == null ? "" : txtGender.getText().trim();
        String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();
        String phone = txtPhone.getText() == null ? "" : txtPhone.getText().trim();
        String ageText = txtAge.getText() == null ? "" : txtAge.getText().trim();

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

        int age = 0;
        if (!ageText.isEmpty()) {
            try { age = Integer.parseInt(ageText); } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(embedded ? mainPanel : this,
                        "Age must be a number.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Save full 9-field format: userId|username|password|name|gender|email|phone|age|role
        String newLine = userId + "|" + username + "|" + password + "|" + name + "|"
                + gender + "|" + email + "|" + phone + "|" + age + "|" + role;

        FileManager.updateById(USERS_FILE, userId, newLine);

        loggedInUser.setName(name);
        loggedInUser.setUsername(username);
        loggedInUser.setPassword(password);
        loggedInUser.setGender(gender);
        loggedInUser.setEmail(email);
        loggedInUser.setPhone(phone);
        loggedInUser.setAge(age);

        JOptionPane.showMessageDialog(embedded ? mainPanel : this,
                "Profile updated successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
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
