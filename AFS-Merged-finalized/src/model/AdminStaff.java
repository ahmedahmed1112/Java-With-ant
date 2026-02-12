package model;

public class AdminStaff extends User {

    public AdminStaff(String userId, String username, String password, String name,
                      String gender, String email, String phone, int age) {
        super(userId, username, password, name, gender, email, phone, age, "ADMIN");
    }

    // Backward-compatible constructor (old schema)
    public AdminStaff(String userId, String name, String username, String password) {
        super(userId, name, username, password, "ADMIN");
    }

    @Override
    public void editProfile() {
        // Admin profile editing handled by UI
    }
}
