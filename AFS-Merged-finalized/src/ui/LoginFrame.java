package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import model.User;
import service.AuthService;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginFrame() {
        setTitle("AFS - Login");
        setSize(520, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        setContentPane(root);

        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.add(card, BorderLayout.CENTER);

        JLabel title = UIUtils.title("Welcome Back");
        JLabel sub = UIUtils.muted("Login with your username and password");

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(sub);
        card.add(titles, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        card.add(form, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0.01;
        gc.weighty = 0.1;
        gc.anchor = GridBagConstraints.NORTH;
        gc.insets = new Insets(2,0,2,0);

        txtUsername = UIUtils.modernTextField();
        txtPassword = UIUtils.modernPasswordField();

        addField(form, gc, 0, "Username", txtUsername);
        addField(form, gc, 2, "Password", txtPassword);

        JPanel actions = new JPanel(new GridLayout(1, 1, 10, 10));
        actions.setOpaque(false);

        btnLogin = UIUtils.primaryButton("Login");
        actions.add(btnLogin);

        card.add(actions, BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());
        txtUsername.addActionListener(e -> doLogin());
    }

    private void addField(JPanel panel, GridBagConstraints gc, int y, String labelText, JComponent field) {
        JLabel label = UIUtils.muted(labelText);
        gc.gridy = y;
        panel.add(label, gc);

        gc.gridy = y + 1;
        panel.add(field, gc);
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        User user = AuthService.login(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.");
            return;
        }

        routeByRole(user);
    }

    private void routeByRole(User user) {
        String role = user.getRole() == null ? "" : user.getRole().trim().toUpperCase();

        switch (role) {
            case "ADMIN":
                new AdminDashboard(user).setVisible(true);
                dispose();
                break;

            case "LEADER":
                new LeaderDashboardFrame(user).setVisible(true);
                dispose();
                break;

            case "LECTURER":
                new LecturerDashboardFrame(user).setVisible(true);
                dispose();
                break;

            case "STUDENT":
                new StudentDashboardFrame(user).setVisible(true);
                dispose();
                break;

            default:
                JOptionPane.showMessageDialog(this, "Unknown role: " + user.getRole());
                break;
        }
    }
}
