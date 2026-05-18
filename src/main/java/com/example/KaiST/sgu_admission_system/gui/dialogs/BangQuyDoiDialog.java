package com.example.KaiST.sgu_admission_system.gui.dialogs;

import com.example.KaiST.sgu_admission_system.entity.XtBangQuyDoi;
import com.example.KaiST.sgu_admission_system.gui.components.CardPanel;
import com.example.KaiST.sgu_admission_system.gui.theme.UiTheme;
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

public class BangQuyDoiDialog extends JDialog {
    public enum Mode {
        ADD,
        EDIT,
        VIEW
    }

    private final Mode mode;
    private final Map<String, JTextField> fields = new HashMap<>();
    private boolean saved;
    private XtBangQuyDoi row;

    public BangQuyDoiDialog(Window owner, String title, XtBangQuyDoi existing, Mode mode) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.mode = mode;
        this.row = existing != null ? copyRow(existing) : new XtBangQuyDoi();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));
        wrapper.setBackground(UiTheme.PAGE_BG);

        JPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(createFormPanel(), BorderLayout.CENTER);
        card.add(createFooterPanel(), BorderLayout.SOUTH);

        wrapper.add(card, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isSaved() {
        return saved;
    }

    public XtBangQuyDoi getRow() {
        return row;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        int rowIndex = 0;
        rowIndex = addField(panel, constraints, rowIndex, "Phương thức", "phuongthuc", row.getPhuongThuc());
        rowIndex = addField(panel, constraints, rowIndex, "Tổ hợp", "tohop", row.getToHop());
        rowIndex = addField(panel, constraints, rowIndex, "Môn", "mon", row.getMon());
        rowIndex = addField(panel, constraints, rowIndex, "Điểm A", "diema", valueOf(row.getDiemA()));
        rowIndex = addField(panel, constraints, rowIndex, "Điểm B", "diemb", valueOf(row.getDiemB()));
        rowIndex = addField(panel, constraints, rowIndex, "Điểm C", "diemc", valueOf(row.getDiemC()));
        rowIndex = addField(panel, constraints, rowIndex, "Điểm D", "diemd", valueOf(row.getDiemD()));
        rowIndex = addField(panel, constraints, rowIndex, "Mã quy đổi", "maquydoi", row.getMaQuyDoi());
        addField(panel, constraints, rowIndex, "Phân vị", "phanvi", row.getPhanVi());

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
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);
        JButton closeButton = new JButton(mode == Mode.VIEW ? "Đóng" : "Hủy");
        closeButton.addActionListener(event -> dispose());
        panel.add(closeButton);

        if (mode != Mode.VIEW) {
            JButton saveButton = new JButton("Lưu");
            saveButton.putClientProperty("JButton.buttonType", "roundRect");
            saveButton.addActionListener(event -> onSave());
            panel.add(saveButton);
        }
        return panel;
    }

    private void onSave() {
        row.setPhuongThuc(fields.get("phuongthuc").getText().trim());
        row.setToHop(fields.get("tohop").getText().trim());
        row.setMon(fields.get("mon").getText().trim());
        row.setDiemA(parseBigDecimal(fields.get("diema").getText().trim()));
        row.setDiemB(parseBigDecimal(fields.get("diemb").getText().trim()));
        row.setDiemC(parseBigDecimal(fields.get("diemc").getText().trim()));
        row.setDiemD(parseBigDecimal(fields.get("diemd").getText().trim()));
        row.setMaQuyDoi(fields.get("maquydoi").getText().trim());
        row.setPhanVi(fields.get("phanvi").getText().trim());
        saved = true;
        dispose();
    }

    private XtBangQuyDoi copyRow(XtBangQuyDoi source) {
        XtBangQuyDoi copy = new XtBangQuyDoi();
        copy.setIdQd(source.getIdQd());
        copy.setPhuongThuc(source.getPhuongThuc());
        copy.setToHop(source.getToHop());
        copy.setMon(source.getMon());
        copy.setDiemA(source.getDiemA());
        copy.setDiemB(source.getDiemB());
        copy.setDiemC(source.getDiemC());
        copy.setDiemD(source.getDiemD());
        copy.setMaQuyDoi(source.getMaQuyDoi());
        copy.setPhanVi(source.getPhanVi());
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
