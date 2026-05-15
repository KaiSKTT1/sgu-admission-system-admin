package com.example.KaiST.sgu_admission_system.gui.components;

import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SearchPanel extends JPanel {
    private final JTextField searchField;

    public SearchPanel(String label, int columns, Runnable onSearch) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

        searchField = new JTextField(columns);
        JButton searchButton = new JButton("Tìm kiếm");
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
