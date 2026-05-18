package com.example.KaiST.sgu_admission_system.gui.dialogs;

import com.example.KaiST.sgu_admission_system.bus.XtUserBus;
import com.example.KaiST.sgu_admission_system.entity.XtUser;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class LoginDialog extends JDialog {
    private final XtUserBus userBus;
    private XtUser loggedIn;
    private JTextField cccdField;
    private JTextField ngaySinhField;

    public LoginDialog(Window owner, XtUserBus userBus) {
        super(owner, "Đăng nhập", ModalityType.APPLICATION_MODAL);
        this.userBus = userBus;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        add(createFormPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    public XtUser getLoggedIn() {
        return loggedIn;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        int row = 0;
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        panel.add(new JLabel("CCCD:"), constraints);

        cccdField = new JTextField(24);
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(cccdField, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        panel.add(new JLabel("Ngày sinh (dd/MM/yyyy):"), constraints);

        ngaySinhField = new JTextField(24);
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(ngaySinhField, constraints);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton loginButton = new JButton("Đăng nhập");
        JButton closeButton = new JButton("Thoát");

        loginButton.addActionListener(event -> onLogin());
        closeButton.addActionListener(event -> dispose());

        panel.add(closeButton);
        panel.add(loginButton);
        return panel;
    }

    private void onLogin() {
        String cccd = cccdField.getText().trim();
        String ngaySinh = ngaySinhField.getText().trim();
        if (cccd.isBlank() || ngaySinh.isBlank()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập CCCD và ngày sinh.", "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userBus.findAll().isEmpty()) {
            XtUser admin = new XtUser();
            admin.setCccd(cccd);
            admin.setNgaySinh(ngaySinh);
            admin.setHoTen("Quản trị hệ thống");
            admin.setRole("admin");
            admin.setEnabled(Boolean.TRUE);
            admin.setPassword(ngaySinh);
            userBus.save(admin);
        }

        XtUser user = userBus.login(cccd, ngaySinh);
        if (user == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Thông tin đăng nhập không đúng hoặc tài khoản bị khóa.",
                    "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        loggedIn = user;
        dispose();
    }
}
