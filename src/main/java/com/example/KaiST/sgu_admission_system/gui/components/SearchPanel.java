package com.example.KaiST.sgu_admission_system.gui.components;

import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SearchPanel extends JPanel {
    private final JTextField searchField;

    public SearchPanel(String label, int columns, Runnable onSearch) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        setOpaque(false);

        searchField = new JTextField(columns);
        searchField.putClientProperty("JTextField.placeholderText", "Nhập từ khóa...");

        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.putClientProperty("JButton.buttonType", "roundRect");
        searchButton.setMargin(new Insets(6, 14, 6, 14));
        searchButton.addActionListener(event -> onSearch.run());

        add(new JLabel(label));
        add(searchField);
        add(searchButton);
    }

    public String getKeyword() {
        return searchField.getText();
    }

    public void setKeyword(String keyword) {
        searchField.setText(keyword);
    }
}
