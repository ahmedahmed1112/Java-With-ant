package util;

import java.nio.file.Paths;

public final class Constants {
    public static final String DATA_DIR = "data";
    public static final String DELIMITER = "|";

    public static final String USERS_FILE = Paths.get(DATA_DIR, "users.txt").toString();
    public static final String LECTURERS_FILE = Paths.get(DATA_DIR, "lecturers.txt").toString();
    public static final String STUDENTS_FILE = Paths.get(DATA_DIR, "students.txt").toString();
    public static final String MODULES_FILE = Paths.get(DATA_DIR, "modules.txt").toString();
    public static final String GRADING_FILE = Paths.get(DATA_DIR, "grading.txt").toString();
    public static final String ASSESSMENTS_FILE = Paths.get(DATA_DIR, "assessments.txt").toString();
    public static final String GRADES_FILE = Paths.get(DATA_DIR, "grades.txt").toString();
    public static final String FEEDBACK_FILE = Paths.get(DATA_DIR, "feedback.txt").toString();
    public static final String GRADING_SYSTEM_FILE = Paths.get(DATA_DIR, "grading_system.txt").toString();
    public static final String CLASSES_FILE = Paths.get(DATA_DIR, "classes.txt").toString();
    public static final String STUDENT_CLASSES_FILE = Paths.get(DATA_DIR, "student_classes.txt").toString();
    public static final String COMMENTS_FILE = Paths.get(DATA_DIR, "comments.txt").toString();

    private Constants() {
    }
}
