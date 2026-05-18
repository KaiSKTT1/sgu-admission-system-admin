package com.example.KaiST.sgu_admission_system.gui.components;

import com.example.KaiST.sgu_admission_system.bus.XtNganhBus;
import com.example.KaiST.sgu_admission_system.bus.XtThiSinhXetTuyen25Bus;
import com.example.KaiST.sgu_admission_system.bus.XtDiemThiXetTuyenBus;
import com.example.KaiST.sgu_admission_system.bus.XtDiemCongXetTuyenBus;
import com.example.KaiST.sgu_admission_system.bus.XtBangQuyDoiBus;
import com.example.KaiST.sgu_admission_system.bus.XtToHopMonThiBus;
import com.example.KaiST.sgu_admission_system.gui.controllers.CandidateController;
import com.example.KaiST.sgu_admission_system.gui.controllers.DiemThiXetTuyenController;
import com.example.KaiST.sgu_admission_system.gui.controllers.DiemCongXetTuyenController;
import com.example.KaiST.sgu_admission_system.gui.controllers.BangQuyDoiController;
import com.example.KaiST.sgu_admission_system.gui.controllers.DashboardController;
import com.example.KaiST.sgu_admission_system.gui.controllers.NganhController;
import com.example.KaiST.sgu_admission_system.gui.controllers.ToHopMonThiController;
import com.example.KaiST.sgu_admission_system.gui.views.CandidateView;
import com.example.KaiST.sgu_admission_system.gui.views.DiemThiXetTuyenView;
import com.example.KaiST.sgu_admission_system.gui.views.DiemCongXetTuyenView;
import com.example.KaiST.sgu_admission_system.gui.views.BangQuyDoiView;
import com.example.KaiST.sgu_admission_system.gui.views.DashboardView;
import com.example.KaiST.sgu_admission_system.gui.views.NganhView;
import com.example.KaiST.sgu_admission_system.gui.views.SettingsView;
import com.example.KaiST.sgu_admission_system.gui.views.ToHopMonThiView;
import java.awt.CardLayout;
import javax.swing.JPanel;

public class ContentPanel extends JPanel {
    public static final String VIEW_DASHBOARD = "dashboard";
    public static final String VIEW_CANDIDATE = "candidate";
    public static final String VIEW_DIEMTHI = "diemthi";
    public static final String VIEW_DIEMCONG = "diemcong";
    public static final String VIEW_QUYDOI = "quydoi";
    public static final String VIEW_NGANH = "nganh";
    public static final String VIEW_TOHOP = "tohop";
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
        CandidateController candidateController = new CandidateController(
                candidateView,
                new XtThiSinhXetTuyen25Bus(),
                new XtDiemThiXetTuyenBus());
        candidateView.setController(candidateController);
        candidateController.init();
        add(candidateView, VIEW_CANDIDATE);

        DiemThiXetTuyenView diemThiView = new DiemThiXetTuyenView();
        DiemThiXetTuyenController diemThiController = new DiemThiXetTuyenController(
                diemThiView,
                new XtDiemThiXetTuyenBus());
        diemThiView.setController(diemThiController);
        diemThiController.init();
        add(diemThiView, VIEW_DIEMTHI);

        DiemCongXetTuyenView diemCongView = new DiemCongXetTuyenView();
        DiemCongXetTuyenController diemCongController = new DiemCongXetTuyenController(
                diemCongView,
                new XtDiemCongXetTuyenBus());
        diemCongView.setController(diemCongController);
        diemCongController.init();
        add(diemCongView, VIEW_DIEMCONG);

        BangQuyDoiView quyDoiView = new BangQuyDoiView();
        BangQuyDoiController quyDoiController = new BangQuyDoiController(
                quyDoiView,
                new XtBangQuyDoiBus());
        quyDoiView.setController(quyDoiController);
        quyDoiController.init();
        add(quyDoiView, VIEW_QUYDOI);

        NganhView nganhView = new NganhView();
        NganhController nganhController = new NganhController(nganhView, new XtNganhBus());
        nganhView.setController(nganhController);
        nganhController.init();
        add(nganhView, VIEW_NGANH);

        ToHopMonThiView toHopView = new ToHopMonThiView();
        ToHopMonThiController toHopController = new ToHopMonThiController(toHopView, new XtToHopMonThiBus());
        toHopView.setController(toHopController);
        toHopController.init();
        add(toHopView, VIEW_TOHOP);

        add(new SettingsView(), VIEW_SETTINGS);

        showView(VIEW_DASHBOARD);
    }

    public void showView(String viewKey) {
        cardLayout.show(this, viewKey);
    }
}
