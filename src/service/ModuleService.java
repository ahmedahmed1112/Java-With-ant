package service;

import model.LeaderLecturerAssignment;
import model.Module;
import model.User;
import util.Constants;
import util.FileManager;

import java.util.*;

/**
 * ModuleService
 * -------------
 * Leader CRUD modules using: data/modules.txt
 * Format:
 * moduleId|moduleName|moduleCode|creditHours|leaderId|lecturerId
 *
 * FIX (Lecturer-Student bug chain):
 * - When a leader assigns a lecturer to a module, modules.txt is updated,
 *   BUT lecturers.txt was not updated, so the Lecturer UI can't see the module.
 * - Now we sync lecturers.txt whenever assignment/unassignment happens.
 */
public class ModuleService {

    private static final String MODULES_FILE = Constants.MODULES_FILE;
    private static final String LEADER_LECTURER_FILE = "data/leader_lecturer.txt"; // no constant in Constants.java

    // lecturers.txt schema:
    // username|password|name|gender|email|phone|age|assignedModuleId|academicLeaderId
    private static final String LECTURERS_FILE = Constants.LECTURERS_FILE;

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

        if (getByLeader(leaderId).size() >= 3) {
            throw new IllegalArgumentException("Max 3 modules per leader. You already reached the limit.");
        }

        for (Module m : getAll()) {
            if (moduleCode.equalsIgnoreCase(safe(m.getModuleCode()))) {
                throw new IllegalArgumentException("Module code already exists: " + moduleCode);
            }
        }

        String newId = generateNextModuleId();
        Module created = new Module(newId, moduleName, moduleCode, creditHours, leaderId, "");

        FileManager.append(MODULES_FILE, created.toFileLine());
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

        Module existing = findById(moduleId);
        if (existing == null) throw new IllegalArgumentException("Module not found: " + moduleId);

        if (!leaderId.equalsIgnoreCase(safe(existing.getLeaderId()))) {
            throw new IllegalArgumentException("You are not allowed to update a module owned by another leader.");
        }

        for (Module m : getAll()) {
            if (moduleId.equalsIgnoreCase(safe(m.getModuleId()))) continue;
            if (newCode.equalsIgnoreCase(safe(m.getModuleCode()))) {
                throw new IllegalArgumentException("Module code already exists: " + newCode);
            }
        }

        existing.setModuleName(newName);
        existing.setModuleCode(newCode);
        existing.setCreditHours(newCreditHours);

        FileManager.updateById(MODULES_FILE, moduleId, existing.toFileLine());
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

        // If this module had a lecturer assigned, clear the lecturer's assignedModuleId too.
        String lecId = safe(existing.getLecturerId());
        if (!lecId.isEmpty()) {
            syncLecturerAssignment(lecId, "", "");
        }

        FileManager.deleteById(MODULES_FILE, moduleId);
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

        if (allowedLecturers.size() > 3) {
            throw new IllegalArgumentException("Max 3 lecturers per leader exceeded in leader_lecturer.txt. Fix admin assignment first.");
        }

        if (!allowedLecturers.contains(lecturerId.toUpperCase())) {
            throw new IllegalArgumentException("This lecturer is not assigned to you (Leader) in leader_lecturer.txt.");
        }

        // 1) Update modules.txt (existing behavior)
        m.setLecturerId(lecturerId);
        FileManager.updateById(MODULES_FILE, moduleId, m.toFileLine());

        // 2) NEW: Sync lecturers.txt so Lecturer dashboard can see the module
        syncLecturerAssignment(lecturerId, moduleId, leaderId);
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

        String oldLecturerId = safe(m.getLecturerId());

        m.setLecturerId("");
        FileManager.updateById(MODULES_FILE, moduleId, m.toFileLine());

        // NEW: also clear lecturers.txt assignment
        if (!oldLecturerId.isEmpty()) {
            syncLecturerAssignment(oldLecturerId, "", "");
        }
    }

    /**
     * Sync the lecturer's record in lecturers.txt with assignedModuleId + academicLeaderId.
     * - lecturerId is like T001
     * - lecturers.txt is keyed by username (lecturer01), so we map lecturerId -> username from users.txt.
     */
    private static void syncLecturerAssignment(String lecturerId, String moduleId, String leaderId) {
        lecturerId = safe(lecturerId);
        moduleId = safe(moduleId);
        leaderId = safe(leaderId);
        if (lecturerId.isEmpty()) return;

        // Map lecturerId (T001) -> username (lecturer01)
        UserService userService = new UserService();
        User u = userService.getById(lecturerId);
        if (u == null || u.getUsername() == null || u.getUsername().trim().isEmpty()) {
            return; // no matching user in users.txt
        }
        String usernameKey = u.getUsername().trim();

        List<String> lines = FileManager.readAll(LECTURERS_FILE);
        List<String> out = new ArrayList<>();

        boolean updated = false;

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;

            String[] p = line.split("\\|", -1);
            // expected 9 fields in lecturers.txt
            if (p.length < 9) {
                out.add(line);
                continue;
            }

            String username = p[0].trim();
            if (!username.equalsIgnoreCase(usernameKey)) {
                out.add(line);
                continue;
            }

            // Update assignedModuleId + academicLeaderId only
            p[7] = moduleId; // assigned module
            p[8] = leaderId; // leader in charge

            out.add(String.join("|", p));
            updated = true;
        }

        // If lecturer doesn't exist in lecturers.txt, do not auto-create (avoid changing structure silently).
        if (updated) {
            FileManager.writeAll(LECTURERS_FILE, out);
        }
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
            String id = safe(m.getModuleId()).toUpperCase(); // e.g., M001
            if (id.startsWith("M") && id.length() >= 2) {
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
}
