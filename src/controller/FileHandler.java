package controller;

import model.Assessment;
import model.Feedback;
import model.Grade;
import model.Lecturer;
import model.Student;
import util.Constants;

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
        List<Lecturer> allLecturers = loadLecturers();
        for (int i = 0; i < allLecturers.size(); i++) {
            if (allLecturers.get(i).getUsername().equals(updatedLecturer.getUsername())) {
                allLecturers.set(i, updatedLecturer);
                break;
            }
        }
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(
                new FileWriter(Constants.LECTURERS_FILE)))) {
            for (Lecturer lecturer : allLecturers) {
                writer.println(lecturer.toFileString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error updating lecturers", e);
        }
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
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(
                new FileWriter(Constants.FEEDBACK_FILE, true)))) {
            writer.println(feedback.toFileString());
        } catch (IOException e) {
            throw new RuntimeException("Error saving feedback", e);
        }
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
        File file = new File(Constants.STUDENTS_FILE);
        if (!file.exists()) {
            return students;
        }
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;
                String[] data = line.split("\\|");
                // Support both 3-field (studentId|name|moduleId) and 10-field format
                if (data.length >= 10) {
                    // Full format: username|password|name|gender|email|phone|age|studentId|intake|moduleId
                    Student student = new Student(data[7].trim(), data[2].trim(), data[9].trim());
                    students.add(student);
                } else if (data.length >= 3) {
                    Student student = new Student(data[0], data[1], data[2]);
                    students.add(student);
                }
            }
        } catch (FileNotFoundException e) {
            return students;
        }
        return students;
    }

    public void saveGrade(Grade grade) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(
                new FileWriter(Constants.GRADES_FILE, true)))) {
            writer.println(grade.toFileString());
        } catch (IOException e) {
            throw new RuntimeException("Error saving grade", e);
        }
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
        Map<String, int[]> grading = new HashMap<>();
        File file = new File(Constants.GRADING_SYSTEM_FILE);
        if (!file.exists()) {
            return grading;
        }
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split("\\|");
                if (data.length < 3) {
                    continue;
                }
                grading.put(data[0], new int[]{
                        Integer.parseInt(data[1]), Integer.parseInt(data[2])
                });
            }
        } catch (FileNotFoundException e) {
            return grading;
        }
        return grading;
    }

    public String calculateGrade(double marks, double total) {
        double percentage = (total == 0) ? 0 : (marks / total) * 100.0;
        for (Map.Entry<String, int[]> entry : loadGradingSystem().entrySet()) {
            int min = entry.getValue()[0];
            int max = entry.getValue()[1];
            if (percentage >= min && percentage <= max) {
                return entry.getKey();
            }
        }
        return "";
    }
}
