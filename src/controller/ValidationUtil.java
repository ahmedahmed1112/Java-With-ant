package controller;

public class ValidationUtil {
    public static boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".")
                && email.indexOf("@") < email.lastIndexOf(".");
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{10,11}");
    }

    public static boolean isValidAge(int age) {
        return age >= 18 && age <= 100;
    }

    public static boolean isValidMarks(double marks, double total) {
        return marks >= 0 && marks <= total;
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidName(String name) {
        return name != null && name.matches("[a-zA-Z\\s]+");
    }
}
