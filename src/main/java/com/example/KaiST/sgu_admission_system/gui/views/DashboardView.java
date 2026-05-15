package com.example.KaiST.sgu_admission_system.gui.views;

import com.example.KaiST.sgu_admission_system.gui.components.HorizontalButtonPanel;
import com.example.KaiST.sgu_admission_system.gui.controllers.DashboardController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class DashboardView extends JPanel {
    private static final int ICON_SIZE = 18;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private DashboardController controller;

    public DashboardView() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel headerPanel = new JPanel(new BorderLayout(8, 8));
        headerPanel.add(new JLabel("Thí sinh nổi bật"), BorderLayout.WEST);
        headerPanel.add(createActionPanel(), BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        String[] columns = {
                "CCCD",
                "Số báo danh",
                "Họ tên",
                "Ngày sinh",
                "Giới tính",
                "Khu vực",
                "",
                "",
                ""
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 6;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);

        setupActionColumns();
    }

    private JPanel createActionPanel() {
        JButton addButton = new JButton("Thêm");
        ImageIcon plusIcon = tintIcon(loadIcon("/icon/plus.png", ICON_SIZE), Color.BLACK);
        if (plusIcon != null) {
            addButton.setIcon(plusIcon);
        }
        addButton.addActionListener(event -> runWithController(DashboardController::onAdd));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> runWithController(DashboardController::onRefresh));

        return new HorizontalButtonPanel(FlowLayout.RIGHT, 8, refreshButton, addButton);
    }

    private void setupActionColumns() {
        ImageIcon eyeIcon = tintIcon(loadIcon("/icon/eye.png", ICON_SIZE), Color.BLACK);
        ImageIcon editIcon = tintIcon(loadIcon("/icon/pencil.png", ICON_SIZE), Color.BLACK);
        ImageIcon deleteIcon = tintIcon(loadIcon("/icon/circle-x.png", ICON_SIZE), Color.BLACK);

        table.getColumnModel().getColumn(6).setCellRenderer(new IconButtonRenderer(
                eyeIcon));
        table.getColumnModel().getColumn(7).setCellRenderer(new IconButtonRenderer(
                editIcon));
        table.getColumnModel().getColumn(8).setCellRenderer(new IconButtonRenderer(
                deleteIcon));

        table.getColumnModel().getColumn(6).setCellEditor(new IconButtonEditor(
                new JCheckBox(),
                eyeIcon,
                row -> runWithController(ctrl -> ctrl.onViewRow(row))));
        table.getColumnModel().getColumn(7).setCellEditor(new IconButtonEditor(
                new JCheckBox(),
                editIcon,
                row -> runWithController(ctrl -> ctrl.onEditRow(row))));
        table.getColumnModel().getColumn(8).setCellEditor(new IconButtonEditor(
                new JCheckBox(),
                deleteIcon,
                row -> runWithController(ctrl -> ctrl.onDeleteRow(row))));
    }

    public void setController(DashboardController controller) {
        this.controller = controller;
    }

    public int getMaxRows() {
        return 20;
    }

    public void setTableRows(List<Object[]> rows) {
        tableModel.setRowCount(0);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
    }

    public void showInfo(String message) {
        javax.swing.JOptionPane.showMessageDialog(this, message, "Thông báo",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean confirm(String message) {
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this,
                message,
                "Xác nhận",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE);
        return confirm == javax.swing.JOptionPane.YES_OPTION;
    }

    public Window getWindow() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private ImageIcon loadIcon(String path, int size) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            return null;
        }
        ImageIcon icon = new ImageIcon(url);
        Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        if (scaled instanceof java.awt.image.BufferedImage) {
            return new ImageIcon(scaled);
        }
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        image.getGraphics().drawImage(scaled, 0, 0, null);
        return new ImageIcon(image);
    }

    private void runWithController(java.util.function.Consumer<DashboardController> action) {
        if (controller != null) {
            action.accept(controller);
        }
    }

    private ImageIcon tintIcon(ImageIcon icon, Color color) {
        if (icon == null || color == null) {
            return icon;
        }
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        icon.paintIcon(null, graphics, 0, 0);
        graphics.dispose();

        int rgb = color.getRGB() & 0x00FFFFFF;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha != 0) {
                    image.setRGB(x, y, (alpha << 24) | rgb);
                }
            }
        }
        return new ImageIcon(image);
    }

    private final class IconButtonRenderer extends JButton implements TableCellRenderer {
        public IconButtonRenderer(javax.swing.Icon icon) {
            setIcon(icon);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private final class IconButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private final java.util.function.IntConsumer action;
        private int row;

        public IconButtonEditor(JCheckBox checkBox, javax.swing.Icon icon, java.util.function.IntConsumer action) {
            super(checkBox);
            this.action = action;
            this.button = new JButton(icon);
            this.button.setBorderPainted(false);
            this.button.setContentAreaFilled(false);
            this.button.setFocusPainted(false);
            this.button.addActionListener(event -> {
                action.accept(row);
                fireEditingStopped();
            });
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                int row, int column) {
            this.row = row;
            return button;
        }
    }
}
