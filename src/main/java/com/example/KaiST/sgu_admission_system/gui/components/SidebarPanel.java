package com.example.KaiST.sgu_admission_system.gui.components;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class SidebarPanel extends JPanel {
    private final ContentPanel contentPanel;

    public SidebarPanel(ContentPanel contentPanel) {
        this.contentPanel = contentPanel;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(220, 0));

        add(createNavButton("Trang chủ", ContentPanel.VIEW_DASHBOARD));
        add(createNavButton("Thí sinh", ContentPanel.VIEW_CANDIDATE));
        add(createNavButton("Cài đặt", ContentPanel.VIEW_SETTINGS));
    }

    private JButton createNavButton(String text, String viewKey) {
        JButton button = new JButton(text);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        button.addActionListener(event -> contentPanel.showView(viewKey));
        return button;
    }
}
