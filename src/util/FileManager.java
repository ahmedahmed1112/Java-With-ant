package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public static List<String> readAll(String filePath) {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return lines;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try { br.close(); } catch (IOException ignored) {}
            }
        }
        return lines;
    }

    public static void append(String filePath, String line) {
        PrintWriter pw = null;
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            pw = new PrintWriter(new FileWriter(file, true));
            pw.println(line);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    public static void updateById(String filePath, String idKey, String newLine) {
        List<String> lines = readAll(filePath);
        List<String> out = new ArrayList<>();

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length == 0) continue;

            String key = p[0].trim();
            if (key.equalsIgnoreCase(idKey)) {
                out.add(newLine);
            } else {
                out.add(line);
            }
        }

        writeAll(filePath, out);
    }

    public static void deleteById(String filePath, String idKey) {
        List<String> lines = readAll(filePath);
        List<String> out = new ArrayList<>();

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] p = line.split("\\|");
            if (p.length == 0) continue;

            String key = p[0].trim();
            if (!key.equalsIgnoreCase(idKey)) {
                out.add(line);
            }
        }

        writeAll(filePath, out);
    }

    public static void writeAll(String filePath, List<String> lines) {
        PrintWriter pw = null;
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            pw = new PrintWriter(new FileWriter(file, false));
            for (String s : lines) {
                pw.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }
}
