package ui;

import controller.LecturerController;
import controller.ValidationUtil;
import model.Lecturer;

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
    private final JRadioButton maleButton = new JRadioButton("Male");
    private final JRadioButton femaleButton = new JRadioButton("Female");
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
        usernameField.setEditable(false);
        moduleField.setText(lecturer.getAssignedModuleId());
        moduleField.setEditable(false);

        nameField.setText(lecturer.getName());
        passwordField.setText(lecturer.getPassword());
        emailField.setText(lecturer.getEmail());
        phoneField.setText(lecturer.getPhone());
        ageField.setText(String.valueOf(lecturer.getAge()));

        ButtonGroup group = new ButtonGroup();
        group.add(maleButton);
        group.add(femaleButton);
        maleButton.setForeground(Theme.TEXT);
        maleButton.setOpaque(false);
        femaleButton.setForeground(Theme.TEXT);
        femaleButton.setOpaque(false);
        if ("Male".equalsIgnoreCase(lecturer.getGender())) maleButton.setSelected(true);
        else if ("Female".equalsIgnoreCase(lecturer.getGender())) femaleButton.setSelected(true);

        int row = 0;
        addRow(formPanel, gbc, row++, "Username (Read Only)", usernameField);
        addRow(formPanel, gbc, row++, "Full Name", nameField);
        addRow(formPanel, gbc, row++, "Password", passwordField);
        addRow(formPanel, gbc, row++, "Gender", createGenderPanel());
        addRow(formPanel, gbc, row++, "Email", emailField);
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

    private JPanel createGenderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);
        panel.add(maleButton);
        panel.add(femaleButton);
        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        JLabel jLabel = UIUtils.muted(label);
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(jLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void saveProfile() {
        String name = nameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String ageText = ageField.getText().trim();
        String gender = maleButton.isSelected() ? "Male" : (femaleButton.isSelected() ? "Female" : "");

        if (!ValidationUtil.isNotEmpty(name) || !ValidationUtil.isValidName(name)) {
            JOptionPane.showMessageDialog(this, "Invalid name", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!ValidationUtil.isNotEmpty(password) || password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format", "Validation Error", JOptionPane.ERROR_MESSAGE);
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
}
