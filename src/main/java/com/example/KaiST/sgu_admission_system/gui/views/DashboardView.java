package com.example.KaiST.sgu_admission_system.gui.views;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DashboardView extends JPanel {
    public DashboardView() {
        setLayout(new BorderLayout());
        add(new JLabel("Màn hình tổng quan", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
