package service;

import model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static final String USERS_PATH = "data/users.txt";
    private static final String SPLIT_REGEX = "\\|";

    public List<User> getUsersByRole(String role) {
        String r = role == null ? "" : role.trim().toUpperCase();
        List<User> all = getAllUsers();
        List<User> out = new ArrayList<>();
        for (User u : all) {
            if (u.getRole() != null && u.getRole().trim().toUpperCase().equals(r)) {
                out.add(u);
            }
        }
        return out;
    }

    public User getById(String userId) {
        String id = userId == null ? "" : userId.trim();
        if (id.isEmpty()) return null;

        for (User u : getAllUsers()) {
            if (u.getUserId() != null && u.getUserId().trim().equalsIgnoreCase(id)) {
                return u;
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        File f = new File(USERS_PATH);
        if (!f.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.toLowerCase().startsWith("id|")) continue;

                String[] p = line.split(SPLIT_REGEX);

                // New schema: id|username|password|name|gender|email|phone|age|role
                if (p.length >= 9) {
                    String id = safe(p, 0);
                    String username = safe(p, 1);
                    String password = safe(p, 2);
                    String name = safe(p, 3);
                    String gender = safe(p, 4);
                    String email = safe(p, 5);
                    String phone = safe(p, 6);
                    int age = parseIntSafe(safe(p, 7), 0);
                    String role = safe(p, 8);

                    list.add(User.create(id, username, password, name, gender, email, phone, age, role));
                    continue;
                }

                // Old schema fallback: id|name|username|password|role
                if (p.length >= 5) {
                    String id = safe(p, 0);
                    String name = safe(p, 1);
                    String username = safe(p, 2);
                    String password = safe(p, 3);
                    String role = safe(p, 4);

                    list.add(User.create(id, username, password, name, "", "", "", 0, role));
                }
            }
        } catch (IOException ignored) {
        }

        return list;
    }

    private String safe(String[] arr, int idx) {
        if (arr == null || idx < 0 || idx >= arr.length || arr[idx] == null) return "";
        return arr[idx].trim();
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return def; }
    }
}
