package com.example.KaiST.sgu_admission_system.gui.dialogs;

import com.example.KaiST.sgu_admission_system.entity.XtDiemCongXetTuyen;
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

public class DiemCongXetTuyenDialog extends JDialog {
    public enum Mode {
        ADD,
        EDIT,
        VIEW
    }

    private final Mode mode;
    private final Map<String, JTextField> fields = new HashMap<>();
    private boolean saved;
    private XtDiemCongXetTuyen row;

    public DiemCongXetTuyenDialog(Window owner, String title, XtDiemCongXetTuyen existing, Mode mode) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.mode = mode;
        this.row = existing != null ? copyRow(existing) : new XtDiemCongXetTuyen();

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

    public XtDiemCongXetTuyen getRow() {
        return row;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        int rowIndex = 0;
        rowIndex = addField(panel, constraints, rowIndex, "CCCD", "ts_cccd", row.getTsCccd());
        rowIndex = addField(panel, constraints, rowIndex, "Mã ngành", "manganh", row.getMaNganh());
        rowIndex = addField(panel, constraints, rowIndex, "Mã tổ hợp", "matohop", row.getMaToHop());
        rowIndex = addField(panel, constraints, rowIndex, "Phương thức", "phuongthuc", row.getPhuongThuc());
        rowIndex = addField(panel, constraints, rowIndex, "Điểm CC", "diemcc", valueOf(row.getDiemCc()));
        rowIndex = addField(panel, constraints, rowIndex, "Điểm ƯTXT", "diemutxt", valueOf(row.getDiemUtxt()));
        rowIndex = addField(panel, constraints, rowIndex, "Điểm tổng", "diemtong", valueOf(row.getDiemTong()));
        rowIndex = addField(panel, constraints, rowIndex, "Ghi chú", "ghichu", row.getGhiChu());
        addField(panel, constraints, rowIndex, "DC keys", "dc_keys", row.getDcKeys());

        if (mode == Mode.VIEW) {
            for (JTextField field : fields.values()) {
                field.setEditable(false);
            }
        }

        return panel;
    }

    private int addField(JPanel panel, GridBagConstraints constraints, int rowIndex,
            String label, String key, String value) {
        constraints.gridx = 0;
        constraints.gridy = rowIndex;
        constraints.weightx = 0;
        panel.add(new JLabel(label + ":"), constraints);

        JTextField field = new JTextField(24);
        field.setText(Objects.toString(value, ""));
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(field, constraints);
        fields.put(key, field);

        return rowIndex + 1;
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
        row.setTsCccd(fields.get("ts_cccd").getText().trim());
        row.setMaNganh(fields.get("manganh").getText().trim());
        row.setMaToHop(fields.get("matohop").getText().trim());
        row.setPhuongThuc(fields.get("phuongthuc").getText().trim());
        row.setDiemCc(parseBigDecimal(fields.get("diemcc").getText().trim()));
        row.setDiemUtxt(parseBigDecimal(fields.get("diemutxt").getText().trim()));
        row.setDiemTong(parseBigDecimal(fields.get("diemtong").getText().trim()));
        row.setGhiChu(fields.get("ghichu").getText().trim());
        row.setDcKeys(fields.get("dc_keys").getText().trim());
        saved = true;
        dispose();
    }

    private XtDiemCongXetTuyen copyRow(XtDiemCongXetTuyen source) {
        XtDiemCongXetTuyen copy = new XtDiemCongXetTuyen();
        copy.setIdDiemCong(source.getIdDiemCong());
        copy.setTsCccd(source.getTsCccd());
        copy.setMaNganh(source.getMaNganh());
        copy.setMaToHop(source.getMaToHop());
        copy.setPhuongThuc(source.getPhuongThuc());
        copy.setDiemCc(source.getDiemCc());
        copy.setDiemUtxt(source.getDiemUtxt());
        copy.setDiemTong(source.getDiemTong());
        copy.setGhiChu(source.getGhiChu());
        copy.setDcKeys(source.getDcKeys());
        return copy;
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value);
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
