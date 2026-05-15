package com.example.KaiST.sgu_admission_system.gui.components;

import com.example.KaiST.sgu_admission_system.gui.views.CandidateView;
import com.example.KaiST.sgu_admission_system.gui.views.DashboardView;
import com.example.KaiST.sgu_admission_system.gui.views.SettingsView;
import java.awt.CardLayout;
import javax.swing.JPanel;

public class ContentPanel extends JPanel {
    public static final String VIEW_DASHBOARD = "dashboard";
    public static final String VIEW_CANDIDATE = "candidate";
    public static final String VIEW_SETTINGS = "settings";

    private final CardLayout cardLayout;

    public ContentPanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        add(new DashboardView(), VIEW_DASHBOARD);
        add(new CandidateView(), VIEW_CANDIDATE);
        add(new SettingsView(), VIEW_SETTINGS);

        showView(VIEW_DASHBOARD);
    }

    public void showView(String viewKey) {
        cardLayout.show(this, viewKey);
    }
}
