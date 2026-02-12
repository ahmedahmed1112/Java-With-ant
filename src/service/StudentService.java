package service;

import model.Student;
import util.Constants;
import util.FileManager;

import java.util.ArrayList;
import java.util.List;

public class StudentService {

    public static Student getStudentProfile(String username) {
        List<String> lines = FileManager.readAll(Constants.STUDENTS_FILE);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length < 10) continue;
            if (parts[0].trim().equalsIgnoreCase(username)) {
                Student s = new Student();
                s.setUsername(parts[0].trim());
                s.setPassword(parts[1].trim());
                s.setName(parts[2].trim());
                s.setGender(parts[3].trim());
                s.setEmail(parts[4].trim());
                s.setPhone(parts[5].trim());
                try { s.setAge(Integer.parseInt(parts[6].trim())); } catch (Exception ignored) {}
                s.setStudentId(parts[7].trim());
                s.setIntake(parts[8].trim());
                s.setModuleId(parts[9].trim());
                s.setRole("STUDENT");
                return s;
            }
        }
        return null;
    }

    public static void updateProfile(Student student) {
        List<String> lines = FileManager.readAll(Constants.STUDENTS_FILE);
        List<String> out = new ArrayList<>();
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length >= 10 && parts[0].trim().equalsIgnoreCase(student.getUsername())) {
                out.add(student.toFileString());
            } else {
                out.add(line);
            }
        }
        FileManager.writeAll(Constants.STUDENTS_FILE, out);
    }

    public static List<String[]> getAvailableClasses(String studentModuleId) {
        List<String[]> classes = new ArrayList<>();
        List<String> lines = FileManager.readAll(Constants.CLASSES_FILE);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length >= 3) {
                // Show classes for the student's module, or all if no filter
                if (studentModuleId == null || studentModuleId.isEmpty()
                        || parts[2].trim().equalsIgnoreCase(studentModuleId)) {
                    classes.add(new String[]{parts[0].trim(), parts[1].trim(), parts[2].trim()});
                }
            }
        }
        return classes;
    }

    public static void registerForClass(String studentId, String classId) {
        FileManager.append(Constants.STUDENT_CLASSES_FILE, studentId + "|" + classId);
    }

    public static List<String[]> getRegisteredClasses(String studentId) {
        List<String[]> registered = new ArrayList<>();
        List<String> lines = FileManager.readAll(Constants.STUDENT_CLASSES_FILE);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length >= 2 && parts[0].trim().equalsIgnoreCase(studentId)) {
                registered.add(new String[]{parts[0].trim(), parts[1].trim()});
            }
        }
        return registered;
    }

    public static boolean isAlreadyRegistered(String studentId, String classId) {
        for (String[] reg : getRegisteredClasses(studentId)) {
            if (reg[1].equalsIgnoreCase(classId)) return true;
        }
        return false;
    }

    public static List<String[]> getMyResults(String studentId) {
        List<String[]> results = new ArrayList<>();
        List<String> gradeLines = FileManager.readAll(Constants.GRADES_FILE);
        List<String> assessmentLines = FileManager.readAll(Constants.ASSESSMENTS_FILE);
        List<String> feedbackLines = FileManager.readAll(Constants.FEEDBACK_FILE);

        for (String gLine : gradeLines) {
            if (gLine == null || gLine.trim().isEmpty()) continue;
            String[] gParts = gLine.split("\\|");
            if (gParts.length < 7) continue;
            if (!gParts[2].trim().equalsIgnoreCase(studentId)) continue;

            String assessmentId = gParts[1].trim();
            String marks = gParts[3].trim();
            String grade = gParts[4].trim();

            // Find assessment name and module
            String assessmentName = assessmentId;
            String moduleId = "";
            for (String aLine : assessmentLines) {
                if (aLine == null || aLine.trim().isEmpty()) continue;
                String[] aParts = aLine.split("\\|");
                if (aParts.length >= 7 && aParts[0].trim().equals(assessmentId)) {
                    assessmentName = aParts[2].trim();
                    moduleId = aParts[1].trim();
                    break;
                }
            }

            // Find feedback
            String feedback = "";
            for (String fLine : feedbackLines) {
                if (fLine == null || fLine.trim().isEmpty()) continue;
                String[] fParts = fLine.split("\\|");
                if (fParts.length >= 6
                        && fParts[1].trim().equals(assessmentId)
                        && fParts[2].trim().equalsIgnoreCase(studentId)) {
                    feedback = fParts[4].trim();
                    break;
                }
            }

            results.add(new String[]{moduleId, assessmentName, marks, grade, feedback});
        }
        return results;
    }

    public static List<String[]> getModulesForComment(String studentId) {
        List<String[]> modules = new ArrayList<>();
        List<String> lines = FileManager.readAll(Constants.MODULES_FILE);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length >= 2) {
                modules.add(new String[]{parts[0].trim(), parts[1].trim()});
            }
        }
        return modules;
    }

    public static void submitComment(String studentId, String moduleId, String comment) {
        String commentId = "CMT" + System.currentTimeMillis();
        String date = java.time.LocalDate.now().toString();
        FileManager.append(Constants.COMMENTS_FILE,
                commentId + "|" + studentId + "|" + moduleId + "|" + comment + "|" + date);
    }

    public static List<String[]> getComments(String studentId) {
        List<String[]> comments = new ArrayList<>();
        List<String> lines = FileManager.readAll(Constants.COMMENTS_FILE);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length >= 5 && parts[1].trim().equalsIgnoreCase(studentId)) {
                comments.add(new String[]{parts[0].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim()});
            }
        }
        return comments;
    }
}
