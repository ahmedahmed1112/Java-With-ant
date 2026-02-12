package model;

public class ClassRecord {
    private String classId;
    private String className;
    private String moduleId;

    public ClassRecord(String classId, String className, String moduleId) {
        this.classId = classId;
        this.className = className;
        this.moduleId = moduleId;
    }

    public String getClassId() { return classId; }
    public String getClassName() { return className; }
    public String getModuleId() { return moduleId; }

    public void setClassId(String classId) { this.classId = classId; }
    public void setClassName(String className) { this.className = className; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }

    @Override
    public String toString() {
        return classId + "|" + className + "|" + moduleId;
    }
}
