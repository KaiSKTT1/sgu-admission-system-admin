package com.example.KaiST.sgu_admission_system.gui.dialogs;

import com.example.KaiST.sgu_admission_system.bus.XtUserBus;
import com.example.KaiST.sgu_admission_system.entity.XtUser;
import com.example.KaiST.sgu_admission_system.gui.components.CardPanel;
import com.example.KaiST.sgu_admission_system.gui.theme.UiTheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.ImageIcon;

public class LoginDialog extends JDialog {
    private final XtUserBus userBus;
    private XtUser loggedIn;
    private JTextField cccdField;
    private JTextField ngaySinhField;

    public LoginDialog(Window owner, XtUserBus userBus) {
        super(owner, "Đăng nhập", ModalityType.APPLICATION_MODAL);
        this.userBus = userBus;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(createBackgroundPanel(), BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(owner);
    }

    public XtUser getLoggedIn() {
        return loggedIn;
    }

    private JPanel createBackgroundPanel() {
        JPanel background = new BackgroundPanel();
        background.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.NONE;

        JPanel card = createLoginCard();
        background.add(card, constraints);
        return background;
    }

    private JPanel createLoginCard() {
        JPanel panel = new CardPanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Đăng nhập hệ thống");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        title.setForeground(UiTheme.TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 0, 6, 0);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        int row = 0;
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 1;
        form.add(createFieldLabel("CCCD"), constraints);

        row++;
        constraints.gridy = row;
        cccdField = new JTextField(24);
        cccdField.putClientProperty("JTextField.placeholderText", "Nhập số CCCD");
        form.add(createIconField(cccdField, loadIcon("/icon/plus.png", 16)), constraints);

        row++;
        constraints.gridy = row;
        form.add(createFieldLabel("Ngày sinh (dd/MM/yyyy)"), constraints);

        row++;
        constraints.gridy = row;
        ngaySinhField = new JTextField(24);
        ngaySinhField.putClientProperty("JTextField.placeholderText", "Ví dụ: 01/01/2006");
        form.add(createIconField(ngaySinhField, loadIcon("/icon/eye.png", 16)), constraints);

        panel.add(form, BorderLayout.CENTER);
        panel.add(createFooterPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);
        JButton loginButton = new JButton("Đăng nhập");
        JButton closeButton = new JButton("Thoát");
        loginButton.putClientProperty("JButton.buttonType", "roundRect");
        closeButton.putClientProperty("JButton.buttonType", "roundRect");
        getRootPane().setDefaultButton(loginButton);

        loginButton.addActionListener(event -> onLogin());
        closeButton.addActionListener(event -> dispose());

        panel.add(closeButton);
        panel.add(loginButton);
        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(UiTheme.TEXT_MUTED);
        return label;
    }

    private JPanel createIconField(JTextField field, ImageIcon icon) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setForeground(UiTheme.TEXT_MUTED);
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private ImageIcon loadIcon(String path, int size) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            return null;
        }
        ImageIcon icon = new ImageIcon(url);
        Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        if (scaled instanceof BufferedImage) {
            return new ImageIcon(scaled);
        }
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        image.getGraphics().drawImage(scaled, 0, 0, null);
        return new ImageIcon(image);
    }

    private final class BackgroundPanel extends JPanel {
        public BackgroundPanel() {
            setBorder(new EmptyBorder(32, 32, 32, 32));
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            Color start = new Color(248, 250, 252);
            Color end = new Color(226, 232, 240);
            g2.setPaint(new java.awt.GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
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
