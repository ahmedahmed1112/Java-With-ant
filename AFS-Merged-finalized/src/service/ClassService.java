package service;

import model.ClassRecord;
import util.FileManager;

import java.util.ArrayList;
import java.util.List;

public class ClassService {

    private static final String CLASSES_FILE = "data/classes.txt";

    public static List<ClassRecord> getAll() {
        List<ClassRecord> list = new ArrayList<>();
        List<String> lines = FileManager.readAll(CLASSES_FILE);

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;

            String[] p = line.split("\\|");
            if (p.length < 3) continue;

            list.add(new ClassRecord(p[0], p[1], p[2]));
        }
        return list;
    }

    public static boolean existsClassId(String classId) {
        for (ClassRecord c : getAll()) {
            if (c.getClassId().equalsIgnoreCase(classId)) return true;
        }
        return false;
    }

    public static void add(ClassRecord rec) {
        validateClassRecord(rec, null);
        FileManager.append(CLASSES_FILE, rec.toString());
    }

    public static void update(String classId, ClassRecord newRec) {
        validateClassRecord(newRec, classId);
        FileManager.updateById(CLASSES_FILE, classId, newRec.toString());
    }

    public static void delete(String classId) {
        FileManager.deleteById(CLASSES_FILE, classId);
    }

    private static void validateClassRecord(ClassRecord rec, String excludeClassId) {
        if (rec == null) throw new IllegalArgumentException("Class data is required.");

        String classId = safe(rec.getClassId());
        String className = safe(rec.getClassName());
        String moduleId = safe(rec.getModuleId());

        if (classId.isEmpty()) throw new IllegalArgumentException("Class ID is required.");
        if (className.isEmpty()) throw new IllegalArgumentException("Class name is required.");
        if (moduleId.isEmpty()) throw new IllegalArgumentException("Module ID is required.");

        if (ModuleService.findById(moduleId) == null) {
            throw new IllegalArgumentException("Module does not exist: " + moduleId);
        }

        for (ClassRecord c : getAll()) {
            if (excludeClassId != null && c.getClassId().equalsIgnoreCase(excludeClassId)) continue;
            if (c.getModuleId().equalsIgnoreCase(moduleId)) {
                throw new IllegalArgumentException("Only one class is allowed per module. Existing class: " + c.getClassId());
            }
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
