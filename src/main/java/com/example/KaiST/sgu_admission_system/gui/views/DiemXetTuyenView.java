package com.example.KaiST.sgu_admission_system.gui.views;

import com.example.KaiST.sgu_admission_system.gui.components.HorizontalButtonPanel;
import com.example.KaiST.sgu_admission_system.gui.components.PaginationPanel;
import com.example.KaiST.sgu_admission_system.gui.components.SearchPanel;
import com.example.KaiST.sgu_admission_system.gui.controllers.DiemXetTuyenController;
import com.example.KaiST.sgu_admission_system.gui.theme.UiTheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class DiemXetTuyenView extends JPanel {
    private static final int PAGE_SIZE = 20;

    private final SearchPanel searchPanel;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final PaginationPanel paginationPanel;
    private DiemXetTuyenController controller;

    public DiemXetTuyenView() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel headerPanel = new JPanel(new BorderLayout(8, 8));
        headerPanel.setOpaque(false);
        searchPanel = createSearchPanel();
        headerPanel.add(searchPanel, BorderLayout.WEST);
        headerPanel.add(createActionPanel(), BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        String[] columns = {
                "STT",
                "CCCD",
                "Họ tên",
                "Mã ngành",
                "Thứ tự",
                "Phương thức",
                "THM",
                "Điểm THXT",
                "Điểm cộng",
                "Điểm ưu tiên",
                "Điểm xét tuyển",
                "Kết quả",
                "Keys"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        UiTheme.applyTableStyle(table);

        // Set column widths and formatting
        setupTableColumns();

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        paginationPanel = new PaginationPanel(page -> runWithController(ctrl -> ctrl.onPageChange(page)));
        add(paginationPanel, BorderLayout.SOUTH);
    }

    private void setupTableColumns() {
        // Column widths: STT, CCCD, Name, MaNganh, ThuTu, Method, THM, Scores, Result, Keys
        int[] columnWidths = { 50, 120, 150, 100, 80, 130, 80, 100, 100, 100, 110, 100, 100 };
        
        for (int i = 0; i < columnWidths.length; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnWidths[i]);
            
            // Format numeric columns (columns 7-10 are score columns)
            if (i >= 7 && i <= 10) {
                DefaultTableCellRenderer numberRenderer = new DefaultTableCellRenderer() {
                    private final DecimalFormat df = new DecimalFormat("0.00");
                    
                    @Override
                    public void setValue(Object value) {
                        if (value instanceof BigDecimal) {
                            setText(df.format((BigDecimal) value));
                        } else if (value instanceof Number) {
                            setText(df.format(((Number) value).doubleValue()));
                        } else {
                            setText((value == null) ? "" : value.toString());
                        }
                    }
                };
                numberRenderer.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                column.setCellRenderer(numberRenderer);
            }
        }
    }

    private SearchPanel createSearchPanel() {
        return new SearchPanel("Tìm theo CCCD:", 22, () -> runWithController(DiemXetTuyenController::onSearch));
    }

    private JPanel createActionPanel() {
        JButton refreshButton = new JButton("Làm mới");
        JButton runButton = new JButton("Xét tuyển");
        JButton exportButton = new JButton("Xuất file");

        refreshButton.addActionListener(event -> runWithController(DiemXetTuyenController::onRefresh));
        runButton.addActionListener(event -> runWithController(DiemXetTuyenController::onRunXetTuyen));
        exportButton.addActionListener(event -> runWithController(DiemXetTuyenController::onExport));

        return new HorizontalButtonPanel(FlowLayout.RIGHT, 8, refreshButton, runButton, exportButton);
    }

    public void setController(DiemXetTuyenController controller) {
        this.controller = controller;
    }

    public String getSearchKeyword() {
        return searchPanel.getKeyword();
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

    public File chooseSaveFile(String title, String defaultName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setSelectedFile(new File(defaultName));
        chooser.setFileFilter(new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));
        int result = chooser.showSaveDialog(this);
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

    public java.awt.Window getWindow() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private void runWithController(java.util.function.Consumer<DiemXetTuyenController> action) {
        if (controller != null) {
            action.accept(controller);
        }
    }
}
