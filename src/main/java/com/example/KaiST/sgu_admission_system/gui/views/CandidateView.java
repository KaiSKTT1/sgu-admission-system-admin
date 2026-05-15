package com.example.KaiST.sgu_admission_system.gui.views;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class CandidateView extends JPanel {
    public CandidateView() {
        setLayout(new BorderLayout());
        add(new JLabel("Quản lý thí sinh", SwingConstants.CENTER), BorderLayout.CENTER);
    }
}
