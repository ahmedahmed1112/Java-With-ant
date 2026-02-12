package service;

import model.LeaderLecturerAssignment;
import model.Module;
import util.FileManager;

import java.util.*;

/**
 * ModuleService
 * -------------
 * Leader CRUD modules using: data/modules.txt
 * Format:
 * moduleId|moduleName|moduleCode|creditHours|leaderId|lecturerId
 *
 * Constraints:
 * - Max 3 modules per leader
 * - Max 3 lecturers per leader (based on leader_lecturer.txt)
 */
public class ModuleService {

    private static final String MODULES_FILE = "data/modules.txt";
    private static final String LEADER_LECTURER_FILE = "data/leader_lecturer.txt";
    private static final String USERS_FILE = "data/users.txt";
    private static final String LECTURERS_FILE = "data/lecturers.txt";

    // --------------------------
    // Reads
    // --------------------------

    public static List<Module> getAll() {
        List<Module> list = new ArrayList<>();
        List<String> lines = FileManager.readAll(MODULES_FILE);

        for (String line : lines) {
            Module m = Module.fromFileLine(line);
            if (m != null) list.add(m);
        }
        return list;
    }

    public static Module findById(String moduleId) {
        moduleId = safe(moduleId);
        if (moduleId.isEmpty()) return null;

        for (Module m : getAll()) {
            if (moduleId.equalsIgnoreCase(safe(m.getModuleId()))) return m;
        }
        return null;
    }

    public static List<Module> getByLeader(String leaderId) {
        leaderId = safe(leaderId);
        List<Module> out = new ArrayList<>();
        if (leaderId.isEmpty()) return out;

        for (Module m : getAll()) {
            if (leaderId.equalsIgnoreCase(safe(m.getLeaderId()))) {
                out.add(m);
            }
        }
        return out;
    }

    public static List<Module> getByLecturer(String lecturerId) {
        lecturerId = safe(lecturerId);
        List<Module> out = new ArrayList<>();
        if (lecturerId.isEmpty()) return out;

        for (Module m : getAll()) {
            if (lecturerId.equalsIgnoreCase(safe(m.getLecturerId()))) {
                out.add(m);
            }
        }
        return out;
    }

    public static Module findFirstByLecturer(String lecturerId) {
        List<Module> list = getByLecturer(lecturerId);
        return list.isEmpty() ? null : list.get(0);
    }

    // --------------------------
    // CRUD
    // --------------------------

    public static Module createModule(String leaderId, String moduleName, String moduleCode, int creditHours) {
        leaderId = safe(leaderId);
        moduleName = safe(moduleName);
        moduleCode = safe(moduleCode);

        if (leaderId.isEmpty()) throw new IllegalArgumentException("Leader ID is required.");
        if (moduleName.isEmpty()) throw new IllegalArgumentException("Module name is required.");
        if (moduleCode.isEmpty()) throw new IllegalArgumentException("Module code is required.");
        if (creditHours <= 0) throw new IllegalArgumentException("Credit hours must be a positive number.");
        if (creditHours > 10) throw new IllegalArgumentException("Credit hours must be between 1 and 10.");

        // Max 3 modules per leader
        if (getByLeader(leaderId).size() >= 3) {
            throw new IllegalArgumentException("Max 3 modules per leader. You already reached the limit.");
        }

        // Unique moduleCode
        for (Module m : getAll()) {
            if (moduleCode.equalsIgnoreCase(safe(m.getModuleCode()))) {
                throw new IllegalArgumentException("Module code already exists: " + moduleCode);
            }
        }

        String newId = generateNextModuleId();
        Module created = new Module(newId, moduleName, moduleCode, creditHours, leaderId, "");

        // use your FileManager.append
        FileManager.append(MODULES_FILE, created.toFileLine());
        syncLecturerLegacyFileFromModules();
        return created;
    }

    public static void updateModule(String leaderId, String moduleId, String newName, String newCode, int newCreditHours) {
        leaderId = safe(leaderId);
        moduleId = safe(moduleId);
        newName = safe(newName);
        newCode = safe(newCode);

        if (leaderId.isEmpty()) throw new IllegalArgumentException("Leader ID is required.");
        if (moduleId.isEmpty()) throw new IllegalArgumentException("Module ID is required.");
        if (newName.isEmpty()) throw new IllegalArgumentException("Module name is required.");
        if (newCode.isEmpty()) throw new IllegalArgumentException("Module code is required.");
        if (newCreditHours <= 0) throw new IllegalArgumentException("Credit hours must be a positive number.");
        if (newCreditHours > 10) throw new IllegalArgumentException("Credit hours must be between 1 and 10.");

        Module existing = findById(moduleId);
        if (existing == null) throw new IllegalArgumentException("Module not found: " + moduleId);

        // Leader can only edit their own modules
        if (!leaderId.equalsIgnoreCase(safe(existing.getLeaderId()))) {
            throw new IllegalArgumentException("You are not allowed to update a module owned by another leader.");
        }

        // Unique moduleCode (excluding itself)
        for (Module m : getAll()) {
            if (moduleId.equalsIgnoreCase(safe(m.getModuleId()))) continue;
            if (newCode.equalsIgnoreCase(safe(m.getModuleCode()))) {
                throw new IllegalArgumentException("Module code already exists: " + newCode);
            }
        }

        existing.setModuleName(newName);
        existing.setModuleCode(newCode);
        existing.setCreditHours(newCreditHours);

        // use your FileManager.updateById (first column is moduleId)
        FileManager.updateById(MODULES_FILE, moduleId, existing.toFileLine());
        syncLecturerLegacyFileFromModules();
    }

