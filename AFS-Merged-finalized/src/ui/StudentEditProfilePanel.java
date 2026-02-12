package ui;

import model.Student;
import service.StudentService;
import util.Constants;
import util.FileManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StudentEditProfilePanel extends JPanel {
    private final Student student;
    private final StudentDashboardFrame parent;

    private final JTextField txtStudentId = UIUtils.modernTextField();
    private final JTextField txtUsername = UIUtils.modernTextField();
    private final JPasswordField txtPassword = UIUtils.modernPasswordField();
    private final JTextField txtName = UIUtils.modernTextField();
    private final JTextField txtEmail = UIUtils.modernTextField();
    private final JTextField txtPhone = UIUtils.modernTextField();
    private final JComboBox<String> cmbGender = new JComboBox<>(new String[]{"Male", "Female"});
    private final JSpinner spnAge = new JSpinner(new SpinnerNumberModel(0, 0, 120, 1));

    public StudentEditProfilePanel(Student student, StudentDashboardFrame parent) {
        this.student = student;
        this.parent = parent;

        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UIUtils.title("Edit Profile"), BorderLayout.NORTH);
        header.add(UIUtils.muted("Update your personal details"), BorderLayout.SOUTH);

        JPanel card = UIUtils.cardPanel();
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtStudentId.setText(student.getStudentId() == null ? "" : student.getStudentId());
        txtStudentId.setEditable(false);
        txtUsername.setText(student.getUsername() == null ? "" : student.getUsername());
        txtUsername.setEditable(true);
        txtPassword.setText(student.getPassword() == null ? "" : student.getPassword());
        txtName.setText(student.getName() == null ? "" : student.getName());
        txtEmail.setText(student.getEmail() == null ? "" : student.getEmail());
        txtEmail.setEditable(false);
        txtPhone.setText(student.getPhone() == null ? "" : student.getPhone());

        cmbGender.setBackground(Theme.CARD);
        cmbGender.setForeground(Theme.TEXT);
        if ("Female".equalsIgnoreCase(student.getGender())) {
            cmbGender.setSelectedIndex(1);
        } else {
            cmbGender.setSelectedIndex(0);
        }

        spnAge.setValue(student.getAge());

        int row = 0;
        addRow(card, gbc, row++, "Student ID (Read Only)", txtStudentId);
        addRow(card, gbc, row++, "Username", txtUsername);
        addRow(card, gbc, row++, "Password", txtPassword);
        addRow(card, gbc, row++, "Name", txtName);
        addRow(card, gbc, row++, "Gender", cmbGender);
        addRow(card, gbc, row++, "Email (Read Only)", txtEmail);
        addRow(card, gbc, row++, "Phone", txtPhone);
        addRow(card, gbc, row++, "Age", spnAge);

        JButton saveBtn = UIUtils.primaryButton("Save");
        JButton cancelBtn = UIUtils.ghostButton("Cancel");

        saveBtn.addActionListener(e -> saveProfile());
        cancelBtn.addActionListener(e -> parent.showDashboard());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setOpaque(false);
        formWrapper.add(card, BorderLayout.NORTH);
        formWrapper.add(btnPanel, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(formWrapper, BorderLayout.CENTER);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        JLabel jLabel = UIUtils.muted(label);
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(jLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void saveProfile() {
        String name = txtName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String phone = txtPhone.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();
        int age = (Integer) spnAge.getValue();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (isUsernameTakenByAnother(username, student.getUserId())) {
            JOptionPane.showMessageDialog(this, "Username already exists.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        student.setUsername(username);
        student.setName(name);
        student.setPassword(password);
        student.setPhone(phone);
        student.setGender(gender);
        student.setAge(age);

        StudentService.updateProfile(student);
        JOptionPane.showMessageDialog(this, "Profile updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        parent.showDashboard();
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
            if (rowUsername.equals(target) && !rowId.equals(currentId)) return true;
        }
        return false;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
