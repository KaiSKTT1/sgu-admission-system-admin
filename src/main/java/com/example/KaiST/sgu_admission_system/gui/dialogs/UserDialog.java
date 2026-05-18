package com.example.KaiST.sgu_admission_system.gui.dialogs;

import com.example.KaiST.sgu_admission_system.entity.XtUser;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class UserDialog extends JDialog {
    public enum Mode {
        ADD,
        EDIT,
        VIEW
    }

    private final Mode mode;
    private final Map<String, JTextField> fields = new HashMap<>();
    private JPasswordField passwordField;
    private boolean saved;
    private XtUser user;

    public UserDialog(Window owner, String title, XtUser existing, Mode mode) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.mode = mode;
        this.user = existing != null ? copyUser(existing) : new XtUser();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        add(createFormPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }

    public XtUser getUser() {
        return user;
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
        row = addField(panel, constraints, row, "CCCD", "cccd", user.getCccd());
        row = addField(panel, constraints, row, "Họ tên", "hoten", user.getHoTen());
        row = addField(panel, constraints, row, "Ngày sinh", "ngaysinh", user.getNgaySinh());
        row = addField(panel, constraints, row, "Email", "email", user.getEmail());
        row = addField(panel, constraints, row, "Điện thoại", "dienthoai", user.getDienThoai());
        row = addField(panel, constraints, row, "Quyền", "role", user.getRole());
        row = addField(panel, constraints, row, "Trạng thái (true/false)", "enabled",
                user.getEnabled() == null ? "" : user.getEnabled().toString());
        addPasswordField(panel, constraints, row, "Mật khẩu", user.getPassword());

        if (mode == Mode.VIEW) {
            for (JTextField field : fields.values()) {
                field.setEditable(false);
            }
            if (passwordField != null) {
                passwordField.setEditable(false);
            }
        }

        return panel;
    }

    private int addField(JPanel panel, GridBagConstraints constraints, int row,
            String label, String key, String value) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        panel.add(new JLabel(label + ":"), constraints);

        JTextField field = new JTextField(24);
        field.setText(Objects.toString(value, ""));
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(field, constraints);
        fields.put(key, field);

        return row + 1;
    }

    private void addPasswordField(JPanel panel, GridBagConstraints constraints, int row,
            String label, String value) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        panel.add(new JLabel(label + ":"), constraints);

        passwordField = new JPasswordField(24);
        passwordField.setText(Objects.toString(value, ""));
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(passwordField, constraints);
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton closeButton = new JButton(mode == Mode.VIEW ? "Đóng" : "Hủy");
        closeButton.addActionListener(event -> dispose());
        panel.add(closeButton);

        if (mode != Mode.VIEW) {
            JButton saveButton = new JButton("Lưu");
            saveButton.addActionListener(event -> onSave());
            panel.add(saveButton);
        }
        return panel;
    }

    private void onSave() {
        user.setCccd(fields.get("cccd").getText().trim());
        user.setHoTen(fields.get("hoten").getText().trim());
        user.setNgaySinh(fields.get("ngaysinh").getText().trim());
        user.setEmail(fields.get("email").getText().trim());
        user.setDienThoai(fields.get("dienthoai").getText().trim());
        user.setRole(fields.get("role").getText().trim().toLowerCase());
        user.setEnabled(parseBoolean(fields.get("enabled").getText().trim()));
        user.setPassword(passwordField == null ? "" : new String(passwordField.getPassword()).trim());
        saved = true;
        dispose();
    }

    private XtUser copyUser(XtUser source) {
        XtUser copy = new XtUser();
        copy.setIdUser(source.getIdUser());
        copy.setCccd(source.getCccd());
        copy.setHoTen(source.getHoTen());
        copy.setNgaySinh(source.getNgaySinh());
        copy.setEmail(source.getEmail());
        copy.setDienThoai(source.getDienThoai());
        copy.setRole(source.getRole());
        copy.setEnabled(source.getEnabled());
        copy.setPassword(source.getPassword());
        return copy;
    }

    private boolean parseBoolean(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.equals("true")
                || normalized.equals("1")
                || normalized.equals("yes")
                || normalized.equals("enable")
                || normalized.equals("active")
                || normalized.equals("danghoatdong")
                || normalized.equals("hoatdong");
    }
}
