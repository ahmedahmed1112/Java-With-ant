package controller;

import model.Assessment;
import model.Feedback;
import model.Grade;
import model.Lecturer;
import model.Student;
import util.Constants;

import util.FileManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FileHandler {
    public void saveLecturer(Lecturer lecturer) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(
                new FileWriter(Constants.LECTURERS_FILE, true)))) {
            writer.println(lecturer.toFileString());
        } catch (IOException e) {
            throw new RuntimeException("Error saving lecturer", e);
        }
    }

    public List<Lecturer> loadLecturers() {
        List<Lecturer> lecturers = new ArrayList<>();
        File file = new File(Constants.LECTURERS_FILE);
        if (!file.exists()) {
            return lecturers;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("\\|");
                if (data.length < 9) {
                    continue;
                }
                Lecturer lecturer = new Lecturer();
                lecturer.setUsername(data[0]);
                lecturer.setPassword(data[1]);
                lecturer.setName(data[2]);
                lecturer.setGender(data[3]);
                lecturer.setEmail(data[4]);
                lecturer.setPhone(data[5]);
                lecturer.setAge(Integer.parseInt(data[6]));
                lecturer.setAssignedModuleId(data[7]);
                lecturer.setAcademicLeaderId(data[8]);
                lecturers.add(lecturer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading lecturers", e);
        }
        return lecturers;
    }

    public void updateLecturer(Lecturer updatedLecturer) {
        if (updatedLecturer == null) return;

        String userId = safe(updatedLecturer.getUserId());
        String username = safe(updatedLecturer.getUsername());
        if (userId.isEmpty() && username.isEmpty()) return;
        String previousUsername = "";
        String previousPassword = "";

        List<String> lines = FileManager.readAll(Constants.USERS_FILE);
        List<String> out = new ArrayList<>();
        boolean updatedUser = false;

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|");

            if (parts.length >= 9) {
                boolean byId = !userId.isEmpty() && safe(parts[0]).equalsIgnoreCase(userId);
                boolean byUsername = !username.isEmpty()
                        && safe(parts[1]).equalsIgnoreCase(username)
                        && "LECTURER".equalsIgnoreCase(safe(parts[8]));

                if (byId || byUsername) {
                    previousUsername = safe(parts[1]);
                    previousPassword = safe(parts[2]);
                    String uid = userId.isEmpty() ? safe(parts[0]) : userId;
                    String uname = username.isEmpty() ? safe(parts[1]) : username;
                    String pwd = safe(updatedLecturer.getPassword()).isEmpty() ? safe(parts[2]) : safe(updatedLecturer.getPassword());

                    out.add(uid + "|" + uname + "|" + pwd + "|" +
                            safe(updatedLecturer.getName()) + "|" +
                            safe(updatedLecturer.getGender()) + "|" +
                            safe(updatedLecturer.getEmail()) + "|" +
                            safe(updatedLecturer.getPhone()) + "|" +
                            updatedLecturer.getAge() + "|LECTURER");
                    updatedUser = true;
                } else {
                    out.add(line);
                }
            } else {
                out.add(line);
            }
        }

        if (!updatedUser && !username.isEmpty()) {
            String uid = userId.isEmpty() ? username : userId;
            previousUsername = username;
            previousPassword = safe(updatedLecturer.getPassword());
            out.add(uid + "|" + username + "|" + safe(updatedLecturer.getPassword()) + "|" +
                    safe(updatedLecturer.getName()) + "|" + safe(updatedLecturer.getGender()) + "|" +
                    safe(updatedLecturer.getEmail()) + "|" + safe(updatedLecturer.getPhone()) + "|" +
                    updatedLecturer.getAge() + "|LECTURER");
        }
        FileManager.writeAll(Constants.USERS_FILE, out);


        List<String> lecLines = FileManager.readAll(Constants.LECTURERS_FILE);
        List<String> lecOut = new ArrayList<>();
        boolean updatedLecturerRow = false;
        String finalPassword = safe(updatedLecturer.getPassword()).isEmpty() ? previousPassword : safe(updatedLecturer.getPassword());

        for (String line : lecLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|");
            boolean byCurrentUsername = !username.isEmpty() && parts.length >= 9 && safe(parts[0]).equalsIgnoreCase(username);
            boolean byPreviousUsername = !previousUsername.isEmpty() && parts.length >= 9 && safe(parts[0]).equalsIgnoreCase(previousUsername);
            if (byCurrentUsername || byPreviousUsername) {
                lecOut.add(
                        username + "|" +
                        finalPassword + "|" +
                        safe(updatedLecturer.getName()) + "|" +
                        safe(updatedLecturer.getGender()) + "|" +
                        safe(updatedLecturer.getEmail()) + "|" +
                        safe(updatedLecturer.getPhone()) + "|" +
                        updatedLecturer.getAge() + "|" +
                        safe(updatedLecturer.getAssignedModuleId()) + "|" +
                        safe(updatedLecturer.getAcademicLeaderId())
                );
                updatedLecturerRow = true;
            } else {
                lecOut.add(line);
            }
        }

        if (!updatedLecturerRow && !username.isEmpty()) {
            lecOut.add(
                    username + "|" +
                    finalPassword + "|" +
                    safe(updatedLecturer.getName()) + "|" +
                    safe(updatedLecturer.getGender()) + "|" +
                    safe(updatedLecturer.getEmail()) + "|" +
                    safe(updatedLecturer.getPhone()) + "|" +
                    updatedLecturer.getAge() + "|" +
                    safe(updatedLecturer.getAssignedModuleId()) + "|" +
                    safe(updatedLecturer.getAcademicLeaderId())
            );
        }
        FileManager.writeAll(Constants.LECTURERS_FILE, lecOut);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    public void saveAssessment(Assessment assessment) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(
                new FileWriter(Constants.ASSESSMENTS_FILE, true)))) {
            writer.println(assessment.toFileString());
        } catch (IOException e) {
            throw new RuntimeException("Error saving assessment", e);
        }
    }

    public List<Assessment> loadAssessments() {
        List<Assessment> assessments = new ArrayList<>();
        File file = new File(Constants.ASSESSMENTS_FILE);
        if (!file.exists()) {
            return assessments;
        }
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split("\\|");
                if (data.length < 7) {
                    continue;
                }
                Assessment assessment = new Assessment(
                        data[0], data[1], data[2], data[3],
                        Double.parseDouble(data[4]),
                        Double.parseDouble(data[5]),
                        data[6]
                );
                assessments.add(assessment);
            }
        } catch (FileNotFoundException e) {
            return assessments;
        }
        return assessments;
    }

    public List<Assessment> loadAssessmentsByModule(String moduleId) {
        List<Assessment> results = new ArrayList<>();
        for (Assessment assessment : loadAssessments()) {
            if (assessment.getModuleId().equals(moduleId)) {
                results.add(assessment);
            }
        }
        return results;
    }

    public void updateAssessment(Assessment updatedAssessment) {
        List<Assessment> assessments = loadAssessments();
        for (int i = 0; i < assessments.size(); i++) {
            if (assessments.get(i).getAssessmentId().equals(updatedAssessment.getAssessmentId())) {
                assessments.set(i, updatedAssessment);
                break;
            }
        }
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(
                new FileWriter(Constants.ASSESSMENTS_FILE)))) {
            for (Assessment assessment : assessments) {
                writer.println(assessment.toFileString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error updating assessments", e);
        }
    }

    public void deleteAssessment(String assessmentId) {
        List<Assessment> assessments = loadAssessments();
        assessments.removeIf(a -> a.getAssessmentId().equals(assessmentId));
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(
                new FileWriter(Constants.ASSESSMENTS_FILE)))) {
            for (Assessment assessment : assessments) {
                writer.println(assessment.toFileString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error deleting assessment", e);
        }
    }

    public void saveFeedback(Feedback feedback) {
        saveOrUpdateFeedback(feedback);
    }

    public void saveOrUpdateFeedback(Feedback feedback) {
        if (feedback == null) return;

        String assessmentId = safe(feedback.getAssessmentId());
        String studentId = safe(feedback.getStudentId());
        if (assessmentId.isEmpty() || studentId.isEmpty()) return;

        List<String> lines = FileManager.readAll(Constants.FEEDBACK_FILE);
        List<String> out = new ArrayList<>();
        boolean updated = false;

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length < 6) {
                out.add(line);
                continue;
            }

            boolean sameAssessment = safe(p[1]).equalsIgnoreCase(assessmentId);
            boolean sameStudent = safe(p[2]).equalsIgnoreCase(studentId);

            if (sameAssessment && sameStudent) {
                if (!updated) {
                    String feedbackId = safe(feedback.getFeedbackId()).isEmpty() ? safe(p[0]) : safe(feedback.getFeedbackId());
                    String lecturerId = safe(feedback.getLecturerId()).isEmpty() ? safe(p[3]) : safe(feedback.getLecturerId());
                    String feedbackText = safe(feedback.getFeedbackText()).isEmpty() ? safe(p[4]) : safe(feedback.getFeedbackText());
                    String dateProvided = safe(feedback.getDateProvided()).isEmpty() ? safe(p[5]) : safe(feedback.getDateProvided());

                    out.add(feedbackId + "|" + assessmentId + "|" + studentId + "|" +
                            lecturerId + "|" + feedbackText + "|" + dateProvided);
                    updated = true;
                }

                continue;
            }

            out.add(line);
        }

        if (!updated) {
            out.add(feedback.toFileString());
        }

        FileManager.writeAll(Constants.FEEDBACK_FILE, out);
    }

    public List<Feedback> loadFeedback() {
        List<Feedback> feedbackList = new ArrayList<>();
        File file = new File(Constants.FEEDBACK_FILE);
        if (!file.exists()) {
            return feedbackList;
        }
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split("\\|");
                if (data.length < 6) {
                    continue;
                }
                Feedback feedback = new Feedback(
                        data[0], data[1], data[2], data[3], data[4], data[5]);
                feedbackList.add(feedback);
            }
        } catch (FileNotFoundException e) {
            return feedbackList;
        }
        return feedbackList;
    }

    public List<Student> loadStudents() {
        List<Student> students = new ArrayList<>();
        Map<String, String> userIdToName = new HashMap<>();


        File usersFile = new File(Constants.USERS_FILE);
        if (usersFile.exists()) {
            try (Scanner sc = new Scanner(usersFile)) {
                while (sc.hasNextLine()) {
                    String uLine = sc.nextLine().trim();
                    if (uLine.isEmpty()) continue;
                    String[] up = uLine.split("\\|");
                    if (up.length >= 9) {
                        userIdToName.put(up[0].trim(), up[3].trim());
                    }
                }
            } catch (FileNotFoundException ignored) {}
        }

        // Supported formats:
        // 1) username|password|name|gender|email|phone|age|studentId|moduleId
        // 2) legacy: username|password|name|gender|email|phone|age|studentId|extra|moduleId
        // 3) studentId|name|moduleId
        // 4) studentId|userId (module derived from student_classes/classes)
        // 5) legacy: studentId|userId|extra (module derived from student_classes/classes)
        File file = new File(Constants.STUDENTS_FILE);
        if (!file.exists()) return students;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] data = line.split("\\|");

                if (data.length >= 10) {
                    String studentId = data[7].trim();
                    String name = data[2].trim();
                    String moduleId = data[9].trim();
                    students.add(new Student(studentId, name, moduleId));
                    continue;
                }

                if (data.length >= 9) {
                    String studentId = data[7].trim();
                    String name = data[2].trim();
                    String moduleId = data[8].trim();
                    students.add(new Student(studentId, name, moduleId));
                    continue;
                }

                if (data.length >= 3) {
                    String studentId = data[0].trim();
                    String second = data[1].trim();
                    String third = data[2].trim();

                    // studentId|userId|extra (legacy)
                    if (userIdToName.containsKey(second)) {
                        String name = userIdToName.get(second);
                        String moduleId = findModuleForStudent(studentId);
                        Student student = new Student(studentId, name, moduleId);
                        students.add(student);
                        continue;
                    }

                    // studentId|name|moduleId
                    Student student = new Student(studentId, second, third);
                    students.add(student);
                    continue;
                }

                if (data.length >= 2) {
                    String studentId = data[0].trim();
                    String second = data[1].trim();

                    // studentId|userId (current compact)
                    if (userIdToName.containsKey(second)) {
                        String name = userIdToName.get(second);
                        String moduleId = findModuleForStudent(studentId);
                        students.add(new Student(studentId, name, moduleId));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            return students;
        }
        return students;
    }

    private String findModuleForStudent(String studentId) {
        // Read student_classes.txt to find classIds for this student
        File scFile = new File(Constants.STUDENT_CLASSES_FILE);
        if (!scFile.exists()) return "";
        try (Scanner sc = new Scanner(scFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 2 && parts[0].trim().equalsIgnoreCase(studentId)) {
                    String classId = parts[1].trim();
                    // Find moduleId from classes.txt
                    File classFile = new File(Constants.CLASSES_FILE);
                    if (classFile.exists()) {
                        try (Scanner cs = new Scanner(classFile)) {
                            while (cs.hasNextLine()) {
                                String cLine = cs.nextLine().trim();
                                if (cLine.isEmpty()) continue;
                                String[] cp = cLine.split("\\|");
                                if (cp.length >= 3 && cp[0].trim().equalsIgnoreCase(classId)) {
                                    return cp[2].trim();
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException ignored) {}
        return "";
    }

    public void saveGrade(Grade grade) {
        saveOrUpdateGrade(grade);
    }

    public void saveOrUpdateGrade(Grade grade) {
        if (grade == null) return;

        String assessmentId = safe(grade.getAssessmentId());
        String studentId = safe(grade.getStudentId());
        if (assessmentId.isEmpty() || studentId.isEmpty()) return;

        List<String> lines = FileManager.readAll(Constants.GRADES_FILE);
        List<String> out = new ArrayList<>();
        boolean updated = false;

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length < 7) {
                out.add(line);
                continue;
            }

            boolean sameAssessment = safe(p[1]).equalsIgnoreCase(assessmentId);
            boolean sameStudent = safe(p[2]).equalsIgnoreCase(studentId);
            if (sameAssessment && sameStudent) {
                if (!updated) {
                    String gradeId = safe(grade.getGradeId()).isEmpty() ? safe(p[0]) : safe(grade.getGradeId());
                    String lecturerId = safe(grade.getLecturerId()).isEmpty() ? safe(p[5]) : safe(grade.getLecturerId());
                    String date = safe(grade.getDateEntered()).isEmpty() ? safe(p[6]) : safe(grade.getDateEntered());
                    out.add(gradeId + "|" + assessmentId + "|" + studentId + "|" +
                            grade.getMarks() + "|" + safe(grade.getGrade()) + "|" + lecturerId + "|" + date);
                    updated = true;
                }
                // Drop duplicates for same assessment+student.
                continue;
            }

            out.add(line);
        }

        if (!updated) {
            out.add(grade.toFileString());
        }

        FileManager.writeAll(Constants.GRADES_FILE, out);
    }

    public List<Grade> loadGrades() {
        List<Grade> grades = new ArrayList<>();
        File file = new File(Constants.GRADES_FILE);
        if (!file.exists()) {
            return grades;
        }
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split("\\|");
                if (data.length < 7) {
                    continue;
                }
                Grade grade = new Grade(
                        data[0], data[1], data[2],
                        Double.parseDouble(data[3]), data[4], data[5], data[6]);
                grades.add(grade);
            }
        } catch (FileNotFoundException e) {
            return grades;
        }
        return grades;
    }

    public Map<String, int[]> loadGradingSystem() {
        Map<String, int[]> grading = new LinkedHashMap<>();
        File file = new File(Constants.GRADING_FILE);

        if (!file.exists()) {
            // Backward-compatibility fallback.
            file = new File(Constants.GRADING_SYSTEM_FILE);
        }

        if (!file.exists()) return grading;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line == null || line.trim().isEmpty()) continue;

                String[] data = line.split("\\|");
                if (data.length < 3) continue;

                try {
                    grading.put(data[0].trim(), new int[]{
                            Integer.parseInt(data[1].trim()),
                            Integer.parseInt(data[2].trim())
                    });
                } catch (Exception ignored) {
                    // Skip malformed grading rows.
                }
            }
        } catch (FileNotFoundException e) {
            return grading;
        }
        return grading;
    }

    public String calculateGrade(double marks, double total) {
        if (total <= 0) return "";
        double percentage = (marks / total) * 100.0;
        if (percentage < 0) percentage = 0;
        if (percentage > 100) percentage = 100;

        for (Map.Entry<String, int[]> entry : loadGradingSystem().entrySet()) {
            int min = entry.getValue()[0];
            int max = entry.getValue()[1];
            // Treat integer boundaries as full bands so decimal percentages don't fall into gaps.
            if (percentage >= min && percentage < (max + 1.0)) {
                return entry.getKey();
            }
        }
        return "";
    }
}
