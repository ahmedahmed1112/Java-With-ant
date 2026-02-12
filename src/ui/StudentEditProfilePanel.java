package ui;

import model.Student;
import service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StudentEditProfilePanel extends JPanel {
    private final Student student;
    private final StudentDashboardFrame parent;

    private final JTextField txtStudentId = UIUtils.modernTextField();
    private final JTextField txtIntake = UIUtils.modernTextField();
    private final JTextField txtName = UIUtils.modernTextField();
    private final JTextField txtEmail = UIUtils.modernTextField();
    private final JTextField txtPhone = UIUtils.modernTextField();

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
        txtIntake.setText(student.getIntake() == null ? "" : student.getIntake());
        txtIntake.setEditable(false);
        txtName.setText(student.getName() == null ? "" : student.getName());
        txtEmail.setText(student.getEmail() == null ? "" : student.getEmail());
        txtPhone.setText(student.getPhone() == null ? "" : student.getPhone());

        int row = 0;
        addRow(card, gbc, row++, "Student ID (Read Only)", txtStudentId);
        addRow(card, gbc, row++, "Intake (Read Only)", txtIntake);
        addRow(card, gbc, row++, "Name", txtName);
        addRow(card, gbc, row++, "Email", txtEmail);
        addRow(card, gbc, row++, "Phone", txtPhone);

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
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        student.setName(name);
        student.setEmail(email);
        student.setPhone(phone);

        StudentService.updateProfile(student);
        JOptionPane.showMessageDialog(this, "Profile updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        parent.showDashboard();
    }
}
