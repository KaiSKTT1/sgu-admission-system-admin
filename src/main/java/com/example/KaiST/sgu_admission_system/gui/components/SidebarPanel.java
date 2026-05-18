package com.example.KaiST.sgu_admission_system.gui.components;

import com.example.KaiST.sgu_admission_system.commen.Quyen;
import com.example.KaiST.sgu_admission_system.entity.XtUser;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class SidebarPanel extends JPanel {
    private final ContentPanel contentPanel;
    private final Map<String, JButton> navButtons = new HashMap<>();
    private final Color activeColor = new Color(210, 228, 245);
    private final Color defaultColor = new Color(245, 245, 245);
    private final Color activeTextColor = new Color(28, 64, 112);
    private final Color defaultTextColor = new Color(40, 40, 40);

    public SidebarPanel(ContentPanel contentPanel, XtUser loggedIn) {
        this.contentPanel = contentPanel;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(220, 0));

        boolean isAdmin = Quyen.fromRole(loggedIn == null ? null : loggedIn.getRole()).isAdmin();

        add(createNavButton("Trang chủ", ContentPanel.VIEW_DASHBOARD));
        add(createNavButton("Thí sinh", ContentPanel.VIEW_CANDIDATE));
        add(createNavButton("Điểm thi", ContentPanel.VIEW_DIEMTHI));
        add(createNavButton("Điểm cộng", ContentPanel.VIEW_DIEMCONG));
        add(createNavButton("Điểm xét tuyển", ContentPanel.VIEW_DIEMXET));
        if (isAdmin) {
            add(createNavButton("Người dùng", ContentPanel.VIEW_USER));
        }
        add(createNavButton("Bảng quy đổi", ContentPanel.VIEW_QUYDOI));
        add(createNavButton("Ngành", ContentPanel.VIEW_NGANH));
        add(createNavButton("Tổ hợp môn thi", ContentPanel.VIEW_TOHOP));
        add(createNavButton("Cài đặt", ContentPanel.VIEW_SETTINGS));

        setActive(ContentPanel.VIEW_DASHBOARD);
    }

    private JButton createNavButton(String text, String viewKey) {
        JButton button = new JButton(text);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setBackground(defaultColor);
        button.setForeground(defaultTextColor);
        button.addActionListener(event -> {
            contentPanel.showView(viewKey);
            setActive(viewKey);
        });
        navButtons.put(viewKey, button);
        return button;
    }

    private void setActive(String viewKey) {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton button = entry.getValue();
            if (entry.getKey().equals(viewKey)) {
                button.setBackground(activeColor);
                button.setForeground(activeTextColor);
            } else {
                button.setBackground(defaultColor);
                button.setForeground(defaultTextColor);
            }
        }
    }
}
