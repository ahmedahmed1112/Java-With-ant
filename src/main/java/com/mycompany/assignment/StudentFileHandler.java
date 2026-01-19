package com.mycompany.assignment;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class StudentFileHandler {

    private static final String FILE_NAME = "students.txt";

    public void saveStudent(Student student) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(student.toFileString());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving student data.");
        }
    }
}
