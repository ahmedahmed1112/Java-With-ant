package model;

/**
 * Module model stored in: data/modules.txt
 * Format:
 * moduleId|moduleName|moduleCode|creditHours|leaderId|lecturerId
 *
 * OOP pillars:
 * - Encapsulation: private fields + getters/setters
 * - Abstraction: fromFileLine(...) and toFileLine() hide file format details
 */
public class Module {

    private String moduleId;
    private String moduleName;
    private String moduleCode;
    private int creditHours;
    private String leaderId;
    private String lecturerId;

    public Module() {
    }

    public Module(String moduleId, String moduleName, String moduleCode, int creditHours, String leaderId, String lecturerId) {
        this.moduleId = safe(moduleId);
        this.moduleName = safe(moduleName);
        this.moduleCode = safe(moduleCode);
        this.creditHours = creditHours;
        this.leaderId = safe(leaderId);
        this.lecturerId = safe(lecturerId);
    }

    // ---------------------------
    // File parsing / writing
    // ---------------------------

    public static Module fromFileLine(String line) {
        if (line == null) return null;
        String t = line.trim();
        if (t.isEmpty()) return null;

        String[] p = t.split("\\|", -1);
        if (p.length != 6) return null;

        String moduleId = p[0].trim();
        String moduleName = p[1].trim();
        String moduleCode = p[2].trim();

        int creditHours;
        try {
            creditHours = Integer.parseInt(p[3].trim());
        } catch (NumberFormatException e) {
            return null;
        }

        String leaderId = p[4].trim();
        String lecturerId = p[5].trim();

        return new Module(moduleId, moduleName, moduleCode, creditHours, leaderId, lecturerId);
    }

    public String toFileLine() {
        return safe(moduleId) + "|" +
               safe(moduleName) + "|" +
               safe(moduleCode) + "|" +
               creditHours + "|" +
               safe(leaderId) + "|" +
               safe(lecturerId);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // ---------------------------
    // Getters / Setters
    // ---------------------------

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = safe(moduleId);
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = safe(moduleName);
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = safe(moduleCode);
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = safe(leaderId);
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = safe(lecturerId);
    }

    @Override
    public String toString() {
        return moduleId + " - " + moduleCode + " (" + moduleName + ")";
    }
}
