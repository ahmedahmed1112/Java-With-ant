package service;

import model.GradingRule;
import util.FileManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GradingService {

    private static final String GRADING_FILE = "data/grading.txt";

    private static final Set<String> ALLOWED_GRADES = new HashSet<>();
    static {
        // A band
        ALLOWED_GRADES.add("A+");
        ALLOWED_GRADES.add("A");
        ALLOWED_GRADES.add("A-");

        // B band
        ALLOWED_GRADES.add("B+");
        ALLOWED_GRADES.add("B");
        ALLOWED_GRADES.add("B-");

        // C band
        ALLOWED_GRADES.add("C+");
        ALLOWED_GRADES.add("C");
        ALLOWED_GRADES.add("C-");

        // D band
        ALLOWED_GRADES.add("D+");
        ALLOWED_GRADES.add("D");
        ALLOWED_GRADES.add("D-");

        // F band
        ALLOWED_GRADES.add("F+");
        ALLOWED_GRADES.add("F");
        ALLOWED_GRADES.add("F-");
    }

    public static boolean isAllowedGrade(String grade) {
        if (grade == null) return false;
        return ALLOWED_GRADES.contains(grade.trim().toUpperCase());
    }

    public static List<GradingRule> getAll() {
        List<GradingRule> list = new ArrayList<>();
        List<String> lines = FileManager.readAll(GRADING_FILE);

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;

            String[] p = line.split("\\|");
            if (p.length < 3) continue;

            try {
                String grade = p[0].trim().toUpperCase();
                int min = Integer.parseInt(p[1].trim());
                int max = Integer.parseInt(p[2].trim());

                // Keep file clean: only load allowed grades
                if (!isAllowedGrade(grade)) continue;

                list.add(new GradingRule(grade, min, max));
            } catch (Exception ignored) {
            }
        }
        return list;
    }

    // grade is the unique key
    public static boolean existsGrade(String grade) {
        if (grade == null) return false;
        String g = grade.trim().toUpperCase();
        for (GradingRule r : getAll()) {
            if (r.getGrade().equalsIgnoreCase(g)) return true;
        }
        return false;
    }

    public static void add(GradingRule rule) {
        if (rule == null) return;
        if (!isAllowedGrade(rule.getGrade())) return;
        FileManager.append(GRADING_FILE, rule.toString());
    }

    public static void update(String gradeKey, GradingRule newRule) {
        if (gradeKey == null || newRule == null) return;
        if (!isAllowedGrade(newRule.getGrade())) return;
        FileManager.updateById(GRADING_FILE, gradeKey.trim().toUpperCase(), newRule.toString());
    }

    public static void delete(String gradeKey) {
        if (gradeKey == null) return;
        FileManager.deleteById(GRADING_FILE, gradeKey.trim().toUpperCase());
    }

    // Optional helper for UI
    public static List<String> getAllowedGradesOrdered() {
        List<String> out = new ArrayList<>();
        String[] order = {
            "A+","A","A-",
            "B+","B","B-",
            "C+","C","C-",
            "D+","D","D-",
            "F+","F","F-"
        };
        for (String g : order) out.add(g);
        return out;
    }
}
