package model;

public class GradingRule {
    private String grade;
    private int min;
    private int max;

    public GradingRule(String grade, int min, int max) {
        this.grade = grade;
        this.min = min;
        this.max = max;
    }

    public String getGrade() { return grade; }
    public int getMin() { return min; }
    public int getMax() { return max; }

    public void setGrade(String grade) { this.grade = grade; }
    public void setMin(int min) { this.min = min; }
    public void setMax(int max) { this.max = max; }

    @Override
    public String toString() {
        return grade + "|" + min + "|" + max;
    }
}
