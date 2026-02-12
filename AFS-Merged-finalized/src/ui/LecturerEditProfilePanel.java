package ui;

import controller.LecturerController;
import controller.ValidationUtil;
import model.Lecturer;
import util.Constants;
import util.FileManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LecturerEditProfilePanel extends JPanel {
    private final Lecturer lecturer;
    private final LecturerController controller;
    private final LecturerDashboardFrame parent;

    private final JTextField usernameField = UIUtils.modernTextField();
    private final JTextField nameField = UIUtils.modernTextField();
    private final JTextField passwordField = UIUtils.modernTextField();
    private final JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female"});
    private final JTextField emailField = UIUtils.modernTextField();
    private final JTextField phoneField = UIUtils.modernTextField();
    private final JTextField ageField = UIUtils.modernTextField();
    private final JTextField moduleField = UIUtils.modernTextField();

    public LecturerEditProfilePanel(Lecturer lecturer, LecturerController controller, LecturerDashboardFrame parent) {
        this.lecturer = lecturer;
        this.controller = controller;
        this.parent = parent;

        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = UIUtils.title("Edit Profile");
        JLabel subtitle = UIUtils.muted("Update your details below");
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        // Form
        JPanel card = UIUtils.cardPanel();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        usernameField.setText(lecturer.getUsername());
        usernameField.setEditable(true);
        moduleField.setText(lecturer.getAssignedModuleId());
        moduleField.setEditable(false);

        nameField.setText(lecturer.getName());
        passwordField.setText(lecturer.getPassword());
        emailField.setText(lecturer.getEmail());
        emailField.setEditable(false);
        phoneField.setText(lecturer.getPhone());
        ageField.setText(String.valueOf(lecturer.getAge()));

        genderBox.setBackground(Theme.CARD);
        genderBox.setForeground(Theme.TEXT);
        if ("Female".equalsIgnoreCase(lecturer.getGender())) {
            genderBox.setSelectedItem("Female");
        } else {
            genderBox.setSelectedItem("Male");
        }

        int row = 0;
        addRow(formPanel, gbc, row++, "Username", usernameField);
        addRow(formPanel, gbc, row++, "Full Name", nameField);
        addRow(formPanel, gbc, row++, "Password", passwordField);
        addRow(formPanel, gbc, row++, "Gender", genderBox);
        addRow(formPanel, gbc, row++, "Email (Read Only)", emailField);
        addRow(formPanel, gbc, row++, "Phone", phoneField);
        addRow(formPanel, gbc, row++, "Age", ageField);
        addRow(formPanel, gbc, row++, "Module (Read Only)", moduleField);

        JButton saveButton = UIUtils.primaryButton("Save");
        JButton cancelButton = UIUtils.ghostButton("Cancel");
        saveButton.addActionListener(e -> saveProfile());
        cancelButton.addActionListener(e -> parent.showDashboard());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        card.add(formPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);

        JScrollPane sp = new JScrollPane(card);
        UIUtils.styleScrollPane(sp);
        sp.setBorder(null);
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);

        add(header, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        JLabel jLabel = UIUtils.muted(label);
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(jLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void saveProfile() {
        String username = usernameField.getText().trim();
        String name = nameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = safe(lecturer.getEmail()); // email is read-only
        String phone = phoneField.getText().trim();
        String ageText = ageField.getText().trim();
        String gender = String.valueOf(genderBox.getSelectedItem()).trim();

        if (!ValidationUtil.isNotEmpty(username)) {
            JOptionPane.showMessageDialog(this, "Username is required", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (isUsernameTakenByAnother(username, lecturer.getUserId())) {
            JOptionPane.showMessageDialog(this, "Username already exists.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!ValidationUtil.isNotEmpty(name) || !ValidationUtil.isValidName(name)) {
            JOptionPane.showMessageDialog(this, "Invalid name", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!ValidationUtil.isNotEmpty(password) || password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Invalid phone number", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int age;
        try { age = Integer.parseInt(ageText); } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Age must be a number", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!ValidationUtil.isValidAge(age)) {
            JOptionPane.showMessageDialog(this, "Age must be between 18 and 100", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (gender.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select gender", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        lecturer.setUsername(username);
        lecturer.setName(name);
        lecturer.setPassword(password);
        lecturer.setEmail(email);
        lecturer.setPhone(phone);
        lecturer.setAge(age);
        lecturer.setGender(gender);

        controller.updateProfile(lecturer);
        JOptionPane.showMessageDialog(this, "Profile updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        parent.showDashboard();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private boolean isUsernameTakenByAnother(String username, String currentUserId) {
        String target = safe(username).toUpperCase();
        String currentId = safe(currentUserId).toUpperCase();
        for (String line : FileManager.readAll(Constants.USERS_FILE)) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length < 9) continue;
            String rowId = safe(p[0]).toUpperCase();
            String rowUsername = safe(p[1]).toUpperCase();
            if (rowUsername.equals(target) && !rowId.equals(currentId)) {
                return true;
            }
        }
        return false;
    }
}
