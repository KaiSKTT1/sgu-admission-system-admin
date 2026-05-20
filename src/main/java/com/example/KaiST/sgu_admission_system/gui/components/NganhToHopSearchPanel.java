package com.example.KaiST.sgu_admission_system.gui.components;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class NganhToHopSearchPanel extends JPanel {
    private final JTextField searchField;
    private final JCheckBox cbMaNganh;
    private final JCheckBox cbTenNganh;
    private final JCheckBox cbMaToHop;
    private final JCheckBox cbMon;

    public NganhToHopSearchPanel(String label, int columns, Runnable onSearch) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        setOpaque(false);

        // Tìm kiếm text field
        searchField = new JTextField(columns);
        searchField.putClientProperty("JTextField.placeholderText", "Nhập từ khóa...");
        // Hỗ trợ nhấn Enter để tìm kiếm
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onSearch.run();
                }
            }
        });

        // Checkboxes cho các loại tìm kiếm
        cbMaNganh = new JCheckBox("Mã ngành", true);
        cbTenNganh = new JCheckBox("Tên ngành", false);
        cbMaToHop = new JCheckBox("Mã tổ hợp", true);
        cbMon = new JCheckBox("Môn", false);

        // Nút tìm kiếm
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.putClientProperty("JButton.buttonType", "roundRect");
        searchButton.setMargin(new Insets(6, 14, 6, 14));
        searchButton.addActionListener(event -> onSearch.run());

        // Thêm các thành phần
        add(new JLabel(label));
        add(searchField);
        add(new JLabel("Tìm trong:"));
        add(cbMaNganh);
        add(cbTenNganh);
        add(cbMaToHop);
        add(cbMon);
        add(searchButton);
    }

    public String getKeyword() {
        return searchField.getText();
    }

    public void setKeyword(String keyword) {
        searchField.setText(keyword);
    }

    public boolean isSearchByMaNganh() {
        return cbMaNganh.isSelected();
    }

    public boolean isSearchByTenNganh() {
        return cbTenNganh.isSelected();
    }

    public boolean isSearchByMaToHop() {
        return cbMaToHop.isSelected();
    }

    public boolean isSearchByMon() {
        return cbMon.isSelected();
    }

    public void setSearchByMaNganh(boolean selected) {
        cbMaNganh.setSelected(selected);
    }

    public void setSearchByTenNganh(boolean selected) {
        cbTenNganh.setSelected(selected);
    }

    public void setSearchByMaToHop(boolean selected) {
        cbMaToHop.setSelected(selected);
    }

    public void setSearchByMon(boolean selected) {
        cbMon.setSelected(selected);
    }
}
