package com.example.KaiST.sgu_admission_system.gui;

import com.example.KaiST.sgu_admission_system.gui.components.ContentPanel;
import com.example.KaiST.sgu_admission_system.gui.components.SidebarPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;

public class GUI extends JFrame {
    private final ContentPanel contentPanel;

    public GUI() {
        setTitle("Hệ thống tuyển sinh SGU");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        contentPanel = new ContentPanel();
        SidebarPanel sidebarPanel = new SidebarPanel(contentPanel);

        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        setMinimumSize(new Dimension(1200, 720));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }
}
