package model;

public class AcademicLeader extends User {

    public AcademicLeader(String userId, String username, String password, String name,
                          String gender, String email, String phone, int age) {
        super(userId, username, password, name, gender, email, phone, age, "LEADER");
    }

    public AcademicLeader(String userId, String username, String password, String name,
                          String gender, String email, String phone, int age, String role) {
        super(userId, username, password, name, gender, email, phone, age, role);
    }

    // Backward-compatible constructor (old schema)
    public AcademicLeader(String userId, String name, String username, String password) {
        super(userId, name, username, password, "LEADER");
    }

    @Override
    public void editProfile() {
        // Leader profile editing handled by UI
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
