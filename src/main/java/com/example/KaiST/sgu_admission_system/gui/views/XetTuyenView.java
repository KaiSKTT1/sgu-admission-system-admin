package com.example.KaiST.sgu_admission_system.gui.views;

import com.example.KaiST.sgu_admission_system.gui.components.PaginationPanel;
import com.example.KaiST.sgu_admission_system.gui.components.SearchPanel;
import com.example.KaiST.sgu_admission_system.gui.controllers.XetTuyenController;
import com.example.KaiST.sgu_admission_system.gui.theme.UiTheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class XetTuyenView extends JPanel {
    private static final int PAGE_SIZE = 20;

    private final SearchPanel searchPanel;
    private final JComboBox<String> maNganhComboBox;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final PaginationPanel paginationPanel;
    private XetTuyenController controller;

    public XetTuyenView() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        maNganhComboBox = new JComboBox<>();
        maNganhComboBox.setPrototypeDisplayValue("XXXXXXXXXXXXX");

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
                "Phương thức",
                "Điểm xét tuyển",
                "Chỉ tiêu",
                "Điểm chuẩn"
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

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        paginationPanel = new PaginationPanel(page -> runWithController(ctrl -> ctrl.onPageChange(page)));
        add(paginationPanel, BorderLayout.SOUTH);
    }

    private SearchPanel createSearchPanel() {
        return new SearchPanel("Tìm CCCD / ngành:", 22, () -> runWithController(XetTuyenController::onSearch));
    }

    private JPanel createActionPanel() {
        JButton refreshButton = new JButton("Làm mới");
        JButton runButton = new JButton("Xét tuyển");

        refreshButton.addActionListener(event -> runWithController(XetTuyenController::onRefresh));
        runButton.addActionListener(event -> runWithController(XetTuyenController::onRun));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(new JLabel("Ngành:"));
        actionPanel.add(maNganhComboBox);
        actionPanel.add(refreshButton);
        actionPanel.add(runButton);
        return actionPanel;
    }

    public void setNganhOptions(List<String> options) {
        maNganhComboBox.removeAllItems();
        maNganhComboBox.addItem("Tất cả ngành");
        if (options != null) {
            for (String maNganh : options) {
                if (maNganh != null && !maNganh.isBlank()) {
                    maNganhComboBox.addItem(maNganh.trim());
                }
            }
        }
    }

    public String getSelectedMaNganh() {
        Object selected = maNganhComboBox.getSelectedItem();
        return selected == null || "Tất cả ngành".equals(selected) ? "" : selected.toString();
    }

    public void setController(XetTuyenController controller) {
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

    public void showInfo(String message) {
        javax.swing.JOptionPane.showMessageDialog(this, message, "Thông báo",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        javax.swing.JOptionPane.showMessageDialog(this, message, "Lỗi",
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    private void runWithController(java.util.function.Consumer<XetTuyenController> action) {
        if (controller != null) {
            action.accept(controller);
        }
    }
}
