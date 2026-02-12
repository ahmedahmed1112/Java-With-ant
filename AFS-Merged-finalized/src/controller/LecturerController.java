package controller;

import model.Assessment;
import model.Feedback;
import model.Grade;
import model.Lecturer;
import model.Student;
import util.Constants;
import util.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LecturerController {
    private final FileHandler fileHandler;

    public LecturerController(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    public void updateProfile(Lecturer lecturer) {
        fileHandler.updateLecturer(lecturer);
    }

    public List<Assessment> getAssessmentsByModule(String moduleId) {
        if (moduleId == null || moduleId.trim().isEmpty()) return new ArrayList<>();
        return fileHandler.loadAssessmentsByModule(moduleId);
    }

    public List<Assessment> getAssessmentsByModuleForLecturer(String lecturerId, String moduleId) {
        if (!isLecturerAssignedToModule(lecturerId, moduleId)) {
            return new ArrayList<>();
        }
        return getAssessmentsByModule(moduleId);
    }

    public List<Student> getStudentsByModule(String moduleId) {
        List<Student> result = new ArrayList<>();
        if (moduleId == null || moduleId.trim().isEmpty()) return result;
        for (Student student : fileHandler.loadStudents()) {
            if (moduleId.equalsIgnoreCase(student.getModuleId())) {
                result.add(student);
            }
        }
        return result;
    }

    public List<Student> getStudentsByModuleForLecturer(String lecturerId, String moduleId) {
        List<Student> result = new ArrayList<>();
        if (!isLecturerAssignedToModule(lecturerId, moduleId)) {
            return result;
        }

        Set<String> enrolledStudentIds = getEnrolledStudentIdsForModule(moduleId);
        if (enrolledStudentIds.isEmpty()) {
            return result;
        }

        Map<String, Student> studentById = new LinkedHashMap<>();
        for (Student s : fileHandler.loadStudents()) {
            String sid = safe(s.getStudentId());
            if (sid.isEmpty()) continue;
            String key = sid.toUpperCase();
            if (!studentById.containsKey(key)) {
                studentById.put(key, s);
            }
        }

        for (String studentId : enrolledStudentIds) {
            Student source = studentById.get(studentId.toUpperCase());
            String name = source == null ? studentId : safe(source.getName());
            if (name.isEmpty()) {
                name = studentId;
            }
            result.add(new Student(studentId, name, moduleId));
        }
        return result;
    }

    public boolean isLecturerAssignedToModule(String lecturerId, String moduleId) {
        String lecturerKey = safe(lecturerId).toUpperCase();
        String moduleKey = safe(moduleId).toUpperCase();
        if (lecturerKey.isEmpty() || moduleKey.isEmpty()) {
            return false;
        }

        for (String line : FileManager.readAll(Constants.MODULES_FILE)) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length < 6) continue;

            String rowModuleId = safe(p[0]).toUpperCase();
            String rowLecturerId = safe(p[5]).toUpperCase();

            if (moduleKey.equals(rowModuleId) && lecturerKey.equals(rowLecturerId)) {
                return true;
            }
        }
        return false;
    }

    public boolean canLecturerAccessAssessment(String lecturerId, String moduleId, String assessmentId) {
        if (!isLecturerAssignedToModule(lecturerId, moduleId)) {
            return false;
        }
        String moduleKey = safe(moduleId).toUpperCase();
        String assessmentKey = safe(assessmentId).toUpperCase();
        if (assessmentKey.isEmpty()) {
            return false;
        }

        for (Assessment a : fileHandler.loadAssessments()) {
            if (assessmentKey.equals(safe(a.getAssessmentId()).toUpperCase())) {
                return moduleKey.equals(safe(a.getModuleId()).toUpperCase());
            }
        }
        return false;
    }

    public boolean canLecturerAssessStudent(String lecturerId, String moduleId, String studentId) {
        if (!isLecturerAssignedToModule(lecturerId, moduleId)) {
            return false;
        }
        String studentKey = safe(studentId).toUpperCase();
        if (studentKey.isEmpty()) {
            return false;
        }
        for (String enrolledId : getEnrolledStudentIdsForModule(moduleId)) {
            if (studentKey.equals(enrolledId.toUpperCase())) {
                return true;
            }
        }
        return false;
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
            fileHandler.saveOrUpdateGrade(grade);
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
        List<Feedback> list = fileHandler.loadFeedback();
        for (int i = list.size() - 1; i >= 0; i--) {
            Feedback feedback = list.get(i);
            if (assessmentId.equals(feedback.getAssessmentId())
                    && studentId.equals(feedback.getStudentId())) {
                return feedback;
            }
        }
        return null;
    }

    public void saveFeedback(Feedback feedback) {
        fileHandler.saveOrUpdateFeedback(feedback);
    }

    public String calculateGrade(double marks, double total) {
        return fileHandler.calculateGrade(marks, total);
    }

    public String today() {
        return LocalDate.now().toString();
    }

    private Set<String> getEnrolledStudentIdsForModule(String moduleId) {
        Set<String> enrolled = new LinkedHashSet<>();
        String moduleKey = safe(moduleId).toUpperCase();
        if (moduleKey.isEmpty()) return enrolled;

        Set<String> classIdsForModule = new LinkedHashSet<>();
        for (String line : FileManager.readAll(Constants.CLASSES_FILE)) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length < 3) continue;
            String classId = safe(p[0]).toUpperCase();
            String classModuleId = safe(p[2]).toUpperCase();
            if (!classId.isEmpty() && moduleKey.equals(classModuleId)) {
                classIdsForModule.add(classId);
            }
        }

        if (classIdsForModule.isEmpty()) return enrolled;

        Set<String> seenStudentIds = new LinkedHashSet<>();
        for (String line : FileManager.readAll(Constants.STUDENT_CLASSES_FILE)) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length < 2) continue;
            String studentId = safe(p[0]);
            String classId = safe(p[1]).toUpperCase();
            if (studentId.isEmpty() || classId.isEmpty()) continue;
            if (!classIdsForModule.contains(classId)) continue;

            String key = studentId.toUpperCase();
            if (seenStudentIds.add(key)) {
                enrolled.add(studentId);
            }
        }

        return enrolled;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
