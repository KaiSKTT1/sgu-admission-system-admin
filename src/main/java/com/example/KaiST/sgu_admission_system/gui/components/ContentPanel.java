package com.example.KaiST.sgu_admission_system.gui.components;

import com.example.KaiST.sgu_admission_system.bus.XtNganhBus;
import com.example.KaiST.sgu_admission_system.bus.XtThiSinhXetTuyen25Bus;
import com.example.KaiST.sgu_admission_system.gui.controllers.CandidateController;
import com.example.KaiST.sgu_admission_system.gui.controllers.DashboardController;
import com.example.KaiST.sgu_admission_system.gui.controllers.NganhController;
import com.example.KaiST.sgu_admission_system.gui.views.CandidateView;
import com.example.KaiST.sgu_admission_system.gui.views.DashboardView;
import com.example.KaiST.sgu_admission_system.gui.views.NganhView;
import com.example.KaiST.sgu_admission_system.gui.views.SettingsView;
import java.awt.CardLayout;
import javax.swing.JPanel;

public class ContentPanel extends JPanel {
    public static final String VIEW_DASHBOARD = "dashboard";
    public static final String VIEW_CANDIDATE = "candidate";
    public static final String VIEW_NGANH = "nganh";
    public static final String VIEW_SETTINGS = "settings";

    private final CardLayout cardLayout;

    public ContentPanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        DashboardView dashboardView = new DashboardView();
        DashboardController dashboardController = new DashboardController(dashboardView, new XtThiSinhXetTuyen25Bus());
        dashboardView.setController(dashboardController);
        dashboardController.init();
        add(dashboardView, VIEW_DASHBOARD);

        CandidateView candidateView = new CandidateView();
        CandidateController candidateController = new CandidateController(candidateView, new XtThiSinhXetTuyen25Bus());
        candidateView.setController(candidateController);
        candidateController.init();
        add(candidateView, VIEW_CANDIDATE);

        NganhView nganhView = new NganhView();
        NganhController nganhController = new NganhController(nganhView, new XtNganhBus());
        nganhView.setController(nganhController);
        nganhController.init();
        add(nganhView, VIEW_NGANH);

        add(new SettingsView(), VIEW_SETTINGS);

        showView(VIEW_DASHBOARD);
    }

    public void showView(String viewKey) {
        cardLayout.show(this, viewKey);
    }
}
