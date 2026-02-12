package service;

import model.Student;
import util.Constants;
import util.FileManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class StudentService {

    public static Student getStudentProfile(String username) {
        String uname = safe(username);
        if (uname.isEmpty()) return null;

        Student student = null;
        List<String> userLines = FileManager.readAll(Constants.USERS_FILE);
        for (String line : userLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");

            if (p.length >= 9) {
                String role = safe(p[8]).toUpperCase();
                if (!"STUDENT".equals(role)) continue;
                if (!safe(p[1]).equalsIgnoreCase(uname)) continue;

                student = new Student();
                student.setUserId(safe(p[0]));
                student.setUsername(safe(p[1]));
                student.setPassword(safe(p[2]));
                student.setName(safe(p[3]));
                student.setGender(safe(p[4]));
                student.setEmail(safe(p[5]));
                student.setPhone(safe(p[6]));
                student.setAge(parseIntSafe(safe(p[7]), 0));
                student.setRole("STUDENT");
                break;
            }

            if (p.length >= 5) {
                String role = safe(p[4]).toUpperCase();
                if (!"STUDENT".equals(role)) continue;
                if (!safe(p[2]).equalsIgnoreCase(uname)) continue;

                student = new Student();
                student.setUserId(safe(p[0]));
                student.setUsername(safe(p[2]));
                student.setPassword(safe(p[3]));
                student.setName(safe(p[1]));
                student.setRole("STUDENT");
                break;
            }
        }

        if (student == null) return null;

        List<String> stuLines = FileManager.readAll(Constants.STUDENTS_FILE);
        for (String line : stuLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);

            // Legacy extended (extra column kept for backward compatibility)
            if (p.length >= 10 && safe(p[0]).equalsIgnoreCase(uname)) {
                student.setStudentId(safe(p[7]));
                student.setModuleId(safe(p[9]));
                break;
            }

            // Current extended: username|password|name|gender|email|phone|age|studentId|moduleId
            if (p.length >= 9 && safe(p[0]).equalsIgnoreCase(uname)) {
                student.setStudentId(safe(p[7]));
                student.setModuleId(safe(p[8]));
                break;
            }

            // Compact schemas: studentId|userId (or a legacy line with one extra trailing column)
            if (p.length >= 2 && !safe(student.getUserId()).isEmpty()
                    && safe(p[1]).equalsIgnoreCase(student.getUserId())) {
                student.setStudentId(safe(p[0]));
                student.setModuleId(findModuleForStudentFromClasses(safe(p[0])));
                break;
            }
        }

        if (safe(student.getStudentId()).isEmpty()) {
            student.setStudentId(safe(student.getUserId()));
        }

        return student;
    }

    public static void updateProfile(Student student) {
        if (student == null) return;

        String userId = safe(student.getUserId());
        String username = safe(student.getUsername());
        if (userId.isEmpty() && username.isEmpty()) return;

        List<String> userLines = FileManager.readAll(Constants.USERS_FILE);
        List<String> userOut = new ArrayList<>();
        boolean userUpdated = false;

        for (String line : userLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");

            if (p.length >= 9) {
                boolean match = (!userId.isEmpty() && safe(p[0]).equalsIgnoreCase(userId))
                        || (userId.isEmpty() && safe(p[1]).equalsIgnoreCase(username) && "STUDENT".equalsIgnoreCase(safe(p[8])));

                if (match) {
                    String uid = userId.isEmpty() ? safe(p[0]) : userId;
                    userId = uid;
                    if (username.isEmpty()) username = safe(p[1]);
                    String password = safe(student.getPassword()).isEmpty() ? safe(p[2]) : safe(student.getPassword());

                    userOut.add(uid + "|" + username + "|" + password + "|" +
                            safe(student.getName()) + "|" + safe(student.getGender()) + "|" +
                            safe(student.getEmail()) + "|" + safe(student.getPhone()) + "|" +
                            student.getAge() + "|STUDENT");
                    userUpdated = true;
                } else {
                    userOut.add(line);
                }
                continue;
            }

            userOut.add(line);
        }

        if (!userUpdated && !username.isEmpty()) {
            String uid = userId.isEmpty() ? username : userId;
            userId = uid;
            userOut.add(uid + "|" + username + "|" + safe(student.getPassword()) + "|" +
                    safe(student.getName()) + "|" + safe(student.getGender()) + "|" +
                    safe(student.getEmail()) + "|" + safe(student.getPhone()) + "|" +
                    student.getAge() + "|STUDENT");
        }
        FileManager.writeAll(Constants.USERS_FILE, userOut);

        String studentId = safe(student.getStudentId());
        if (studentId.isEmpty()) studentId = generateNextStudentRecordId();
        String moduleId = safe(student.getModuleId());

        List<String> stuLines = FileManager.readAll(Constants.STUDENTS_FILE);
        List<String> stuOut = new ArrayList<>();
        boolean matched = false;

        for (String line : stuLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);

            boolean isExtended = p.length >= 9 && safe(p[0]).equalsIgnoreCase(username);
            boolean isCompact = p.length >= 2 && p.length < 9 && !userId.isEmpty() && safe(p[1]).equalsIgnoreCase(userId);

            if (!isExtended && !isCompact) {
                stuOut.add(line);
                continue;
            }

            // Remove duplicates, keep one normalized row.
            if (matched) continue;
            matched = true;

            if (isExtended) {
                String sid = safe(p[7]).isEmpty() ? studentId : safe(p[7]);
                String existingMod = (p.length >= 10) ? safe(p[9]) : safe(p[8]);
                String mod = moduleId.isEmpty() ? existingMod : moduleId;
                stuOut.add(buildStudentExtendedLine(student, sid, mod));
            } else {
                String sid = safe(p[0]).isEmpty() ? studentId : safe(p[0]);
                stuOut.add(buildStudentCompactLine(sid, userId));
            }
        }

        if (!matched && !userId.isEmpty()) {
            stuOut.add(buildStudentCompactLine(studentId, userId));
        }
        FileManager.writeAll(Constants.STUDENTS_FILE, stuOut);
    }

    public static List<String[]> getAvailableClasses(String studentModuleId) {
        List<String[]> classes = new ArrayList<>();
        String filterModule = safe(studentModuleId);

        List<String> lines = FileManager.readAll(Constants.CLASSES_FILE);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length < 3) continue;

            String moduleId = safe(parts[2]);
            if (!filterModule.isEmpty() && !moduleId.equalsIgnoreCase(filterModule)) continue;

            classes.add(new String[]{safe(parts[0]), safe(parts[1]), moduleId});
        }
        return classes;
    }

    public static void registerForClass(String studentId, String classId) {
        FileManager.append(Constants.STUDENT_CLASSES_FILE, safe(studentId) + "|" + safe(classId));
    }

    public static List<String[]> getRegisteredClasses(String studentId) {
        List<String[]> registered = new ArrayList<>();
        String sid = safe(studentId);
        if (sid.isEmpty()) return registered;

        List<String> lines = FileManager.readAll(Constants.STUDENT_CLASSES_FILE);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 2 && safe(p[0]).equalsIgnoreCase(sid)) {
                registered.add(new String[]{safe(p[0]), safe(p[1])});
            }
        }
        return registered;
    }

    public static boolean isAlreadyRegistered(String studentId, String classId) {
        for (String[] reg : getRegisteredClasses(studentId)) {
            if (safe(reg[1]).equalsIgnoreCase(safe(classId))) return true;
        }
        return false;
    }

    public static List<String[]> getMyResults(String studentId) {
        List<String[]> results = new ArrayList<>();
        String sid = safe(studentId);
        if (sid.isEmpty()) return results;

        List<String> gradeLines = FileManager.readAll(Constants.GRADES_FILE);
        List<String> assessmentLines = FileManager.readAll(Constants.ASSESSMENTS_FILE);
        List<String> feedbackLines = FileManager.readAll(Constants.FEEDBACK_FILE);

        for (String gLine : gradeLines) {
            if (gLine == null || gLine.trim().isEmpty()) continue;
            String[] gParts = gLine.split("\\|");
            if (gParts.length < 7) continue;
            if (!safe(gParts[2]).equalsIgnoreCase(sid)) continue;

            String assessmentId = safe(gParts[1]);
            String marks = safe(gParts[3]);
            String grade = safe(gParts[4]);

            String assessmentName = assessmentId;
            String moduleId = "";
            for (String aLine : assessmentLines) {
                if (aLine == null || aLine.trim().isEmpty()) continue;
                String[] aParts = aLine.split("\\|");
                if (aParts.length >= 7 && safe(aParts[0]).equalsIgnoreCase(assessmentId)) {
                    assessmentName = safe(aParts[2]);
                    moduleId = safe(aParts[1]);
                    break;
                }
            }

            String feedback = "";
            for (String fLine : feedbackLines) {
                if (fLine == null || fLine.trim().isEmpty()) continue;
                String[] fParts = fLine.split("\\|");
                if (fParts.length >= 6
                        && safe(fParts[1]).equalsIgnoreCase(assessmentId)
                        && safe(fParts[2]).equalsIgnoreCase(sid)) {
                    feedback = safe(fParts[4]);
                    break;
                }
            }

            results.add(new String[]{moduleId, assessmentName, marks, grade, feedback});
        }
        return results;
    }

    public static List<String[]> getModulesForComment(String studentId) {
        Set<String> allowedModules = new LinkedHashSet<>();
        String sid = safe(studentId);

        if (!sid.isEmpty()) {
            List<String> registrations = FileManager.readAll(Constants.STUDENT_CLASSES_FILE);
            List<String> classLines = FileManager.readAll(Constants.CLASSES_FILE);

            for (String line : registrations) {
                if (line == null || line.trim().isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length < 2 || !safe(p[0]).equalsIgnoreCase(sid)) continue;

                String classId = safe(p[1]);
                for (String cLine : classLines) {
                    if (cLine == null || cLine.trim().isEmpty()) continue;
                    String[] cp = cLine.split("\\|");
                    if (cp.length >= 3 && safe(cp[0]).equalsIgnoreCase(classId)) {
                        allowedModules.add(safe(cp[2]));
                    }
                }
            }
        }

        List<String[]> modules = new ArrayList<>();
        List<String> moduleLines = FileManager.readAll(Constants.MODULES_FILE);
        for (String line : moduleLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length < 6) continue;

            String moduleId = safe(p[0]);
            if (!allowedModules.isEmpty() && !allowedModules.contains(moduleId)) continue;

            modules.add(new String[]{moduleId, safe(p[1]), safe(p[5])});
        }

        // Fallback: if student has no registrations yet, show all modules.
        if (modules.isEmpty() && allowedModules.isEmpty()) {
            for (String line : moduleLines) {
                if (line == null || line.trim().isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length < 6) continue;
                modules.add(new String[]{safe(p[0]), safe(p[1]), safe(p[5])});
            }
        }

        return modules;
    }

    public static void submitComment(String studentId, String lecturerId, String moduleId, String comment) {
        String sid = safe(studentId);
        String mid = safe(moduleId);
        if (sid.isEmpty() || mid.isEmpty() || safe(comment).isEmpty()) return;

        String lid = safe(lecturerId);
        if (lid.isEmpty()) {
            List<String> moduleLines = FileManager.readAll(Constants.MODULES_FILE);
            for (String line : moduleLines) {
                if (line == null || line.trim().isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length >= 6 && safe(p[0]).equalsIgnoreCase(mid)) {
                    lid = safe(p[5]);
                    break;
                }
            }
        }

        String commentId = "CMT" + System.currentTimeMillis();
        String date = java.time.LocalDate.now().toString();
        FileManager.append(Constants.COMMENTS_FILE,
                commentId + "|" + sid + "|" + lid + "|" + mid + "|" + safe(comment) + "|" + date);
    }

    // Backward-compatible method signature.
    public static void submitComment(String studentId, String moduleId, String comment) {
        submitComment(studentId, "", moduleId, comment);
    }

    public static List<String[]> getComments(String studentId) {
        List<String[]> comments = new ArrayList<>();
        String sid = safe(studentId);
        if (sid.isEmpty()) return comments;

        List<String> lines = FileManager.readAll(Constants.COMMENTS_FILE);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");

            // commentId|studentId|lecturerId|moduleId|comment|date
            if (p.length >= 6 && safe(p[1]).equalsIgnoreCase(sid)) {
                comments.add(new String[]{safe(p[0]), safe(p[3]), safe(p[4]), safe(p[5])});
                continue;
            }

            // Legacy: commentId|studentId|moduleId|comment|date
            if (p.length >= 5 && safe(p[1]).equalsIgnoreCase(sid)) {
                comments.add(new String[]{safe(p[0]), safe(p[2]), safe(p[3]), safe(p[4])});
            }
        }
        return comments;
    }

    public static String getStudentIdByUserId(String userId) {
        String uid = safe(userId);
        if (uid.isEmpty()) return "";

        String username = "";
        List<String> userLines = FileManager.readAll(Constants.USERS_FILE);
        for (String line : userLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length >= 9 && safe(p[0]).equalsIgnoreCase(uid)) {
                username = safe(p[1]);
                break;
            }
        }

        List<String> stuLines = FileManager.readAll(Constants.STUDENTS_FILE);
        for (String line : stuLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");

            if (p.length >= 9 && !username.isEmpty() && safe(p[0]).equalsIgnoreCase(username)) {
                return safe(p[7]);
            }
            if (p.length >= 2 && safe(p[1]).equalsIgnoreCase(uid)) {
                return safe(p[0]);
            }
        }

        return "";
    }

    private static String findModuleForStudentFromClasses(String studentId) {
        String sid = safe(studentId);
        if (sid.isEmpty()) return "";

        List<String> regLines = FileManager.readAll(Constants.STUDENT_CLASSES_FILE);
        List<String> classLines = FileManager.readAll(Constants.CLASSES_FILE);

        for (String line : regLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length < 2 || !safe(p[0]).equalsIgnoreCase(sid)) continue;

            String classId = safe(p[1]);
            for (String cLine : classLines) {
                if (cLine == null || cLine.trim().isEmpty()) continue;
                String[] cp = cLine.split("\\|");
                if (cp.length >= 3 && safe(cp[0]).equalsIgnoreCase(classId)) {
                    return safe(cp[2]);
                }
            }
        }

        return "";
    }

    private static String buildStudentExtendedLine(Student student, String studentId, String moduleId) {
        return safe(student.getUsername()) + "|" +
                safe(student.getPassword()) + "|" +
                safe(student.getName()) + "|" +
                safe(student.getGender()) + "|" +
                safe(student.getEmail()) + "|" +
                safe(student.getPhone()) + "|" +
                student.getAge() + "|" +
                safe(studentId) + "|" +
                safe(moduleId);
    }

    private static String buildStudentCompactLine(String studentId, String userId) {
        return safe(studentId) + "|" + safe(userId);
    }

    private static String generateNextStudentRecordId() {
        int max = 0;
        List<String> lines = FileManager.readAll(Constants.STUDENTS_FILE);
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            String sid = "";
            if (p.length >= 9) sid = safe(p[7]);
            else if (p.length >= 1) sid = safe(p[0]);

            String upper = sid.toUpperCase();
            if (!upper.startsWith("TP") || upper.length() <= 2) continue;
            try {
                int n = Integer.parseInt(upper.substring(2));
                if (n > max) max = n;
            } catch (NumberFormatException ignored) {
            }
        }
        return String.format("TP%06d", max + 1);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception ignored) { return def; }
    }
}