    public static void deleteModule(String leaderId, String moduleId) {
        leaderId = safe(leaderId);
        moduleId = safe(moduleId);

        if (leaderId.isEmpty()) throw new IllegalArgumentException("Leader ID is required.");
        if (moduleId.isEmpty()) throw new IllegalArgumentException("Module ID is required.");

        Module existing = findById(moduleId);
        if (existing == null) throw new IllegalArgumentException("Module not found: " + moduleId);

        if (!leaderId.equalsIgnoreCase(safe(existing.getLeaderId()))) {
            throw new IllegalArgumentException("You are not allowed to delete a module owned by another leader.");
        }

        // use your FileManager.deleteById
        FileManager.deleteById(MODULES_FILE, moduleId);
        syncLecturerLegacyFileFromModules();
    }

    // --------------------------
    // Lecturer assignment
    // --------------------------

    public static void assignLecturerToModule(String leaderId, String moduleId, String lecturerId) {
        leaderId = safe(leaderId);
        moduleId = safe(moduleId);
        lecturerId = safe(lecturerId);

        if (leaderId.isEmpty()) throw new IllegalArgumentException("Leader ID is required.");
        if (moduleId.isEmpty()) throw new IllegalArgumentException("Module ID is required.");
        if (lecturerId.isEmpty()) throw new IllegalArgumentException("Lecturer ID is required.");

        Module m = findById(moduleId);
        if (m == null) throw new IllegalArgumentException("Module not found: " + moduleId);

        if (!leaderId.equalsIgnoreCase(safe(m.getLeaderId()))) {
            throw new IllegalArgumentException("You are not allowed to assign lecturers for another leader's module.");
        }

        Set<String> allowedLecturers = getAllowedLecturersForLeader(leaderId);

        // Enforce max 3 lecturers per leader (based on leader_lecturer.txt)
        if (allowedLecturers.size() > 3) {
            throw new IllegalArgumentException("Max 3 lecturers per leader exceeded in leader_lecturer.txt. Fix admin assignment first.");
        }

        // Lecturer must be under this leader
        if (!allowedLecturers.contains(lecturerId.toUpperCase())) {
            throw new IllegalArgumentException("This lecturer is not assigned to you (Leader) in leader_lecturer.txt.");
        }

        // Keep lecturer ownership consistent (one lecturer -> one module in this system).
        for (Module other : getAll()) {
            if (moduleId.equalsIgnoreCase(safe(other.getModuleId()))) continue;
            if (lecturerId.equalsIgnoreCase(safe(other.getLecturerId()))) {
                throw new IllegalArgumentException("Lecturer " + lecturerId + " is already assigned to module " + other.getModuleId() + ".");
            }
        }

        m.setLecturerId(lecturerId);
        FileManager.updateById(MODULES_FILE, moduleId, m.toFileLine());
        syncLecturerLegacyFileFromModules();
    }

    public static void unassignLecturerFromModule(String leaderId, String moduleId) {
        leaderId = safe(leaderId);
        moduleId = safe(moduleId);

        if (leaderId.isEmpty()) throw new IllegalArgumentException("Leader ID is required.");
        if (moduleId.isEmpty()) throw new IllegalArgumentException("Module ID is required.");

        Module m = findById(moduleId);
        if (m == null) throw new IllegalArgumentException("Module not found: " + moduleId);

        if (!leaderId.equalsIgnoreCase(safe(m.getLeaderId()))) {
            throw new IllegalArgumentException("You are not allowed to unassign lecturers for another leader's module.");
        }

        m.setLecturerId("");
        FileManager.updateById(MODULES_FILE, moduleId, m.toFileLine());
        syncLecturerLegacyFileFromModules();
    }

    // --------------------------
    // Helpers
    // --------------------------

