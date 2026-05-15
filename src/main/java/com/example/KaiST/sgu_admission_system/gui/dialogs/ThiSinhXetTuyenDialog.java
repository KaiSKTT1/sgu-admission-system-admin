package com.example.KaiST.sgu_admission_system.gui.dialogs;

import com.example.KaiST.sgu_admission_system.entity.XtThiSinhXetTuyen25;
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
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class ThiSinhXetTuyenDialog extends JDialog {
    public enum Mode {
        ADD,
        EDIT,
        VIEW
    }

    private final Mode mode;
    private final Map<String, JTextField> fields = new HashMap<>();
    private boolean saved;
    private XtThiSinhXetTuyen25 candidate;

    public ThiSinhXetTuyenDialog(Window owner, String title, XtThiSinhXetTuyen25 existing, Mode mode) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.mode = mode;
        this.candidate = existing != null ? copyCandidate(existing) : new XtThiSinhXetTuyen25();

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

    public XtThiSinhXetTuyen25 getCandidate() {
        return candidate;
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
        row = addField(panel, constraints, row, "CCCD", "cccd", candidate.getCccd());
        row = addField(panel, constraints, row, "Số báo danh", "sobaodanh", candidate.getSoBaoDanh());
        row = addField(panel, constraints, row, "Tên", "ten", candidate.getTen());
        row = addField(panel, constraints, row, "Ngày sinh", "ngaysinh", candidate.getNgaySinh());
        row = addField(panel, constraints, row, "Giới tính", "gioitinh", candidate.getGioiTinh());
        row = addField(panel, constraints, row, "Điện thoại", "dienthoai", candidate.getDienThoai());
        row = addField(panel, constraints, row, "Email", "email", candidate.getEmail());
        row = addField(panel, constraints, row, "Nơi sinh", "noisinh", candidate.getNoiSinh());
        row = addField(panel, constraints, row, "Đối tượng", "doituong", candidate.getDoiTuong());
        addField(panel, constraints, row, "Khu vực", "khuvuc", candidate.getKhuVuc());

        if (mode == Mode.VIEW) {
            for (JTextField field : fields.values()) {
                field.setEditable(false);
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
        candidate.setCccd(fields.get("cccd").getText().trim());
        candidate.setSoBaoDanh(fields.get("sobaodanh").getText().trim());
        candidate.setTen(fields.get("ten").getText().trim());
        candidate.setNgaySinh(fields.get("ngaysinh").getText().trim());
        candidate.setGioiTinh(fields.get("gioitinh").getText().trim());
        candidate.setDienThoai(fields.get("dienthoai").getText().trim());
        candidate.setEmail(fields.get("email").getText().trim());
        candidate.setNoiSinh(fields.get("noisinh").getText().trim());
        candidate.setDoiTuong(fields.get("doituong").getText().trim());
        candidate.setKhuVuc(fields.get("khuvuc").getText().trim());
        saved = true;
        dispose();
    }

    private XtThiSinhXetTuyen25 copyCandidate(XtThiSinhXetTuyen25 source) {
        XtThiSinhXetTuyen25 copy = new XtThiSinhXetTuyen25();
        copy.setIdThiSinh(source.getIdThiSinh());
        copy.setCccd(source.getCccd());
        copy.setSoBaoDanh(source.getSoBaoDanh());
        copy.setTen(source.getTen());
        copy.setNgaySinh(source.getNgaySinh());
        copy.setGioiTinh(source.getGioiTinh());
        copy.setDienThoai(source.getDienThoai());
        copy.setEmail(source.getEmail());
        copy.setNoiSinh(source.getNoiSinh());
        copy.setDoiTuong(source.getDoiTuong());
        copy.setKhuVuc(source.getKhuVuc());
        copy.setPassword(source.getPassword());
        copy.setUpdatedAt(source.getUpdatedAt());
        return copy;
    }
}
