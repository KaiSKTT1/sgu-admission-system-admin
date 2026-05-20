package com.example.KaiST.sgu_admission_system.gui.components;

import com.example.KaiST.sgu_admission_system.bus.XtNganhBus;
import com.example.KaiST.sgu_admission_system.bus.XtThiSinhXetTuyen25Bus;
import com.example.KaiST.sgu_admission_system.bus.XtDiemThiXetTuyenBus;
import com.example.KaiST.sgu_admission_system.bus.XtDiemCongXetTuyenBus;
import com.example.KaiST.sgu_admission_system.bus.XtBangQuyDoiBus;
import com.example.KaiST.sgu_admission_system.bus.XtToHopMonThiBus;
import com.example.KaiST.sgu_admission_system.bus.XtNganhToHopBus;
import com.example.KaiST.sgu_admission_system.bus.XtNguyenVongXetTuyenBus;
import com.example.KaiST.sgu_admission_system.bus.XetTuyenBus;
import com.example.KaiST.sgu_admission_system.bus.XtUserBus;
import com.example.KaiST.sgu_admission_system.gui.controllers.CandidateController;
import com.example.KaiST.sgu_admission_system.gui.controllers.DiemThiXetTuyenController;
import com.example.KaiST.sgu_admission_system.gui.controllers.DiemCongXetTuyenController;
import com.example.KaiST.sgu_admission_system.gui.controllers.DiemXetTuyenController;
import com.example.KaiST.sgu_admission_system.gui.controllers.BangQuyDoiController;
import com.example.KaiST.sgu_admission_system.gui.controllers.DashboardController;
import com.example.KaiST.sgu_admission_system.gui.controllers.NganhController;
import com.example.KaiST.sgu_admission_system.gui.controllers.NganhToHopController;
import com.example.KaiST.sgu_admission_system.gui.controllers.ToHopMonThiController;
import com.example.KaiST.sgu_admission_system.gui.controllers.UserController;
import com.example.KaiST.sgu_admission_system.gui.controllers.XetTuyenController;
import com.example.KaiST.sgu_admission_system.gui.views.CandidateView;
import com.example.KaiST.sgu_admission_system.gui.views.DiemThiXetTuyenView;
import com.example.KaiST.sgu_admission_system.gui.views.DiemCongXetTuyenView;
import com.example.KaiST.sgu_admission_system.gui.views.DiemXetTuyenView;
import com.example.KaiST.sgu_admission_system.gui.views.BangQuyDoiView;
import com.example.KaiST.sgu_admission_system.gui.views.DashboardView;
import com.example.KaiST.sgu_admission_system.gui.views.NganhView;
import com.example.KaiST.sgu_admission_system.gui.views.NganhToHopView;
import com.example.KaiST.sgu_admission_system.gui.views.SettingsView;
import com.example.KaiST.sgu_admission_system.gui.views.ToHopMonThiView;
import com.example.KaiST.sgu_admission_system.gui.views.UserView;
import com.example.KaiST.sgu_admission_system.gui.views.XetTuyenView;
import com.example.KaiST.sgu_admission_system.gui.theme.UiTheme;
import java.awt.CardLayout;
import javax.swing.JPanel;

public class ContentPanel extends JPanel {
        public static final String VIEW_DASHBOARD = "dashboard";
        public static final String VIEW_CANDIDATE = "candidate";
        public static final String VIEW_DIEMTHI = "diemthi";
        public static final String VIEW_DIEMCONG = "diemcong";
        public static final String VIEW_DIEMXET = "diemxet";
        public static final String VIEW_XETTUYEN = "xettuyen";
        public static final String VIEW_USER = "user";
        public static final String VIEW_QUYDOI = "quydoi";
        public static final String VIEW_NGANH = "nganh";
        public static final String VIEW_TOHOP = "tohop";
        public static final String VIEW_NGANH_TOHOP = "nganh_tohop";
        public static final String VIEW_SETTINGS = "settings";

        private final CardLayout cardLayout;

        public ContentPanel() {
                cardLayout = new CardLayout();
                setLayout(cardLayout);
                setBackground(UiTheme.PAGE_BG);

                DashboardView dashboardView = new DashboardView();
                DashboardController dashboardController = new DashboardController(dashboardView,
                                new XtThiSinhXetTuyen25Bus());
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

                DiemXetTuyenView diemXetView = new DiemXetTuyenView();
                XtNguyenVongXetTuyenBus nguyenVongBus = new XtNguyenVongXetTuyenBus();
                XetTuyenBus xetTuyenBus = new XetTuyenBus(
                                new XtThiSinhXetTuyen25Bus(),
                                nguyenVongBus,
                                new XtDiemThiXetTuyenBus(),
                                new XtDiemCongXetTuyenBus(),
                                new XtBangQuyDoiBus(),
                                new XtNganhBus(),
                                new XtNganhToHopBus());
                DiemXetTuyenController diemXetController = new DiemXetTuyenController(diemXetView, xetTuyenBus, nguyenVongBus);
                diemXetView.setController(diemXetController);
                diemXetController.init();
                add(diemXetView, VIEW_DIEMXET);

                XetTuyenView xetTuyenView = new XetTuyenView();
                XetTuyenController xetTuyenController = new XetTuyenController(xetTuyenView, xetTuyenBus, new XtNganhBus());
                xetTuyenView.setController(xetTuyenController);
                xetTuyenController.init();
                add(xetTuyenView, VIEW_XETTUYEN);

                UserView userView = new UserView();
                UserController userController = new UserController(userView, new XtUserBus());
                userView.setController(userController);
                userController.init();
                add(userView, VIEW_USER);

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

                NganhToHopView nganhToHopView = new NganhToHopView();
                NganhToHopController nganhToHopController = new NganhToHopController(
                                nganhToHopView,
                                new XtNganhToHopBus(),
                                new XtNganhBus(),
                                new XtToHopMonThiBus());
                nganhToHopView.setController(nganhToHopController);
                nganhToHopController.init();
                add(nganhToHopView, VIEW_NGANH_TOHOP);

                add(new SettingsView(), VIEW_SETTINGS);

                showView(VIEW_DASHBOARD);
        }

        public void showView(String viewKey) {
                cardLayout.show(this, viewKey);
        }
}