    private static Set<String> getAllowedLecturersForLeader(String leaderId) {
        List<String> lines = FileManager.readAll(LEADER_LECTURER_FILE);
        Set<String> lecturers = new LinkedHashSet<>();

        for (String line : lines) {
            if (line == null) continue;
            String t = line.trim();
            if (t.isEmpty()) continue;

            // If someone accidentally added a header line, skip it safely
            if (t.toLowerCase().startsWith("leaderid")) continue;

            LeaderLecturerAssignment a = parseLeaderLecturerLine(t);
            if (a == null) continue;

            if (leaderId.equalsIgnoreCase(safe(a.getLeaderId()))) {
                lecturers.add(safe(a.getLecturerId()).toUpperCase());
            }
        }
        return lecturers;
    }

    private static LeaderLecturerAssignment parseLeaderLecturerLine(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 2) return null;

        String leader = p[0].trim();
        String lec = p[1].trim();

        if (leader.isEmpty() || lec.isEmpty()) return null;

        return new LeaderLecturerAssignment(leader, lec);
    }

    private static String generateNextModuleId() {
        int max = 0;
        for (Module m : getAll()) {
            String id = safe(m.getModuleId()).toUpperCase();
            if (id.startsWith("MOD")) id = "M" + id.substring(3);

            if (id.startsWith("M") && id.length() > 1) {
                String num = id.substring(1);
                try {
                    int val = Integer.parseInt(num);
                    if (val > max) max = val;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("M%03d", max + 1);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * Keep lecturers.txt consistent with assignments in modules.txt.
     * users.txt remains primary for lecturer profile fields.
     */
    private static void syncLecturerLegacyFileFromModules() {
        List<String> userLines = FileManager.readAll(USERS_FILE);

        // lecturerId -> [username,password,name,gender,email,phone,age]
        Map<String, String[]> lecturerUsersById = new LinkedHashMap<>();
        // username -> lecturerId
        Map<String, String> lecturerIdByUsername = new HashMap<>();

        for (String line : userLines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length < 9) continue;

            String role = safe(p[8]).toUpperCase();
            if (!"LECTURER".equals(role)) continue;

            String lecturerId = safe(p[0]);
            String username = safe(p[1]);
            if (lecturerId.isEmpty() || username.isEmpty()) continue;

            lecturerUsersById.put(lecturerId.toUpperCase(), new String[]{
                    username,
                    safe(p[2]), // password
                    safe(p[3]), // name
                    safe(p[4]), // gender
                    safe(p[5]), // email
                    safe(p[6]), // phone
                    safe(p[7])  // age
            });
            lecturerIdByUsername.put(username.toUpperCase(), lecturerId.toUpperCase());
        }

        // lecturerId -> [moduleId, leaderId]
        Map<String, String[]> assignmentByLecturerId = new HashMap<>();
        for (Module m : getAll()) {
            String lecturerId = safe(m.getLecturerId()).toUpperCase();
            if (lecturerId.isEmpty()) continue;
            assignmentByLecturerId.put(lecturerId, new String[]{safe(m.getModuleId()), safe(m.getLeaderId())});
        }

        List<String> existing = FileManager.readAll(LECTURERS_FILE);
        List<String> out = new ArrayList<>();
        Set<String> seenUsernames = new HashSet<>();

        for (String line : existing) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|", -1);
            if (p.length < 9) continue;

            String username = safe(p[0]);
            if (username.isEmpty()) continue;

            String lecturerId = lecturerIdByUsername.get(username.toUpperCase());
            String[] userRow = lecturerId == null ? null : lecturerUsersById.get(lecturerId);
            String[] assignment = lecturerId == null ? null : assignmentByLecturerId.get(lecturerId);

            if (userRow != null) {
                p[0] = userRow[0];
                p[1] = userRow[1];
                p[2] = userRow[2];
                p[3] = userRow[3];
                p[4] = userRow[4];
                p[5] = userRow[5];
                p[6] = userRow[6];
            }

            p[7] = assignment == null ? "" : safe(assignment[0]);
            p[8] = assignment == null ? "" : safe(assignment[1]);

            out.add(String.join("|", Arrays.copyOf(p, 9)));
            seenUsernames.add(safe(p[0]).toUpperCase());
        }

        // Add any lecturer in users.txt missing from lecturers.txt
        for (Map.Entry<String, String[]> e : lecturerUsersById.entrySet()) {
            String lecturerId = e.getKey();
            String[] u = e.getValue();
            String username = safe(u[0]);
            if (username.isEmpty() || seenUsernames.contains(username.toUpperCase())) continue;

            String[] assignment = assignmentByLecturerId.get(lecturerId);
            String moduleId = assignment == null ? "" : safe(assignment[0]);
            String leaderId = assignment == null ? "" : safe(assignment[1]);

            out.add(username + "|" + u[1] + "|" + u[2] + "|" + u[3] + "|" + u[4] + "|" + u[5] + "|" + u[6] + "|" + moduleId + "|" + leaderId);
        }

        FileManager.writeAll(LECTURERS_FILE, out);
    }
}
