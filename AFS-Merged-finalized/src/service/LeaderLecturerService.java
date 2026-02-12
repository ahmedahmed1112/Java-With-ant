package service;

import model.LeaderLecturerAssignment;

import java.io.*;
import java.util.*;

public class LeaderLecturerService {

    public enum AddResult {
        SUCCESS,
        DUPLICATE,
        LECTURER_ALREADY_ASSIGNED,
        LEADER_MAX_LECTURERS,
        INVALID_IDS,
        IO_ERROR
    }

    private static final String DEFAULT_PATH = "data/leader_lecturer.txt";
    private static final String DELIM = "|";
    private static final String SPLIT_REGEX = "\\|";
    private static final int MAX_LECTURERS_PER_LEADER = 3;

    private final String filePath;

    public LeaderLecturerService() {
        this(DEFAULT_PATH);
    }

    public LeaderLecturerService(String filePath) {
        this.filePath = (filePath == null || filePath.trim().isEmpty()) ? DEFAULT_PATH : filePath.trim();
        ensureFileExistsWithHeader();
    }

    public List<LeaderLecturerAssignment> getAll() {
        List<LeaderLecturerAssignment> list = new ArrayList<>();
        File f = new File(filePath);
        if (!f.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (isHeaderLine(line)) continue;

                String[] parts = line.split(SPLIT_REGEX);
                if (parts.length < 2) continue;

                String leaderId = safe(parts[0]);
                String lecturerId = safe(parts[1]);
                if (leaderId.isEmpty() || lecturerId.isEmpty()) continue;

                list.add(new LeaderLecturerAssignment(leaderId, lecturerId));
            }
        } catch (IOException ignored) {
        }
        return list;
    }

    public AddResult add(String leaderId, String lecturerId) {
        leaderId = safe(leaderId);
        lecturerId = safe(lecturerId);

        if (leaderId.isEmpty() || lecturerId.isEmpty()) {
            return AddResult.INVALID_IDS;
        }

        List<LeaderLecturerAssignment> list = getAll();

        for (LeaderLecturerAssignment a : list) {
            if (leaderId.equalsIgnoreCase(safe(a.getLeaderId()))
                    && lecturerId.equalsIgnoreCase(safe(a.getLecturerId()))) {
                return AddResult.DUPLICATE;
            }
        }

        for (LeaderLecturerAssignment a : list) {
            if (lecturerId.equalsIgnoreCase(safe(a.getLecturerId()))
                    && !leaderId.equalsIgnoreCase(safe(a.getLeaderId()))) {
                return AddResult.LECTURER_ALREADY_ASSIGNED;
            }
        }

        int count = 0;
        for (LeaderLecturerAssignment a : list) {
            if (leaderId.equalsIgnoreCase(safe(a.getLeaderId()))) count++;
        }
        if (count >= MAX_LECTURERS_PER_LEADER) {
            return AddResult.LEADER_MAX_LECTURERS;
        }

        list.add(new LeaderLecturerAssignment(leaderId, lecturerId));
        return writeAll(list) ? AddResult.SUCCESS : AddResult.IO_ERROR;
    }

    public boolean deletePair(String leaderId, String lecturerId) {
        leaderId = safe(leaderId);
        lecturerId = safe(lecturerId);

        if (leaderId.isEmpty() || lecturerId.isEmpty()) return false;

        List<LeaderLecturerAssignment> list = getAll();
        int before = list.size();

        Iterator<LeaderLecturerAssignment> it = list.iterator();
        while (it.hasNext()) {
            LeaderLecturerAssignment a = it.next();
            if (leaderId.equalsIgnoreCase(safe(a.getLeaderId()))
                    && lecturerId.equalsIgnoreCase(safe(a.getLecturerId()))) {
                it.remove();
            }
        }

        if (list.size() == before) return false;
        return writeAll(list);
    }

    private boolean writeAll(List<LeaderLecturerAssignment> list) {
        ensureFileExistsWithHeader();
        File f = new File(filePath);
        try (PrintWriter pw = new PrintWriter(new FileWriter(f, false))) {
            pw.println("LeaderID|LecturerID");
            for (LeaderLecturerAssignment a : list) {
                String lId = safe(a.getLeaderId());
                String tId = safe(a.getLecturerId());
                if (lId.isEmpty() || tId.isEmpty()) continue;
                pw.println(lId + DELIM + tId);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isHeaderLine(String line) {
        String s = line.replace(" ", "").trim();
        return s.equalsIgnoreCase("LeaderID|LecturerID")
                || s.equalsIgnoreCase("LeaderId|LecturerId")
                || s.equalsIgnoreCase("leaderid|lecturerid")
                || s.equalsIgnoreCase("leader|lecturer");
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private void ensureFileExistsWithHeader() {
        File f = new File(filePath);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        if (!f.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
                pw.println("LeaderID|LecturerID");
            } catch (IOException ignored) {
            }
            return;
        }

        if (f.length() == 0) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
                pw.println("LeaderID|LecturerID");
            } catch (IOException ignored) {
            }
        }
    }
}
