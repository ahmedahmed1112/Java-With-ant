package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UIUtils {

    public static Font font(int size, int style) {
        return new Font("Segoe UI", style, size);
    }

    public static JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(Theme.CARD);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.setOpaque(true);
        return p;
    }

    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.TEXT);
        l.setFont(font(18, Font.BOLD));
        return l;
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.MUTED);
        l.setFont(font(12, Font.PLAIN));
        return l;
    }

    public static JTextField modernTextField() {
        JTextField t = new JTextField();
        t.setFont(font(13, Font.PLAIN));
        t.setForeground(Theme.TEXT);
        t.setBackground(Theme.INPUT_BG);
        t.setCaretColor(Theme.TEXT);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return t;
    }

    public static JPasswordField modernPasswordField() {
        JPasswordField t = new JPasswordField();
        t.setFont(font(13, Font.PLAIN));
        t.setForeground(Theme.TEXT);
        t.setBackground(Theme.INPUT_BG);
        t.setCaretColor(Theme.TEXT);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return t;
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(font(13, Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setBackground(Theme.PRIMARY);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(font(13, Font.BOLD));
        b.setForeground(Theme.TEXT);
        b.setBackground(Theme.SIDEBAR);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        b.setFont(font(13, Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setBackground(Theme.DANGER);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static void applyTableStyle(JTable table) {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFont(font(13, Font.PLAIN));
        table.setBackground(new Color(18, 22, 30));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 70, 90));
        table.setSelectionBackground(new Color(70, 130, 180));
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                lbl.setOpaque(true);
                lbl.setBackground(new Color(30, 35, 50));
                lbl.setForeground(Color.WHITE);
                lbl.setFont(font(13, Font.BOLD));
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                return lbl;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                if (isSelected) {
                    lbl.setBackground(new Color(70, 130, 180));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(row % 2 == 0 ? new Color(18, 22, 30) : new Color(24, 29, 40));
                    lbl.setForeground(column == 0 ? new Color(200, 220, 255) : Color.WHITE);
                }
                return lbl;
            }
        });
    }

    public static void styleScrollPane(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));
        sp.getViewport().setBackground(Theme.CARD);

        JScrollBar v = sp.getVerticalScrollBar();
        v.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = Theme.BORDER;
                this.trackColor = Theme.CARD;
            }
            @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0,0));
                b.setMinimumSize(new Dimension(0,0));
                b.setMaximumSize(new Dimension(0,0));
                return b;
            }
        });
    }
}
