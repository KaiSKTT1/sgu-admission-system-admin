package com.example.KaiST.sgu_admission_system.gui.views;

import com.example.KaiST.sgu_admission_system.gui.components.HorizontalButtonPanel;
import com.example.KaiST.sgu_admission_system.gui.components.PaginationPanel;
import com.example.KaiST.sgu_admission_system.gui.components.SearchPanel;
import com.example.KaiST.sgu_admission_system.gui.controllers.DiemThiXetTuyenController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class DiemThiXetTuyenView extends JPanel {
    private static final int PAGE_SIZE = 20;
    private static final int ICON_SIZE = 18;
    private static final int ACTION_COLUMN_INDEX = 6;

    private final SearchPanel searchPanel;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final PaginationPanel paginationPanel;
    private DiemThiXetTuyenController controller;

    public DiemThiXetTuyenView() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel headerPanel = new JPanel(new BorderLayout(8, 8));
        searchPanel = createSearchPanel();
        headerPanel.add(searchPanel, BorderLayout.WEST);
        headerPanel.add(createActionPanel(), BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        String[] columns = {
                "STT",
                "CCCD",
                "Số báo danh",
                "Phương thức",
                "TO",
                "LI",
                "Chức năng"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == ACTION_COLUMN_INDEX;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        setupActionColumns();

        paginationPanel = new PaginationPanel(page -> runWithController(ctrl -> ctrl.onPageChange(page)));
        add(paginationPanel, BorderLayout.SOUTH);
    }

    private SearchPanel createSearchPanel() {
        return new SearchPanel("Tìm điểm:", 22, () -> runWithController(DiemThiXetTuyenController::onSearch));
    }

    private JPanel createActionPanel() {
        JButton importButton = new JButton("Import");
        JButton refreshButton = new JButton("Refresh");
        JButton addButton = new JButton();
        addButton.setToolTipText("Thêm điểm thi");
        addButton.setIcon(tintIcon(loadIcon("/icon/plus.png", ICON_SIZE), Color.BLACK));

        importButton.addActionListener(event -> runWithController(DiemThiXetTuyenController::onImport));
        refreshButton.addActionListener(event -> runWithController(DiemThiXetTuyenController::onRefresh));
        addButton.addActionListener(event -> runWithController(DiemThiXetTuyenController::onAdd));

        return new HorizontalButtonPanel(FlowLayout.RIGHT, 8, importButton, refreshButton, addButton);
    }

    private void setupActionColumns() {
        ImageIcon eyeIcon = tintIcon(loadIcon("/icon/eye.png", ICON_SIZE, false), Color.BLACK);
        ImageIcon editIcon = tintIcon(loadIcon("/icon/pencil.png", ICON_SIZE, false), Color.BLACK);
        ImageIcon deleteIcon = tintIcon(loadIcon("/icon/circle-x.png", ICON_SIZE, false), Color.BLACK);

        table.getColumnModel().getColumn(ACTION_COLUMN_INDEX).setCellRenderer(
                new ActionCellRenderer(eyeIcon, editIcon, deleteIcon));
        table.getColumnModel().getColumn(ACTION_COLUMN_INDEX).setCellEditor(
                new ActionCellEditor(eyeIcon, editIcon, deleteIcon));
        table.getColumnModel().getColumn(ACTION_COLUMN_INDEX).setMaxWidth(140);
        table.getColumnModel().getColumn(ACTION_COLUMN_INDEX).setMinWidth(140);
        table.getColumnModel().getColumn(ACTION_COLUMN_INDEX).setPreferredWidth(140);
        table.getColumnModel().getColumn(ACTION_COLUMN_INDEX).setResizable(false);
    }

    public void setController(DiemThiXetTuyenController controller) {
        this.controller = controller;
    }

    public String getSearchKeyword() {
        return searchPanel.getKeyword();
    }

    public int getSelectedRow() {
        return table.getSelectedRow();
    }

    public int getPageSize() {
        return PAGE_SIZE;
    }

    public void setTableRows(List<Object[]> rows) {
        tableModel.setRowCount(0);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
    }

    public void updatePagination(int currentPage, int totalPages) {
        paginationPanel.updatePageInfo(currentPage, totalPages);
    }

    public File chooseExcelFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Excel (*.xlsx, *.xls)", "xlsx", "xls"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return chooser.getSelectedFile();
    }

    public void showInfo(String message) {
        javax.swing.JOptionPane.showMessageDialog(this, message, "Thông báo",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        javax.swing.JOptionPane.showMessageDialog(this, message, "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
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

    private void runWithController(java.util.function.Consumer<DiemThiXetTuyenController> action) {
        if (controller != null) {
            action.accept(controller);
        }
    }

    private ImageIcon loadIcon(String path, int size, boolean forceBlack) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            return null;
        }
        ImageIcon icon = new ImageIcon(url);
        Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        if (!forceBlack) {
            return new ImageIcon(scaled);
        }
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        image.getGraphics().drawImage(scaled, 0, 0, null);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha != 0) {
                    image.setRGB(x, y, (alpha << 24));
                }
            }
        }
        return new ImageIcon(image);
    }

    private ImageIcon loadIcon(String path, int size) {
        return loadIcon(path, size, false);
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

    private final class ActionCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton viewButton;
        private final JButton editButton;
        private final JButton deleteButton;

        private ActionCellRenderer(ImageIcon viewIcon, ImageIcon editIcon, ImageIcon deleteIcon) {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));
            setOpaque(true);

            viewButton = createIconButton(viewIcon, "Xem chi tiết", "Xem");
            editButton = createIconButton(editIcon, "Sửa", "Sửa");
            deleteButton = createIconButton(deleteIcon, "Xóa", "X");

            add(viewButton);
            add(editButton);
            add(deleteButton);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }

    private final class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel;

        private ActionCellEditor(ImageIcon viewIcon, ImageIcon editIcon, ImageIcon deleteIcon) {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            panel.setOpaque(true);

            JButton viewButton = createIconButton(viewIcon, "Xem chi tiết", "Xem");
            JButton editButton = createIconButton(editIcon, "Sửa", "Sửa");
            JButton deleteButton = createIconButton(deleteIcon, "Xóa", "X");

            viewButton.addActionListener(event -> handleActionRow(ActionType.VIEW));
            editButton.addActionListener(event -> handleActionRow(ActionType.EDIT));
            deleteButton.addActionListener(event -> handleActionRow(ActionType.DELETE));

            panel.add(viewButton);
            panel.add(editButton);
            panel.add(deleteButton);
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                int row, int column) {
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }
            panel.putClientProperty("row", row);
            return panel;
        }

        private void handleActionRow(ActionType type) {
            Object rowObj = panel.getClientProperty("row");
            int row = rowObj instanceof Integer ? (Integer) rowObj : -1;
            if (row >= 0) {
                runWithController(ctrl -> {
                    switch (type) {
                        case VIEW -> ctrl.onViewRow(row);
                        case EDIT -> ctrl.onEditRow(row);
                        case DELETE -> ctrl.onDeleteRow(row);
                    }
                });
            }
            fireEditingStopped();
        }
    }

    private JButton createIconButton(ImageIcon icon, String tooltip, String fallbackText) {
        JButton button = new JButton();
        if (icon != null) {
            button.setIcon(icon);
        } else {
            button.setText(fallbackText);
        }
        button.setToolTipText(tooltip);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new java.awt.Dimension(22, 22));
        return button;
    }

    private enum ActionType {
        VIEW,
        EDIT,
        DELETE
    }
}
