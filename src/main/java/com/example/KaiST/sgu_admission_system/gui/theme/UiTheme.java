package com.example.KaiST.sgu_admission_system.gui.theme;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public final class UiTheme {
    public static final Color PAGE_BG = new Color(245, 247, 250);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color CARD_BORDER = new Color(226, 232, 240);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);
    public static final Color TEXT_DARK = new Color(30, 41, 59);
    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color PRIMARY_HOVER = new Color(30, 64, 175);

    private UiTheme() {
    }

    public static void apply() {
        FlatLaf.setup(new FlatLightLaf());
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));

        UIManager.put("Panel.background", PAGE_BG);
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.track", PAGE_BG);
        UIManager.put("ScrollBar.thumb", new Color(203, 213, 225));

        UIManager.put("Table.rowHeight", 32);
        UIManager.put("Table.showHorizontalLines", Boolean.FALSE);
        UIManager.put("Table.showVerticalLines", Boolean.FALSE);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 0));
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.selectionBackground", new Color(219, 234, 254));
        UIManager.put("Table.selectionForeground", TEXT_DARK);
        UIManager.put("Table.alternateRowColor", new Color(248, 250, 252));

        UIManager.put("TableHeader.background", Color.WHITE);
        UIManager.put("TableHeader.foreground", TEXT_MUTED);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 12));

        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", TEXT_DARK);
        UIManager.put("TextField.inactiveBackground", new Color(241, 245, 249));
        UIManager.put("PasswordField.background", Color.WHITE);

        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.buttonBackground", new Color(226, 232, 240));
        UIManager.put("ComboBox.buttonArrowColor", TEXT_MUTED);

        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("Button.foreground", TEXT_DARK);
        UIManager.put("Button.hoverBackground", new Color(226, 232, 240));
        UIManager.put("Button.default.background", PRIMARY);
        UIManager.put("Button.default.foreground", Color.WHITE);
        UIManager.put("Button.default.hoverBackground", PRIMARY_HOVER);
    }

    public static void applyTableStyle(JTable table) {
        table.setRowHeight(36);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_DARK);
        HoverTableCellRenderer renderer = new HoverTableCellRenderer();
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Number.class, renderer);
        table.setDefaultRenderer(Boolean.class, renderer);

        TableHoverSupport support = new TableHoverSupport(table);
        table.addMouseMotionListener(support);
        table.addMouseListener(support);
    }

    private static final class TableHoverSupport extends MouseAdapter {
        private final JTable table;

        private TableHoverSupport(JTable table) {
            this.table = table;
        }

        @Override
        public void mouseMoved(MouseEvent event) {
            int row = table.rowAtPoint(event.getPoint());
            Integer current = (Integer) table.getClientProperty("hoverRow");
            if (current == null || row != current) {
                table.putClientProperty("hoverRow", row);
                table.repaint();
            }
        }

        @Override
        public void mouseExited(MouseEvent event) {
            table.putClientProperty("hoverRow", -1);
            table.repaint();
        }
    }

    private static final class HoverTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component component = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            Integer hoverRow = (Integer) table.getClientProperty("hoverRow");
            boolean isHover = hoverRow != null && hoverRow == row;

            if (!isSelected && isHover) {
                component.setBackground(new Color(241, 245, 249));
            } else if (!isSelected) {
                component.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            }
            return component;
        }
    }
}
