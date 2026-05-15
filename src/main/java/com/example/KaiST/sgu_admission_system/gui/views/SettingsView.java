package com.example.KaiST.sgu_admission_system.gui.views;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class SettingsView extends JPanel {
    public SettingsView() {
        setLayout(new BorderLayout());
        add(new JLabel("Cài đặt hệ thống", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
