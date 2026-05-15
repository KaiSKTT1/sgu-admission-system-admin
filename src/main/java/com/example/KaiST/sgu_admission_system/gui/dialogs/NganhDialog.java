package com.example.KaiST.sgu_admission_system.gui.dialogs;

import com.example.KaiST.sgu_admission_system.entity.XtNganh;
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

public class NganhDialog extends JDialog {
    public enum Mode {
        ADD,
        EDIT,
        VIEW
    }

    private final Mode mode;
    private final Map<String, JTextField> fields = new HashMap<>();
    private boolean saved;
    private XtNganh nganh;

    public NganhDialog(Window owner, String title, XtNganh existing, Mode mode) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.mode = mode;
        this.nganh = existing != null ? copyNganh(existing) : new XtNganh();

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

    public XtNganh getNganh() {
        return nganh;
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
        row = addField(panel, constraints, row, "Mã ngành", "manganh", nganh.getMaNganh());
        row = addField(panel, constraints, row, "Tên ngành", "tennganh", nganh.getTenNganh());
        row = addField(panel, constraints, row, "Tổ hợp gốc", "tohopgoc", nganh.getToHopGoc());
        row = addField(panel, constraints, row, "Chỉ tiêu", "chitieu", valueOf(nganh.getChiTieu()));
        row = addField(panel, constraints, row, "Điểm sàn", "diemsan", valueOf(nganh.getDiemSan()));
        addField(panel, constraints, row, "Điểm trúng tuyển", "diemtrungtuyen", valueOf(nganh.getDiemTrungTuyen()));

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
        nganh.setMaNganh(fields.get("manganh").getText().trim());
        nganh.setTenNganh(fields.get("tennganh").getText().trim());
        nganh.setToHopGoc(fields.get("tohopgoc").getText().trim());
        nganh.setChiTieu(parseInteger(fields.get("chitieu").getText().trim()));
        nganh.setDiemSan(parseBigDecimal(fields.get("diemsan").getText().trim()));
        nganh.setDiemTrungTuyen(parseBigDecimal(fields.get("diemtrungtuyen").getText().trim()));
        saved = true;
        dispose();
    }

    private XtNganh copyNganh(XtNganh source) {
        XtNganh copy = new XtNganh();
        copy.setIdNganh(source.getIdNganh());
        copy.setMaNganh(source.getMaNganh());
        copy.setTenNganh(source.getTenNganh());
        copy.setToHopGoc(source.getToHopGoc());
        copy.setChiTieu(source.getChiTieu());
        copy.setDiemSan(source.getDiemSan());
        copy.setDiemTrungTuyen(source.getDiemTrungTuyen());
        copy.setTuyenThang(source.getTuyenThang());
        copy.setDgnl(source.getDgnl());
        copy.setThpt(source.getThpt());
        copy.setVsat(source.getVsat());
        copy.setSlXtt(source.getSlXtt());
        copy.setSlDgnl(source.getSlDgnl());
        copy.setSlVsat(source.getSlVsat());
        copy.setSlThpt(source.getSlThpt());
        return copy;
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private java.math.BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            String normalized = value.trim();
            boolean hasComma = normalized.contains(",");
            boolean hasDot = normalized.contains(".");
            if (hasComma && !hasDot) {
                normalized = normalized.replace(',', '.');
            }
            normalized = normalized.replaceAll("[^0-9.\\-]", "");
            int firstDot = normalized.indexOf('.');
            if (firstDot != -1) {
                normalized = normalized.substring(0, firstDot + 1)
                        + normalized.substring(firstDot + 1).replace(".", "");
            }
            if (normalized.isBlank() || "-".equals(normalized) || ".".equals(normalized)) {
                return null;
            }
            return new java.math.BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
