package com.example.KaiST.sgu_admission_system.gui.components;

import java.awt.FlowLayout;
import java.util.function.IntConsumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PaginationPanel extends JPanel {
    private final JButton prevButton;
    private final JButton nextButton;
    private final JLabel pageLabel;
    private final IntConsumer onPageChange;
    private int currentPage;

    public PaginationPanel(IntConsumer onPageChange) {
        this.onPageChange = onPageChange;
        setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));

        prevButton = new JButton("<");
        nextButton = new JButton(">");
        pageLabel = new JLabel("Trang 1/1");

        prevButton.addActionListener(event -> onPageChange.accept(currentPage - 1));
        nextButton.addActionListener(event -> onPageChange.accept(currentPage + 1));

        add(prevButton);
        add(pageLabel);
        add(nextButton);
    }

    public void updatePageInfo(int currentPage, int totalPages) {
        this.currentPage = currentPage;
        pageLabel.setText("Trang " + currentPage + "/" + totalPages);
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
    }
}
