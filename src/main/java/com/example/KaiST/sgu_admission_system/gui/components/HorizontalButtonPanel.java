package com.example.KaiST.sgu_admission_system.gui.components;

import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class HorizontalButtonPanel extends JPanel {
    public HorizontalButtonPanel(int alignment, int hgap, JButton... buttons) {
        setLayout(new FlowLayout(alignment, hgap, 0));
        setOpaque(false);
        for (JButton button : buttons) {
            add(button);
        }
    }
}
