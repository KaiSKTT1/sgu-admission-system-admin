package com.example.KaiST.sgu_admission_system.gui.dialogs;

import com.example.KaiST.sgu_admission_system.entity.XtToHopMonThi;
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

public class ToHopMonThiDialog extends JDialog {
    public enum Mode {
        ADD,
        EDIT,
        VIEW
    }

    private final Mode mode;
    private final Map<String, JTextField> fields = new HashMap<>();
    private boolean saved;
    private XtToHopMonThi toHop;

    public ToHopMonThiDialog(Window owner, String title, XtToHopMonThi existing, Mode mode) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.mode = mode;
        this.toHop = existing != null ? copyToHop(existing) : new XtToHopMonThi();

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

    public XtToHopMonThi getToHop() {
        return toHop;
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
        row = addField(panel, constraints, row, "Mã tổ hợp", "matohop", toHop.getMaToHop());
        row = addField(panel, constraints, row, "Tên tổ hợp", "tentohop", toHop.getTenToHop());
        row = addField(panel, constraints, row, "Môn 1", "mon1", toHop.getMon1());
        row = addField(panel, constraints, row, "Môn 2", "mon2", toHop.getMon2());
        addField(panel, constraints, row, "Môn 3", "mon3", toHop.getMon3());

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
        toHop.setMaToHop(fields.get("matohop").getText().trim());
        toHop.setTenToHop(fields.get("tentohop").getText().trim());
        toHop.setMon1(fields.get("mon1").getText().trim());
        toHop.setMon2(fields.get("mon2").getText().trim());
        toHop.setMon3(fields.get("mon3").getText().trim());
        saved = true;
        dispose();
    }

    private XtToHopMonThi copyToHop(XtToHopMonThi source) {
        XtToHopMonThi copy = new XtToHopMonThi();
        copy.setIdToHop(source.getIdToHop());
        copy.setMaToHop(source.getMaToHop());
        copy.setTenToHop(source.getTenToHop());
        copy.setMon1(source.getMon1());
        copy.setMon2(source.getMon2());
        copy.setMon3(source.getMon3());
        return copy;
    }
}
