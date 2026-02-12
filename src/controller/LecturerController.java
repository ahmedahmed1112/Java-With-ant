package controller;

import model.Assessment;
import model.Feedback;
import model.Grade;
import model.Lecturer;
import model.Student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LecturerController {
    private final FileHandler fileHandler;

    public LecturerController(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    public void updateProfile(Lecturer lecturer) {
        fileHandler.updateLecturer(lecturer);
    }

    public List<Assessment> getAssessmentsByModule(String moduleId) {
        return fileHandler.loadAssessmentsByModule(moduleId);
    }

    public List<Student> getStudentsByModule(String moduleId) {
        List<Student> result = new ArrayList<>();
        for (Student student : fileHandler.loadStudents()) {
            if (moduleId.equals(student.getModuleId())) {
                result.add(student);
            }
        }
        return result;
    }

    public void addAssessment(Assessment assessment) {
        fileHandler.saveAssessment(assessment);
    }

    public void updateAssessment(Assessment assessment) {
        fileHandler.updateAssessment(assessment);
    }

    public void deleteAssessment(String assessmentId) {
        fileHandler.deleteAssessment(assessmentId);
    }

    public void saveGrades(List<Grade> grades) {
        for (Grade grade : grades) {
            fileHandler.saveGrade(grade);
        }
    }

    public List<Grade> getGradesByAssessment(String assessmentId) {
        List<Grade> result = new ArrayList<>();
        for (Grade grade : fileHandler.loadGrades()) {
            if (assessmentId.equals(grade.getAssessmentId())) {
                result.add(grade);
            }
        }
        return result;
    }

    public Feedback getFeedback(String assessmentId, String studentId) {
        for (Feedback feedback : fileHandler.loadFeedback()) {
            if (assessmentId.equals(feedback.getAssessmentId())
                    && studentId.equals(feedback.getStudentId())) {
                return feedback;
            }
        }
        return null;
    }

    public void saveFeedback(Feedback feedback) {
        fileHandler.saveFeedback(feedback);
    }

    public String calculateGrade(double marks, double total) {
        return fileHandler.calculateGrade(marks, total);
    }

    public String today() {
        return LocalDate.now().toString();
    }
}
