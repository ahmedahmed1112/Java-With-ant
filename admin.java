import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

/**
 * admin.java (single file) - Admin Staff System - TXT storage
 * Modern dark theme + muted blue (eye friendly) + FIXED JTable white header/viewport issue (Nimbus)
 */
public class admin extends JFrame {

    // =========================================================
    // THEME (EYE-FRIENDLY DARK + MUTED BLUE)
    // =========================================================
    static class Theme {
        // backgrounds
        final Color APP_BG   = new Color(0x0E1015); // deep slate
        final Color SURFACE  = new Color(0x111827); // slate 900-ish
        final Color CARD     = new Color(0x151C2C); // slightly lighter
        final Color CARD_2   = new Color(0x101726); // alternate row
        final Color INPUT_BG = new Color(0x0F172A); // input background
        final Color HEADER   = new Color(0x0B1220); // top bar

        // borders
        final Color BORDER   = new Color(0x24324D);
        final Color BORDER_2 = new Color(0x1E2A40);

        // text
        final Color TEXT     = new Color(0xE5E7EB); // soft white
        final Color MUTED    = new Color(0xAAB4C5); // muted gray-blue
        final Color MUTED_2  = new Color(0x7F8BA6); // more muted

        // accent
        final Color BLUE     = new Color(0x3B82F6); // muted blue
        final Color BLUE_2   = new Color(0x2563EB); // deeper
        final Color BLUE_SOFT= new Color(0x3B82F6, true);

        // selection
        final Color SELECT_BG= new Color(0x1C2A4A);
        final Color SELECT_FG= new Color(0xE5E7EB);

        // fonts
        final Font H1        = new Font("SansSerif", Font.BOLD, 20);
        final Font H2        = new Font("SansSerif", Font.BOLD, 16);
        final Font H3        = new Font("SansSerif", Font.BOLD, 13);
        final Font BODY      = new Font("SansSerif", Font.PLAIN, 13);
        final Font BODY_BOLD = new Font("SansSerif", Font.BOLD, 13);
        final Font SMALL     = new Font("SansSerif", Font.PLAIN, 12);
    }

    private final Theme theme = new Theme();

