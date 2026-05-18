package com.example.KaiST.sgu_admission_system.gui.components;

import com.example.KaiST.sgu_admission_system.gui.theme.UiTheme;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class CardPanel extends JPanel {
    public CardPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UiTheme.CARD_BG);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
        g2.setColor(UiTheme.CARD_BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
        g2.dispose();
        super.paintComponent(g);
    }
}
