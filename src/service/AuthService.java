package service;

import java.util.List;
import model.User;
import model.Lecturer;
import model.Student;
import util.FileManager;
import util.Constants;

public class AuthService {

    private static final String USERS_FILE = "data/users.txt";

    public static User login(String username, String password) {
        if (username == null || password == null) return null;

        String u = username.trim();
        String p = password.trim();
        if (u.isEmpty() || p.isEmpty()) return null;

        // 1. Check users.txt (9-field or 5-field format)
        List<String> lines = FileManager.readAll(USERS_FILE);
        for (String line : lines) {
            if (line == null) continue;
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\|");

            // New schema: id|username|password|name|gender|email|phone|age|role
            if (parts.length >= 9) {
                String userId = parts[0].trim();
                String fileUsername = parts[1].trim();
                String filePassword = parts[2].trim();
                String name = parts[3].trim();
                String gender = parts[4].trim();
                String email = parts[5].trim();
                String phone = parts[6].trim();
                int age = 0;
                try { age = Integer.parseInt(parts[7].trim()); } catch (Exception ignored) {}
                String role = parts[8].trim();

                if (fileUsername.equalsIgnoreCase(u) && filePassword.equals(p)) {
                    return User.create(userId, fileUsername, filePassword, name, gender, email, phone, age, role);
                }
                continue;
            }

            // Old schema: id|name|username|password|role
            if (parts.length >= 5) {
                String userId = parts[0].trim();
                String name = parts[1].trim();
                String fileUsername = parts[2].trim();
                String filePassword = parts[3].trim();
                String role = parts[4].trim();

                if (fileUsername.equalsIgnoreCase(u) && filePassword.equals(p)) {
                    return User.create(userId, fileUsername, filePassword, name, "", "", "", 0, role);
                }
            }
        }

        // 2. Check lecturers.txt (Abdalla's format: username|password|name|gender|email|phone|age|moduleId|leaderId)
        List<String> lecLines = FileManager.readAll(Constants.LECTURERS_FILE);
        for (String line : lecLines) {
            if (line == null) continue;
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\|");
            if (parts.length < 9) continue;

            String fileUsername = parts[0].trim();
            String filePassword = parts[1].trim();

            if (fileUsername.equalsIgnoreCase(u) && filePassword.equals(p)) {
                String name = parts[2].trim();
                String gender = parts[3].trim();
                String email = parts[4].trim();
                String phone = parts[5].trim();
                int age = 0;
                try { age = Integer.parseInt(parts[6].trim()); } catch (Exception ignored) {}

                Lecturer lec = new Lecturer();
                lec.setUsername(fileUsername);
                lec.setPassword(filePassword);
                lec.setName(name);
                lec.setGender(gender);
                lec.setEmail(email);
                lec.setPhone(phone);
                lec.setAge(age);
                lec.setAssignedModuleId(parts[7].trim());
                lec.setAcademicLeaderId(parts[8].trim());
                lec.setRole("LECTURER");
                return lec;
            }
        }

        // 3. Check students.txt (Abdalla's format: username|password|name|gender|email|phone|age|studentId|intake|moduleId)
        List<String> stuLines = FileManager.readAll(Constants.STUDENTS_FILE);
        for (String line : stuLines) {
            if (line == null) continue;
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\|");
            if (parts.length < 10) continue;

            String fileUsername = parts[0].trim();
            String filePassword = parts[1].trim();

            if (fileUsername.equalsIgnoreCase(u) && filePassword.equals(p)) {
                String name = parts[2].trim();
                String gender = parts[3].trim();
                String email = parts[4].trim();
                String phone = parts[5].trim();
                int age = 0;
                try { age = Integer.parseInt(parts[6].trim()); } catch (Exception ignored) {}

                Student stu = new Student();
                stu.setUsername(fileUsername);
                stu.setPassword(filePassword);
                stu.setName(name);
                stu.setGender(gender);
                stu.setEmail(email);
                stu.setPhone(phone);
                stu.setAge(age);
                stu.setStudentId(parts[7].trim());
                stu.setIntake(parts[8].trim());
                stu.setModuleId(parts[9].trim());
                stu.setRole("STUDENT");
                return stu;
            }
        }

        return null;
    }
}