    // =========================================================
    // LOOK & FEEL + GLOBAL UI OVERRIDES (fix Nimbus white areas)
    // =========================================================
    private static void setNimbus() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) { }
    }

    private void applyGlobalUIOverrides() {
        // Force Nimbus defaults for table + header + scrollpane to dark.
        UIManager.put("control", theme.SURFACE);
        UIManager.put("info", theme.SURFACE);
        UIManager.put("nimbusBase", theme.SURFACE);
        UIManager.put("nimbusBlueGrey", theme.SURFACE);
        UIManager.put("text", theme.TEXT);

        UIManager.put("Table.background", theme.CARD);
        UIManager.put("Table.foreground", theme.TEXT);
        UIManager.put("Table.selectionBackground", theme.SELECT_BG);
        UIManager.put("Table.selectionForeground", theme.SELECT_FG);
        UIManager.put("Table.gridColor", theme.BORDER);

        UIManager.put("TableHeader.background", theme.HEADER);
        UIManager.put("TableHeader.foreground", theme.TEXT);
        UIManager.put("TableHeader.font", theme.BODY_BOLD);

        UIManager.put("ScrollPane.background", theme.CARD);
        UIManager.put("Viewport.background", theme.CARD);

        UIManager.put("TextField.background", theme.INPUT_BG);
        UIManager.put("TextField.foreground", theme.TEXT);
        UIManager.put("TextField.caretForeground", theme.TEXT);

        UIManager.put("PasswordField.background", theme.INPUT_BG);
        UIManager.put("PasswordField.foreground", theme.TEXT);
        UIManager.put("PasswordField.caretForeground", theme.TEXT);

        UIManager.put("TextArea.background", theme.INPUT_BG);
        UIManager.put("TextArea.foreground", theme.TEXT);
        UIManager.put("TextArea.caretForeground", theme.TEXT);

        UIManager.put("ComboBox.background", theme.INPUT_BG);
        UIManager.put("ComboBox.foreground", theme.TEXT);

        UIManager.put("Panel.background", theme.APP_BG);

        // update existing components too
        SwingUtilities.updateComponentTreeUI(this);
    }

    // =========================================================
    // SIMPLE TOAST
    // =========================================================
    private void toast(String msg) {
        JWindow w = new JWindow(this);

        RoundedPanel p = new RoundedPanel(14);
        p.setColors(new Color(0x111827), theme.BORDER);
        p.setBorder(new EmptyBorder(10, 12, 10, 12));
        p.setLayout(new BorderLayout(10, 0));

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(theme.BLUE);
                g2.fillOval(0, 2, 10, 10);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(12, 12));

        JLabel l = new JLabel(msg);
        l.setForeground(theme.TEXT);
        l.setFont(theme.BODY_BOLD);

        p.add(dot, BorderLayout.WEST);
        p.add(l, BorderLayout.CENTER);

        w.setAlwaysOnTop(true);
        w.setBackground(new Color(0, 0, 0, 0));
        w.setContentPane(p);
        w.pack();

        int x = getX() + getWidth() - w.getWidth() - 22;
        int y = getY() + getHeight() - w.getHeight() - 54;
        w.setLocation(x, y);
        w.setVisible(true);

        Timer t = new Timer(2000, e -> w.dispose());
        t.setRepeats(false);
        t.start();
    }

    // =========================================================
    // CUSTOM PANELS / BUTTONS (student but pro)
    // =========================================================
    static class RoundedPanel extends JPanel {
        private final int radius;
        private Color fill;
        private Color border;

        RoundedPanel(int radius) { this.radius = radius; setOpaque(false); }
        void setColors(Color fill, Color border) { this.fill = fill; this.border = border; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (fill != null) {
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            }
            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class TopBar extends JPanel {
        TopBar() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(theme.HEADER);
            g2.fillRect(0, 0, getWidth(), getHeight());

            GradientPaint gp = new GradientPaint(0, getHeight()-3, theme.BLUE_2, 220, getHeight()-3, theme.HEADER);
            g2.setPaint(gp);
            g2.fillRect(0, getHeight()-3, 260, 3);

            g2.setColor(theme.BORDER_2);
            g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    abstract class ProButtonBase extends JButton {
        ProButtonBase(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFont(theme.BODY_BOLD);
            setBorder(new EmptyBorder(11, 16, 11, 16));
            setForeground(theme.TEXT);
        }

        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.height = Math.max(d.height, 42);
            return d;
        }

        protected void drawCenteredText(Graphics2D g2, Color color) {
            g2.setFont(getFont());
            g2.setColor(color);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
        }
    }

    class PrimaryButton extends ProButtonBase {
        PrimaryButton(String text) { super(text); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = 16;

            Color top = theme.BLUE;
            Color bot = theme.BLUE_2;
            if (!isEnabled()) { top = new Color(0x22304A); bot = new Color(0x1A253A); }
            else if (getModel().isPressed()) { top = new Color(0x1F4FAF); bot = new Color(0x183B7D); }

            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bot));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            g2.setColor(new Color(0x000000, true));
            g2.setColor(new Color(255,255,255,20));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);

            drawCenteredText(g2, new Color(0x0B1220));
            g2.dispose();
        }
    }

    class SecondaryButton extends ProButtonBase {
        SecondaryButton(String text) { super(text); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = 16;

            Color fill = theme.CARD;
            if (getModel().isRollover()) fill = new Color(0x18213A);
            if (getModel().isPressed()) fill = theme.SELECT_BG;

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            g2.setColor(getModel().isRollover() ? theme.BLUE : theme.BORDER);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);

            drawCenteredText(g2, theme.TEXT);
            g2.dispose();
        }
    }

    class SideButton extends JButton {
        private boolean active = false;

        SideButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setFont(theme.BODY_BOLD);
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(12, 14, 12, 14));
            setForeground(theme.TEXT);
        }

        public void setActive(boolean v) { active = v; repaint(); }

        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.height = Math.max(d.height, 46);
            return d;
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = 14;

            Color bg = active ? theme.SELECT_BG : new Color(0,0,0,0);
            if (!active && getModel().isRollover()) bg = new Color(0x131B2E);

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            if (active) {
                g2.setColor(theme.BLUE);
                g2.fillRoundRect(0, 0, 6, getHeight(), arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    class HintTextField extends JTextField {
        private final String hint;
        private boolean showingHint = true;

        HintTextField(String hint) {
            this.hint = hint;
            setText(hint);
            setForeground(theme.MUTED_2);
            setBackground(theme.INPUT_BG);
            setCaretColor(theme.TEXT);
            setBorder(new CompoundBorder(new LineBorder(theme.BORDER, 1, true), new EmptyBorder(10, 10, 10, 10)));

            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    if (showingHint) {
                        setText("");
                        setForeground(theme.TEXT);
                        showingHint = false;
                    }
                }
                @Override public void focusLost(FocusEvent e) {
                    if (getText().trim().isEmpty()) {
                        setText(hint);
                        setForeground(theme.MUTED_2);
                        showingHint = true;
                    }
                }
            });
        }

        String realText() { return showingHint ? "" : getText().trim(); }
    }

    // =========================================================
    // TXT STORAGE (same logic)
    // =========================================================
    static class DataStore {
        static final String DATA_DIR = "afs_data";
        static final String USERS_FILE = DATA_DIR + "/users.txt";
        static final String MAP_FILE = DATA_DIR + "/lecturer_to_leader.txt";
        static final String GRADING_FILE = DATA_DIR + "/grading.txt";
        static final String CLASSES_FILE = DATA_DIR + "/classes.txt";

        static void bootstrap() {
            try {
                Files.createDirectories(Paths.get(DATA_DIR));
                touch(USERS_FILE);
                touch(MAP_FILE);
                touch(GRADING_FILE);
                touch(CLASSES_FILE);

                List<User> users = loadUsers();
                if (users.isEmpty()) {
                    users.add(new User("admin", "admin123", "ADMIN_STAFF", "Default Admin"));
                    saveUsers(users);
                }

                if (Files.size(Paths.get(GRADING_FILE)) == 0) {
                    List<String> lines = new ArrayList<>();
                    lines.add("A|80|100");
                    lines.add("B|70|79");
                    lines.add("C|60|69");
                    lines.add("D|50|59");
                    lines.add("F|0|49");
                    Files.write(Paths.get(GRADING_FILE), lines);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static void touch(String path) throws IOException {
            Path p = Paths.get(path);
            if (!Files.exists(p)) Files.createFile(p);
        }

        static List<User> loadUsers() {
            List<User> out = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] p = line.split("\\|");
                    if (p.length >= 4) out.add(new User(p[0], p[1], p[2], p[3]));
                }
            } catch (Exception ignored) {}
            return out;
        }

        static void saveUsers(List<User> users) {
            List<String> lines = new ArrayList<>();
            for (User u : users) lines.add(u.username + "|" + u.password + "|" + u.role + "|" + u.fullName);
            try { Files.write(Paths.get(USERS_FILE), lines); } catch (Exception e) { e.printStackTrace(); }
        }

        static Map<String, String> loadMap() {
            Map<String, String> map = new LinkedHashMap<>();
            try (BufferedReader br = new BufferedReader(new FileReader(MAP_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] p = line.split("\\|");
                    if (p.length >= 2) map.put(p[0], p[1]);
                }
            } catch (Exception ignored) {}
            return map;
        }

        static void saveMap(Map<String, String> map) {
            List<String> lines = new ArrayList<>();
            for (String lect : map.keySet()) lines.add(lect + "|" + map.get(lect));
            try { Files.write(Paths.get(MAP_FILE), lines); } catch (Exception e) { e.printStackTrace(); }
        }

        static List<GradeRule> loadGrading() {
            List<GradeRule> rules = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(GRADING_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] p = line.split("\\|");
                    if (p.length >= 3) rules.add(new GradeRule(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2])));
                }
            } catch (Exception ignored) {}
            return rules;
        }

        static void saveGrading(List<GradeRule> rules) {
            List<String> lines = new ArrayList<>();
            for (GradeRule r : rules) lines.add(r.grade + "|" + r.min + "|" + r.max);
            try { Files.write(Paths.get(GRADING_FILE), lines); } catch (Exception e) { e.printStackTrace(); }
        }

        static List<AFSClass> loadClasses() {
            List<AFSClass> out = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(CLASSES_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] p = line.split("\\|");
                    if (p.length >= 3) out.add(new AFSClass(p[0], p[1], p[2]));
                }
            } catch (Exception ignored) {}
            return out;
        }

        static void saveClasses(List<AFSClass> list) {
            List<String> lines = new ArrayList<>();
            for (AFSClass c : list) lines.add(c.classId + "|" + c.className + "|" + c.semester);
            try { Files.write(Paths.get(CLASSES_FILE), lines); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // =========================================================
    // MODELS
    // =========================================================
    static class User {
        String username, password, role, fullName;
        User(String u, String p, String r, String n) { username=u; password=p; role=r; fullName=n; }
    }
    static class GradeRule {
        String grade; int min, max;
        GradeRule(String g, int mi, int ma) { grade=g; min=mi; max=ma; }
        boolean matches(int m) { return m >= min && m <= max; }
    }
    static class AFSClass {
        String classId, className, semester;
        AFSClass(String id, String name, String sem) { classId=id; className=name; semester=sem; }
    }

    // =========================================================
    // HELPERS
    // =========================================================
    private void err(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }
    private boolean confirm(String m) { return JOptionPane.showConfirmDialog(this, m, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION; }
    private static boolean blank(String s) { return s == null || s.trim().isEmpty(); }
    private static Integer toInt(String s) { try { return Integer.parseInt(s.trim()); } catch(Exception e) { return null; } }

    private JLabel statusLabel = new JLabel(" Ready");
    private JLabel timeLabel = new JLabel("");

    private void setStatus(String msg) { statusLabel.setText(" " + msg); }

    private void startClock() {
        Timer t = new Timer(1000, e -> timeLabel.setText(new SimpleDateFormat("HH:mm:ss").format(new Date())));
        t.setRepeats(true);
        t.start();
    }

    // =========================================================
    // DARK SCROLL (kills white viewport)
    // =========================================================
    private JScrollPane darkScroll(JComponent view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(new LineBorder(theme.BORDER, 1, true));
        sp.getViewport().setBackground(theme.CARD);
        sp.setBackground(theme.CARD);
        sp.setOpaque(true);

        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setBackground(theme.CARD);
        sp.getHorizontalScrollBar().setBackground(theme.CARD);
        return sp;
    }

    // =========================================================
    // TABLE STYLE (kills white header + makes readable)
    // =========================================================
    private void styleTable(JTable t) {
        t.setRowHeight(34);
        t.setShowHorizontalLines(false);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);

        t.setBackground(theme.CARD);
        t.setForeground(theme.TEXT);
        t.setSelectionBackground(theme.SELECT_BG);
        t.setSelectionForeground(theme.SELECT_FG);
        t.setGridColor(theme.BORDER);
        t.setFont(theme.BODY);

        JTableHeader th = t.getTableHeader();
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(100, 36));
        th.setOpaque(true);
        th.setBackground(theme.HEADER);
        th.setForeground(theme.TEXT);
        th.setFont(theme.BODY_BOLD);
        th.setBorder(new MatteBorder(0, 0, 1, 0, theme.BORDER_2));

        // HARD override header renderer (Nimbus sometimes forces white)
        DefaultTableCellRenderer headerR = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                          boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setOpaque(true);
                setBackground(theme.HEADER);
                setForeground(theme.TEXT);
                setFont(theme.BODY_BOLD);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setHorizontalAlignment(SwingConstants.LEFT);
                return this;
            }
        };
        th.setDefaultRenderer(headerR);

        DefaultTableCellRenderer cellR = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                          boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setOpaque(true);
                if (isSelected) setBackground(theme.SELECT_BG);
                else setBackground((row % 2 == 0) ? theme.CARD : theme.CARD_2);
                setForeground(theme.TEXT);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return this;
            }
        };

        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(cellR);
        }
    }

    // =========================================================
    // LOGIN
    // =========================================================
    private static User loginDialog() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(14, 14, 14, 14));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;

        JLabel title = new JLabel("Admin Login");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        p.add(title, gc);

        gc.gridwidth = 1;
        gc.gridy++;
        p.add(new JLabel("Username:"), gc);
        gc.gridx = 1;
        JTextField u = new JTextField(16);
        p.add(u, gc);

        gc.gridx = 0; gc.gridy++;
        p.add(new JLabel("Password:"), gc);
        gc.gridx = 1;
        JPasswordField pw = new JPasswordField(16);
        p.add(pw, gc);

        int ok = JOptionPane.showConfirmDialog(null, p, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return null;

        String user = u.getText().trim();
        String pass = new String(pw.getPassword());
        if (blank(user) || blank(pass)) return null;

        for (User x : DataStore.loadUsers()) {
            if (x.username.equals(user) && x.password.equals(pass) && "ADMIN_STAFF".equalsIgnoreCase(x.role)) {
                return x;
            }
        }
        return null;
    }

    // =========================================================
    // MAIN UI
    // =========================================================
    private final CardLayout card = new CardLayout();
    private final JPanel center = new JPanel(card);

    private SideButton sbHome, sbUsers, sbAssign, sbGrading, sbClasses;

    private JLabel statUsers = new JLabel("-");
    private JLabel statLect = new JLabel("-");
    private JLabel statLead = new JLabel("-");
    private JLabel statClasses = new JLabel("-");
    private JLabel statRules = new JLabel("-");

    private DefaultTableModel usersModel;
    private JTable usersTable;
    private TableRowSorter<DefaultTableModel> usersSorter;
    private HintTextField usersSearch = new HintTextField("Search username / name / role...");
    private final JTextField uUser = new JTextField();
    private final JTextField uName = new JTextField();
    private final JPasswordField uPass = new JPasswordField();
    private final JComboBox<String> uRole = new JComboBox<>(new String[]{"ADMIN_STAFF","ACADEMIC_LEADER","LECTURER","STUDENT"});

    private DefaultTableModel mapModel;
    private JTable mapTable;
    private TableRowSorter<DefaultTableModel> mapSorter;
    private HintTextField mapSearch = new HintTextField("Search lecturer / leader...");
    private final JComboBox<String> aLect = new JComboBox<>();
    private final JComboBox<String> aLead = new JComboBox<>();

    private DefaultTableModel gradeModel;
    private JTable gradeTable;
    private TableRowSorter<DefaultTableModel> gradeSorter;
    private HintTextField gradeSearch = new HintTextField("Search grade / min / max...");
    private final JTextField gGrade = new JTextField();
    private final JTextField gMin = new JTextField();
    private final JTextField gMax = new JTextField();
    private final JTextField gTest = new JTextField();
    private final JLabel gResult = new JLabel("Result: -");

    private DefaultTableModel classModel;
    private JTable classTable;
    private TableRowSorter<DefaultTableModel> classSorter;
    private HintTextField classSearch = new HintTextField("Search class id / name / semester...");
    private final JTextField cId = new JTextField();
    private final JTextField cName = new JTextField();
    private final JTextField cSem = new JTextField();

    public admin(User adminUser) {
        setTitle("AFS Admin Staff System");
        setSize(1200, 740);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(theme.APP_BG);
        setContentPane(root);

        // Must apply UI overrides AFTER frame created but BEFORE building tables
        applyGlobalUIOverrides();

        // TOP BAR
        TopBar top = new TopBar();
        top.setPreferredSize(new Dimension(100, 78));
        top.setLayout(new BorderLayout());
        top.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel("AFS Admin Staff");
        title.setFont(theme.H1);
        title.setForeground(theme.TEXT);

        JLabel sub = new JLabel("Logged in: " + adminUser.fullName + "  •  ADMIN_STAFF");
        sub.setFont(theme.SMALL);
        sub.setForeground(theme.MUTED);

        JPanel topLeft = new JPanel(new GridLayout(2,1));
        topLeft.setOpaque(false);
        topLeft.add(title);
        topLeft.add(sub);

        JPanel topRight = new JPanel(new GridLayout(1,2,10,0));
        topRight.setOpaque(false);

        SecondaryButton refresh = new SecondaryButton("Refresh");
        SecondaryButton logout = new SecondaryButton("Logout");
        refresh.addActionListener(e -> { refreshAll(); toast("Refreshed from txt files"); setStatus("Refreshed data"); });
        logout.addActionListener(e -> { dispose(); main(new String[]{}); });

        topRight.add(refresh);
        topRight.add(logout);

        top.add(topLeft, BorderLayout.WEST);
        top.add(topRight, BorderLayout.EAST);

        // SIDEBAR
        RoundedPanel sidebar = new RoundedPanel(18);
        sidebar.setColors(theme.SURFACE, theme.BORDER_2);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(new EmptyBorder(14, 12, 14, 12));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel nav = new JLabel("NAVIGATION");
        nav.setForeground(theme.MUTED);
        nav.setFont(new Font("SansSerif", Font.BOLD, 12));
        nav.setBorder(new EmptyBorder(0, 8, 12, 8));

        sbHome   = new SideButton("Dashboard");
        sbUsers  = new SideButton("Users");
        sbAssign = new SideButton("Assign Lecturer");
        sbGrading= new SideButton("Grading Rules");
        sbClasses= new SideButton("Classes");

        SecondaryButton exit = new SecondaryButton("Exit");
        exit.addActionListener(e -> System.exit(0));

        sidebar.add(nav);
        sidebar.add(sbHome);   sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(sbUsers);  sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(sbAssign); sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(sbGrading);sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(sbClasses);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(exit);

        // CENTER
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(14, 14, 14, 14));
        center.add(homeCard(), "HOME");
        center.add(usersCard(), "USERS");
        center.add(assignCard(), "ASSIGN");
        center.add(gradingCard(), "GRADING");
        center.add(classesCard(), "CLASSES");

        // STATUS BAR
        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(theme.HEADER);
        status.setBorder(new MatteBorder(1, 0, 0, 0, theme.BORDER_2));
        statusLabel.setForeground(theme.MUTED);
        statusLabel.setFont(theme.SMALL);
        timeLabel.setForeground(theme.MUTED);
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timeLabel.setFont(theme.SMALL);
        status.add(statusLabel, BorderLayout.WEST);
        status.add(timeLabel, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);
        root.add(sidebar, BorderLayout.WEST);
        root.add(center, BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);

        // NAV actions
        sbHome.addActionListener(e -> showPage("HOME"));
        sbUsers.addActionListener(e -> showPage("USERS"));
        sbAssign.addActionListener(e -> showPage("ASSIGN"));
        sbGrading.addActionListener(e -> showPage("GRADING"));
        sbClasses.addActionListener(e -> showPage("CLASSES"));

        // style inputs
        applyFieldStyles();

        refreshAll();
        showPage("HOME");
        startClock();
        toast("Welcome, " + adminUser.fullName);
    }

    private void applyFieldStyles() {
        styleField(uUser); styleField(uName); styleField(uPass);
        styleField(gGrade); styleField(gMin); styleField(gMax); styleField(gTest);
        styleField(cId); styleField(cName); styleField(cSem);

        styleCombo(uRole);
        styleCombo(aLect);
        styleCombo(aLead);

        gResult.setForeground(theme.TEXT);
        gResult.setFont(theme.BODY_BOLD);
    }

    private void styleField(JComponent c) {
        c.setFont(theme.BODY);
        c.setForeground(theme.TEXT);
        c.setBackground(theme.INPUT_BG);
        c.setBorder(new CompoundBorder(new LineBorder(theme.BORDER, 1, true), new EmptyBorder(10, 10, 10, 10)));
        if (c instanceof JTextField) ((JTextField)c).setCaretColor(theme.TEXT);
        if (c instanceof JPasswordField) ((JPasswordField)c).setCaretColor(theme.TEXT);
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(theme.BODY);
        cb.setForeground(theme.TEXT);
        cb.setBackground(theme.INPUT_BG);
        cb.setBorder(new LineBorder(theme.BORDER, 1, true));
    }

    private void showPage(String name) {
        sbHome.setActive("HOME".equals(name));
        sbUsers.setActive("USERS".equals(name));
        sbAssign.setActive("ASSIGN".equals(name));
        sbGrading.setActive("GRADING".equals(name));
        sbClasses.setActive("CLASSES".equals(name));
        card.show(center, name);
        setStatus("Opened: " + name);
    }

    private void bindSearchFilter(HintTextField search, TableRowSorter<DefaultTableModel> sorter) {
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void apply() {
                String q = search.realText();
                if (q.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q)));
            }
            @Override public void insertUpdate(DocumentEvent e) { apply(); }
            @Override public void removeUpdate(DocumentEvent e) { apply(); }
            @Override public void changedUpdate(DocumentEvent e) { apply(); }
        });
    }

    private JLabel label(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(theme.MUTED);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        return l;
    }

    // =========================================================
    // HOME
    // =========================================================
    private JPanel homeCard() {
        JPanel wrap = new JPanel(new BorderLayout(12, 12));
        wrap.setOpaque(false);

        JLabel h = new JLabel("Dashboard");
        h.setForeground(theme.TEXT);
        h.setFont(theme.H2);

        JLabel small = new JLabel("Overview from TXT files (afs_data)");
        small.setForeground(theme.MUTED);
        small.setFont(theme.SMALL);

        JPanel top = new JPanel(new GridLayout(2,1));
        top.setOpaque(false);
        top.add(h);
        top.add(small);

        JPanel grid = new JPanel(new GridLayout(2, 3, 12, 12));
        grid.setOpaque(false);

        grid.add(statCard("Total Users", statUsers, "All roles"));
        grid.add(statCard("Lecturers", statLect, "Role: LECTURER"));
        grid.add(statCard("Academic Leaders", statLead, "Role: ACADEMIC_LEADER"));
        grid.add(statCard("Classes", statClasses, "classes.txt"));
        grid.add(statCard("Grade Rules", statRules, "grading.txt"));
        grid.add(infoCard());

        wrap.add(top, BorderLayout.NORTH);
        wrap.add(grid, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel statCard(String title, JLabel value, String sub) {
        RoundedPanel p = new RoundedPanel(18);
        p.setColors(theme.CARD, theme.BORDER_2);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));
        p.setLayout(new BorderLayout(8, 8));

        JLabel t = new JLabel(title);
        t.setForeground(theme.MUTED);
        t.setFont(new Font("SansSerif", Font.BOLD, 12));

        value.setForeground(theme.TEXT);
        value.setFont(new Font("SansSerif", Font.BOLD, 28));

        JLabel s = new JLabel(sub);
        s.setForeground(theme.MUTED_2);
        s.setFont(theme.SMALL);

        JPanel accent = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(theme.BLUE);
                g2.fillRoundRect(0, 0, 70, 4, 8, 8);
                g2.dispose();
            }
        };
        accent.setOpaque(false);
        accent.setPreferredSize(new Dimension(70, 6));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(t, BorderLayout.NORTH);
        header.add(accent, BorderLayout.SOUTH);

        p.add(header, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        p.add(s, BorderLayout.SOUTH);
        return p;
    }

    private JPanel infoCard() {
        RoundedPanel p = new RoundedPanel(18);
        p.setColors(theme.CARD, theme.BORDER_2);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));
        p.setLayout(new BorderLayout(10, 10));

        JLabel t = new JLabel("Admin Notes");
        t.setForeground(theme.MUTED);
        t.setFont(new Font("SansSerif", Font.BOLD, 12));

        JTextArea area = new JTextArea();
        area.setText(
            "• Users: Create accounts for all roles (Lecturer, Leader, Student)\n" +
            "• Assign: Link Lecturer → Academic Leader\n" +
            "• Grading: Manage grade rules (mark → grade)\n" +
            "• Classes: Add / update / delete classes\n" +
            "• Data: Stored in folder: afs_data\n"
        );
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(true);
        area.setBackground(theme.INPUT_BG);
        area.setForeground(theme.TEXT);
        area.setFont(theme.SMALL);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane sp = darkScroll(area);
        p.add(t, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // =========================================================
    // USERS
    // =========================================================
    private JPanel usersCard() {
        RoundedPanel root = new RoundedPanel(18);
        root.setColors(theme.CARD, theme.BORDER_2);
        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Users Management");
        title.setFont(theme.H2);
        title.setForeground(theme.TEXT);

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);
        usersSearch.setPreferredSize(new Dimension(360, 40));
        top.add(usersSearch, BorderLayout.EAST);

        usersModel = new DefaultTableModel(new Object[]{"Username", "Full Name", "Role"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = new JTable(usersModel);
        styleTable(usersTable);
        usersSorter = new TableRowSorter<>(usersModel);
        usersTable.setRowSorter(usersSorter);
        bindSearchFilter(usersSearch, usersSorter);

        JScrollPane tableScroll = darkScroll(usersTable);

        RoundedPanel formCard = new RoundedPanel(18);
        formCard.setColors(theme.SURFACE, theme.BORDER_2);
        formCard.setLayout(new BorderLayout(10, 10));
        formCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        formCard.setPreferredSize(new Dimension(430, 0));

        JLabel fTitle = new JLabel("User Form");
        fTitle.setForeground(theme.TEXT);
        fTitle.setFont(theme.H3);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx=0; gc.gridy=0; gc.gridwidth=2;
        form.add(fTitle, gc);
        gc.gridwidth=1;

        gc.gridy++; gc.gridx=0; form.add(label("Username"), gc);
        gc.gridx=1; form.add(uUser, gc);

        gc.gridy++; gc.gridx=0; form.add(label("Full Name"), gc);
        gc.gridx=1; form.add(uName, gc);

        gc.gridy++; gc.gridx=0; form.add(label("Password"), gc);
        gc.gridx=1; form.add(uPass, gc);

        gc.gridy++; gc.gridx=0; form.add(label("Role"), gc);
        gc.gridx=1; form.add(uRole, gc);

        JPanel btns = new JPanel(new GridLayout(1,4,10,10));
        btns.setOpaque(false);
        PrimaryButton add = new PrimaryButton("Add");
        PrimaryButton upd = new PrimaryButton("Update");
        SecondaryButton del = new SecondaryButton("Delete");
        SecondaryButton clr = new SecondaryButton("Clear");

        btns.add(add); btns.add(upd); btns.add(del); btns.add(clr);

        formCard.add(form, BorderLayout.CENTER);
        formCard.add(btns, BorderLayout.SOUTH);

        JPanel mid = new JPanel(new BorderLayout(12, 12));
        mid.setOpaque(false);
        mid.add(tableScroll, BorderLayout.CENTER);
        mid.add(formCard, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);
        root.add(mid, BorderLayout.CENTER);

        usersTable.getSelectionModel().addListSelectionListener(e -> usersFillFromTable());
        add.addActionListener(e -> usersAdd());
        upd.addActionListener(e -> usersUpdate());
        del.addActionListener(e -> usersDelete());
        clr.addActionListener(e -> usersClear());

        return root;
    }

    private void loadUsersTable() {
        usersModel.setRowCount(0);
        for (User u : DataStore.loadUsers()) usersModel.addRow(new Object[]{u.username, u.fullName, u.role});
        usersClear();
    }

    private void usersFillFromTable() {
        int viewRow = usersTable.getSelectedRow();
        if (viewRow < 0) return;
        int row = usersTable.convertRowIndexToModel(viewRow);

        String username = String.valueOf(usersModel.getValueAt(row, 0));
        for (User u : DataStore.loadUsers()) {
            if (u.username.equals(username)) {
                uUser.setText(u.username);
                uName.setText(u.fullName);
                uPass.setText(u.password);
                uRole.setSelectedItem(u.role);
                uUser.setEnabled(false);
                setStatus("Selected user: " + username);
                break;
            }
        }
    }

    private void usersClear() {
        uUser.setText("");
        uName.setText("");
        uPass.setText("");
        uRole.setSelectedIndex(0);
        uUser.setEnabled(true);
        usersTable.clearSelection();
        setStatus("Ready");
    }

    private void usersAdd() {
        String username = uUser.getText().trim();
        String name = uName.getText().trim();
        String pass = new String(uPass.getPassword());
        String role = String.valueOf(uRole.getSelectedItem());

        if (blank(username) || blank(name) || blank(pass)) { err("All fields required."); return; }

        List<User> users = DataStore.loadUsers();
        for (User u : users) if (u.username.equalsIgnoreCase(username)) { err("Username already exists."); return; }

        users.add(new User(username, pass, role, name));
        DataStore.saveUsers(users);
        refreshAll();
        toast("User added: " + username);
        setStatus("User added: " + username);
    }

    private void usersUpdate() {
        if (uUser.isEnabled()) { err("Select a user row first."); return; }

        String username = uUser.getText().trim();
        String name = uName.getText().trim();
        String pass = new String(uPass.getPassword());
        String role = String.valueOf(uRole.getSelectedItem());

        if (blank(name) || blank(pass)) { err("Name/Password cannot be empty."); return; }

        List<User> users = DataStore.loadUsers();
        boolean found = false;
        for (User u : users) {
            if (u.username.equals(username)) {
                u.fullName = name;
                u.password = pass;
                u.role = role;
                found = true;
                break;
            }
        }
        if (!found) { err("User not found."); return; }

        DataStore.saveUsers(users);
        refreshAll();
        toast("User updated: " + username);
        setStatus("User updated: " + username);
    }

    private void usersDelete() {
        int viewRow = usersTable.getSelectedRow();
        if (viewRow < 0) { err("Select a user row to delete."); return; }
        int row = usersTable.convertRowIndexToModel(viewRow);

        String username = String.valueOf(usersModel.getValueAt(row, 0));
        if ("admin".equalsIgnoreCase(username)) { err("Cannot delete default admin."); return; }
        if (!confirm("Delete user '" + username + "'?")) return;

        List<User> users = DataStore.loadUsers();
        List<User> newList = new ArrayList<>();
        for (User u : users) if (!u.username.equals(username)) newList.add(u);

        Map<String, String> map = DataStore.loadMap();
        map.remove(username);
        DataStore.saveMap(map);

        DataStore.saveUsers(newList);
        refreshAll();
        toast("User deleted: " + username);
        setStatus("User deleted: " + username);
    }

    // =========================================================
    // ASSIGN
    // =========================================================
    private JPanel assignCard() {
        RoundedPanel root = new RoundedPanel(18);
        root.setColors(theme.CARD, theme.BORDER_2);
        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Assign Lecturer → Academic Leader");
        title.setFont(theme.H2);
        title.setForeground(theme.TEXT);

        JPanel top = new JPanel(new BorderLayout(10,10));
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);
        mapSearch.setPreferredSize(new Dimension(360, 40));
        top.add(mapSearch, BorderLayout.EAST);

        RoundedPanel left = new RoundedPanel(18);
        left.setColors(theme.SURFACE, theme.BORDER_2);
        left.setBorder(new EmptyBorder(12, 12, 12, 12));
        left.setLayout(new GridBagLayout());
        left.setPreferredSize(new Dimension(440, 0));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 6, 8, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel f = new JLabel("Assignment Form");
        f.setFont(theme.H3);
        f.setForeground(theme.TEXT);

        gc.gridx=0; gc.gridy=0; gc.gridwidth=2;
        left.add(f, gc);
        gc.gridwidth=1;

        gc.gridy++; gc.gridx=0; left.add(label("Lecturer"), gc);
        gc.gridx=1; left.add(aLect, gc);

        gc.gridy++; gc.gridx=0; left.add(label("Academic Leader"), gc);
        gc.gridx=1; left.add(aLead, gc);

        PrimaryButton assign = new PrimaryButton("Assign / Update");
        SecondaryButton remove = new SecondaryButton("Remove Selected");

        gc.gridy++; gc.gridx=0; gc.gridwidth=2;
        JPanel row = new JPanel(new GridLayout(1,2,10,10));
        row.setOpaque(false);
        row.add(assign);
        row.add(remove);
        left.add(row, gc);

        mapModel = new DefaultTableModel(new Object[]{"Lecturer", "Academic Leader"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        mapTable = new JTable(mapModel);
        styleTable(mapTable);
        mapSorter = new TableRowSorter<>(mapModel);
        mapTable.setRowSorter(mapSorter);
        bindSearchFilter(mapSearch, mapSorter);

        JScrollPane tableScroll = darkScroll(mapTable);

        JPanel mid = new JPanel(new BorderLayout(12,12));
        mid.setOpaque(false);
        mid.add(left, BorderLayout.WEST);
        mid.add(tableScroll, BorderLayout.CENTER);

        root.add(top, BorderLayout.NORTH);
        root.add(mid, BorderLayout.CENTER);

        assign.addActionListener(e -> doAssign());
        remove.addActionListener(e -> doRemoveMap());

        return root;
    }

    private void refreshAssignLists() {
        aLect.removeAllItems();
        aLead.removeAllItems();
        for (User u : DataStore.loadUsers()) {
            if ("LECTURER".equalsIgnoreCase(u.role)) aLect.addItem(u.username);
            if ("ACADEMIC_LEADER".equalsIgnoreCase(u.role)) aLead.addItem(u.username);
        }
    }

    private void loadMapTable() {
        mapModel.setRowCount(0);
        Map<String, String> map = DataStore.loadMap();
        for (String lect : map.keySet()) mapModel.addRow(new Object[]{lect, map.get(lect)});
    }

    private void doAssign() {
        String lect = (String) aLect.getSelectedItem();
        String lead = (String) aLead.getSelectedItem();
        if (lect == null || lead == null) { err("Create Lecturer + Academic Leader first in Users."); return; }

        Map<String, String> map = DataStore.loadMap();
        map.put(lect, lead);
        DataStore.saveMap(map);

        loadMapTable();
        toast("Assigned: " + lect + " → " + lead);
        setStatus("Assigned: " + lect + " → " + lead);
    }

    private void doRemoveMap() {
        int viewRow = mapTable.getSelectedRow();
        if (viewRow < 0) { err("Select a mapping row to remove."); return; }
        int row = mapTable.convertRowIndexToModel(viewRow);

        String lect = String.valueOf(mapModel.getValueAt(row, 0));
        if (!confirm("Remove mapping for '" + lect + "'?")) return;

        Map<String, String> map = DataStore.loadMap();
        map.remove(lect);
        DataStore.saveMap(map);

        loadMapTable();
        toast("Mapping removed for: " + lect);
        setStatus("Mapping removed for: " + lect);
    }

    // =========================================================
    // GRADING
    // =========================================================
    private JPanel gradingCard() {
        RoundedPanel root = new RoundedPanel(18);
        root.setColors(theme.CARD, theme.BORDER_2);
        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Grading Rules");
        title.setFont(theme.H2);
        title.setForeground(theme.TEXT);

        JPanel top = new JPanel(new BorderLayout(10,10));
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);
        gradeSearch.setPreferredSize(new Dimension(360, 40));
        top.add(gradeSearch, BorderLayout.EAST);

        gradeModel = new DefaultTableModel(new Object[]{"Grade", "Min", "Max"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        gradeTable = new JTable(gradeModel);
        styleTable(gradeTable);
        gradeSorter = new TableRowSorter<>(gradeModel);
        gradeTable.setRowSorter(gradeSorter);
        bindSearchFilter(gradeSearch, gradeSorter);

        JScrollPane tableScroll = darkScroll(gradeTable);

        RoundedPanel right = new RoundedPanel(18);
        right.setColors(theme.SURFACE, theme.BORDER_2);
        right.setBorder(new EmptyBorder(12,12,12,12));
        right.setPreferredSize(new Dimension(440, 0));
        right.setLayout(new BorderLayout(10,10));

        JLabel f = new JLabel("Rule Form");
        f.setFont(theme.H3);
        f.setForeground(theme.TEXT);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx=0; gc.gridy=0; gc.gridwidth=2;
        form.add(f, gc);
        gc.gridwidth=1;

        gc.gridy++; gc.gridx=0; form.add(label("Grade (A/B/...)"), gc);
        gc.gridx=1; form.add(gGrade, gc);

        gc.gridy++; gc.gridx=0; form.add(label("Min"), gc);
        gc.gridx=1; form.add(gMin, gc);

        gc.gridy++; gc.gridx=0; form.add(label("Max"), gc);
        gc.gridx=1; form.add(gMax, gc);

        JPanel btns = new JPanel(new GridLayout(1,3,10,10));
        btns.setOpaque(false);
        PrimaryButton add = new PrimaryButton("Add");
        PrimaryButton upd = new PrimaryButton("Update");
        SecondaryButton del = new SecondaryButton("Delete");
        btns.add(add); btns.add(upd); btns.add(del);

        RoundedPanel test = new RoundedPanel(16);
        test.setColors(theme.CARD, theme.BORDER_2);
        test.setBorder(new EmptyBorder(10,10,10,10));
        test.setLayout(new GridBagLayout());

        GridBagConstraints tc = new GridBagConstraints();
        tc.insets = new Insets(6,6,6,6);
        tc.fill = GridBagConstraints.HORIZONTAL;

        tc.gridx=0; tc.gridy=0; tc.gridwidth=2;
        JLabel tt = new JLabel("Test Mark");
        tt.setForeground(theme.MUTED);
        tt.setFont(new Font("SansSerif", Font.BOLD, 12));
        test.add(tt, tc);
        tc.gridwidth=1;

        tc.gridy++; tc.gridx=0; test.add(label("Mark (0-100)"), tc);
        tc.gridx=1; test.add(gTest, tc);

        tc.gridy++; tc.gridx=0; tc.gridwidth=2;
        PrimaryButton testBtn = new PrimaryButton("Test");
        test.add(testBtn, tc);

        tc.gridy++;
        gResult.setText("Result: -");
        test.add(gResult, tc);

        right.add(form, BorderLayout.NORTH);
        right.add(btns, BorderLayout.CENTER);
        right.add(test, BorderLayout.SOUTH);

        JPanel mid = new JPanel(new BorderLayout(12,12));
        mid.setOpaque(false);
        mid.add(tableScroll, BorderLayout.CENTER);
        mid.add(right, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);
        root.add(mid, BorderLayout.CENTER);

        gradeTable.getSelectionModel().addListSelectionListener(e -> gradeFill());
        add.addActionListener(e -> gradeAdd());
        upd.addActionListener(e -> gradeUpdate());
        del.addActionListener(e -> gradeDelete());
        testBtn.addActionListener(e -> gradeTest());

        return root;
    }

    private void loadGradeTable() {
        gradeModel.setRowCount(0);
        for (GradeRule r : DataStore.loadGrading()) gradeModel.addRow(new Object[]{r.grade, r.min, r.max});
        gradeClear();
    }

    private void gradeFill() {
        int viewRow = gradeTable.getSelectedRow();
        if (viewRow < 0) return;
        int row = gradeTable.convertRowIndexToModel(viewRow);
        gGrade.setText(String.valueOf(gradeModel.getValueAt(row, 0)));
        gMin.setText(String.valueOf(gradeModel.getValueAt(row, 1)));
        gMax.setText(String.valueOf(gradeModel.getValueAt(row, 2)));
        gGrade.setEnabled(false);
        setStatus("Selected rule: " + gGrade.getText().trim());
    }

    private void gradeClear() {
        gGrade.setText("");
        gMin.setText("");
        gMax.setText("");
        gGrade.setEnabled(true);
        gradeTable.clearSelection();
        gResult.setText("Result: -");
    }

    private void gradeAdd() {
        String grade = gGrade.getText().trim().toUpperCase();
        Integer min = toInt(gMin.getText());
        Integer max = toInt(gMax.getText());

        if (blank(grade) || min == null || max == null) { err("Fill grade/min/max correctly."); return; }
        if (min < 0 || max > 100 || min > max) { err("Min/Max must be 0..100 and Min<=Max."); return; }

        List<GradeRule> rules = DataStore.loadGrading();
        for (GradeRule r : rules) if (r.grade.equalsIgnoreCase(grade)) { err("Grade exists. Use update."); return; }

        rules.add(new GradeRule(grade, min, max));
        DataStore.saveGrading(rules);
        refreshAll();
        toast("Rule added: " + grade);
    }

    private void gradeUpdate() {
        if (gGrade.isEnabled()) { err("Select a rule row first."); return; }
        String grade = gGrade.getText().trim().toUpperCase();
        Integer min = toInt(gMin.getText());
        Integer max = toInt(gMax.getText());

        if (min == null || max == null) { err("Min/Max must be numbers."); return; }
        if (min < 0 || max > 100 || min > max) { err("Min/Max must be 0..100 and Min<=Max."); return; }

        List<GradeRule> rules = DataStore.loadGrading();
        boolean found = false;
        for (GradeRule r : rules) {
            if (r.grade.equalsIgnoreCase(grade)) { r.min=min; r.max=max; found=true; break; }
        }
        if (!found) { err("Rule not found."); return; }

        DataStore.saveGrading(rules);
        refreshAll();
        toast("Rule updated: " + grade);
    }

    private void gradeDelete() {
        int viewRow = gradeTable.getSelectedRow();
        if (viewRow < 0) { err("Select a rule row to delete."); return; }
        int row = gradeTable.convertRowIndexToModel(viewRow);

        String grade = String.valueOf(gradeModel.getValueAt(row, 0));
        if (!confirm("Delete rule '" + grade + "'?")) return;

        List<GradeRule> rules = DataStore.loadGrading();
        List<GradeRule> newList = new ArrayList<>();
        for (GradeRule r : rules) if (!r.grade.equalsIgnoreCase(grade)) newList.add(r);

        DataStore.saveGrading(newList);
        refreshAll();
        toast("Rule deleted: " + grade);
    }

    private void gradeTest() {
        Integer mark = toInt(gTest.getText());
        if (mark == null || mark < 0 || mark > 100) { err("Enter mark 0..100"); return; }

        for (GradeRule r : DataStore.loadGrading()) {
            if (r.matches(mark)) {
                gResult.setText("Result: " + mark + " => " + r.grade);
                toast("Mark " + mark + " => " + r.grade);
                return;
            }
        }
        gResult.setText("Result: No rule matched.");
        toast("No rule matched " + mark);
    }

    // =========================================================
    // CLASSES
    // =========================================================
    private JPanel classesCard() {
        RoundedPanel root = new RoundedPanel(18);
        root.setColors(theme.CARD, theme.BORDER_2);
        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Classes Management");
        title.setFont(theme.H2);
        title.setForeground(theme.TEXT);

        JPanel top = new JPanel(new BorderLayout(10,10));
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);
        classSearch.setPreferredSize(new Dimension(360, 40));
        top.add(classSearch, BorderLayout.EAST);

        classModel = new DefaultTableModel(new Object[]{"Class ID", "Class Name", "Semester"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        classTable = new JTable(classModel);
        styleTable(classTable);
        classSorter = new TableRowSorter<>(classModel);
        classTable.setRowSorter(classSorter);
        bindSearchFilter(classSearch, classSorter);

        JScrollPane tableScroll = darkScroll(classTable);

        RoundedPanel right = new RoundedPanel(18);
        right.setColors(theme.SURFACE, theme.BORDER_2);
        right.setBorder(new EmptyBorder(12,12,12,12));
        right.setPreferredSize(new Dimension(440, 0));
        right.setLayout(new BorderLayout(10,10));

        JLabel f = new JLabel("Class Form");
        f.setFont(theme.H3);
        f.setForeground(theme.TEXT);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx=0; gc.gridy=0; gc.gridwidth=2;
        form.add(f, gc);
        gc.gridwidth=1;

        gc.gridy++; gc.gridx=0; form.add(label("Class ID"), gc);
        gc.gridx=1; form.add(cId, gc);

        gc.gridy++; gc.gridx=0; form.add(label("Class Name"), gc);
        gc.gridx=1; form.add(cName, gc);

        gc.gridy++; gc.gridx=0; form.add(label("Semester"), gc);
        gc.gridx=1; form.add(cSem, gc);

        JPanel btns = new JPanel(new GridLayout(1,4,10,10));
        btns.setOpaque(false);
        PrimaryButton add = new PrimaryButton("Add");
        PrimaryButton upd = new PrimaryButton("Update");
        SecondaryButton del = new SecondaryButton("Delete");
        SecondaryButton clr = new SecondaryButton("Clear");

        btns.add(add); btns.add(upd); btns.add(del); btns.add(clr);

        right.add(form, BorderLayout.CENTER);
        right.add(btns, BorderLayout.SOUTH);

        JPanel mid = new JPanel(new BorderLayout(12,12));
        mid.setOpaque(false);
        mid.add(tableScroll, BorderLayout.CENTER);
        mid.add(right, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);
        root.add(mid, BorderLayout.CENTER);

        classTable.getSelectionModel().addListSelectionListener(e -> classFill());
        add.addActionListener(e -> classAdd());
        upd.addActionListener(e -> classUpdate());
        del.addActionListener(e -> classDelete());
        clr.addActionListener(e -> classClear());

        return root;
    }

    private void loadClassTable() {
        classModel.setRowCount(0);
        for (AFSClass c : DataStore.loadClasses()) classModel.addRow(new Object[]{c.classId, c.className, c.semester});
        classClear();
    }

    private void classFill() {
        int viewRow = classTable.getSelectedRow();
        if (viewRow < 0) return;
        int row = classTable.convertRowIndexToModel(viewRow);
        cId.setText(String.valueOf(classModel.getValueAt(row, 0)));
        cName.setText(String.valueOf(classModel.getValueAt(row, 1)));
        cSem.setText(String.valueOf(classModel.getValueAt(row, 2)));
        cId.setEnabled(false);
        setStatus("Selected class: " + cId.getText().trim());
    }

    private void classClear() {
        cId.setText("");
        cName.setText("");
        cSem.setText("");
        cId.setEnabled(true);
        classTable.clearSelection();
    }

    private void classAdd() {
        String id = cId.getText().trim();
        String name = cName.getText().trim();
        String sem = cSem.getText().trim();
        if (blank(id) || blank(name) || blank(sem)) { err("All fields required."); return; }

        List<AFSClass> list = DataStore.loadClasses();
        for (AFSClass c : list) if (c.classId.equalsIgnoreCase(id)) { err("Class ID exists."); return; }

        list.add(new AFSClass(id, name, sem));
        DataStore.saveClasses(list);
        refreshAll();
        toast("Class added: " + id);
    }

    private void classUpdate() {
        if (cId.isEnabled()) { err("Select a class row first."); return; }
        String id = cId.getText().trim();
        String name = cName.getText().trim();
        String sem = cSem.getText().trim();
        if (blank(name) || blank(sem)) { err("Name/Semester cannot be empty."); return; }

        List<AFSClass> list = DataStore.loadClasses();
        boolean found = false;
        for (AFSClass c : list) {
            if (c.classId.equalsIgnoreCase(id)) { c.className=name; c.semester=sem; found=true; break; }
        }
        if (!found) { err("Class not found."); return; }

        DataStore.saveClasses(list);
        refreshAll();
        toast("Class updated: " + id);
    }

    private void classDelete() {
        int viewRow = classTable.getSelectedRow();
        if (viewRow < 0) { err("Select a class row to delete."); return; }
        int row = classTable.convertRowIndexToModel(viewRow);

        String id = String.valueOf(classModel.getValueAt(row, 0));
        if (!confirm("Delete class '" + id + "'?")) return;

        List<AFSClass> list = DataStore.loadClasses();
        List<AFSClass> newList = new ArrayList<>();
        for (AFSClass c : list) if (!c.classId.equalsIgnoreCase(id)) newList.add(c);

        DataStore.saveClasses(newList);
        refreshAll();
        toast("Class deleted: " + id);
    }

    // =========================================================
    // REFRESH + DASHBOARD
    // =========================================================
    private void refreshAll() {
        if (usersModel != null) loadUsersTable();
        refreshAssignLists();
        if (mapModel != null) loadMapTable();
        if (gradeModel != null) loadGradeTable();
        if (classModel != null) loadClassTable();
        updateDashboardStats();
    }

    private void updateDashboardStats() {
        List<User> users = DataStore.loadUsers();
        int total = users.size();
        int lect = 0, lead = 0;
        for (User u : users) {
            if ("LECTURER".equalsIgnoreCase(u.role)) lect++;
            if ("ACADEMIC_LEADER".equalsIgnoreCase(u.role)) lead++;
        }
        int cls = DataStore.loadClasses().size();
        int rules = DataStore.loadGrading().size();

        statUsers.setText(String.valueOf(total));
        statLect.setText(String.valueOf(lect));
        statLead.setText(String.valueOf(lead));
        statClasses.setText(String.valueOf(cls));
        statRules.setText(String.valueOf(rules));
    }

    // =========================================================
    // MAIN
    // =========================================================
    public static void main(String[] args) {
        setNimbus();
        DataStore.bootstrap();

        User adminUser = loginDialog();
        if (adminUser == null) {
            JOptionPane.showMessageDialog(null, "Login failed.\nDefault: admin / admin123",
                    "Exit", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> new admin(adminUser).setVisible(true));
    }
}
