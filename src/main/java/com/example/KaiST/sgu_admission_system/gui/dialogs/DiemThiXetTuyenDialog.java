package com.example.KaiST.sgu_admission_system.gui.dialogs;

import com.example.KaiST.sgu_admission_system.commen.PhuongThuc;
import com.example.KaiST.sgu_admission_system.entity.XtDiemThiXetTuyen;
import com.example.KaiST.sgu_admission_system.gui.components.CardPanel;
import com.example.KaiST.sgu_admission_system.gui.theme.UiTheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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

public class DiemThiXetTuyenDialog extends JDialog {
    public enum Mode {
        ADD,
        EDIT,
        VIEW
    }

    private final Mode mode;
    private final Map<String, JTextField> fields = new HashMap<>();
    private boolean saved;
    private XtDiemThiXetTuyen score;

    public DiemThiXetTuyenDialog(Window owner, String title, XtDiemThiXetTuyen existing, Mode mode) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.mode = mode;
        this.score = existing != null ? copyScore(existing) : new XtDiemThiXetTuyen();

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

    public XtDiemThiXetTuyen getScore() {
        return score;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setOpaque(false);
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        GridBagConstraints leftConstraints = new GridBagConstraints();
        leftConstraints.insets = new Insets(6, 6, 6, 6);
        leftConstraints.anchor = GridBagConstraints.WEST;
        leftConstraints.fill = GridBagConstraints.HORIZONTAL;
        leftConstraints.weightx = 1;

        GridBagConstraints rightConstraints = new GridBagConstraints();
        rightConstraints.insets = new Insets(6, 6, 6, 6);
        rightConstraints.anchor = GridBagConstraints.WEST;
        rightConstraints.fill = GridBagConstraints.HORIZONTAL;
        rightConstraints.weightx = 1;

        String[][] fieldsData = new String[][] {
                { "CCCD", "cccd", score.getCccd() },
                { "Số báo danh", "sobaodanh", score.getSoBaoDanh() },
                { "Phương thức", "phuongthuc", phuongThucText(score.getPhuongThuc()) },
                { "TO", "to", valueOf(score.getTo()) },
                { "LI", "li", valueOf(score.getLi()) },
                { "HO", "ho", valueOf(score.getHo()) },
                { "SI", "si", valueOf(score.getSi()) },
                { "SU", "su", valueOf(score.getSu()) },
                { "DI", "di", valueOf(score.getDi()) },
                { "VA", "va", valueOf(score.getVa()) },
                { "N1_THI", "n1thi", valueOf(score.getN1Thi()) },
                { "N1_CC", "n1cc", valueOf(score.getN1Cc()) },
                { "CNCN", "cncn", valueOf(score.getCncn()) },
                { "CNNN", "cnnn", valueOf(score.getCnnn()) },
                { "TI", "ti", valueOf(score.getTi()) },
                { "KTPL", "ktpl", valueOf(score.getKtpl()) },
                { "NL1", "nl1", valueOf(score.getNl1()) },
                { "NK1", "nk1", valueOf(score.getNk1()) },
                { "NK2", "nk2", valueOf(score.getNk2()) }
        };

        int leftRow = 0;
        int rightRow = 0;
        for (int i = 0; i < fieldsData.length; i++) {
            String label = fieldsData[i][0];
            String key = fieldsData[i][1];
            String value = fieldsData[i][2];
            if (i % 2 == 0) {
                leftRow = addField(leftPanel, leftConstraints, leftRow, label, key, value);
            } else {
                rightRow = addField(rightPanel, rightConstraints, rightRow, label, key, value);
            }
        }

        panel.add(leftPanel);
        panel.add(rightPanel);

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
        score.setCccd(fields.get("cccd").getText().trim());
        score.setSoBaoDanh(fields.get("sobaodanh").getText().trim());
        score.setPhuongThuc(PhuongThuc.fromText(fields.get("phuongthuc").getText().trim()));
        score.setTo(parseBigDecimal(fields.get("to").getText().trim()));
        score.setLi(parseBigDecimal(fields.get("li").getText().trim()));
        score.setHo(parseBigDecimal(fields.get("ho").getText().trim()));
        score.setSi(parseBigDecimal(fields.get("si").getText().trim()));
        score.setSu(parseBigDecimal(fields.get("su").getText().trim()));
        score.setDi(parseBigDecimal(fields.get("di").getText().trim()));
        score.setVa(parseBigDecimal(fields.get("va").getText().trim()));
        score.setN1Thi(parseBigDecimal(fields.get("n1thi").getText().trim()));
        score.setN1Cc(parseBigDecimal(fields.get("n1cc").getText().trim()));
        score.setCncn(parseBigDecimal(fields.get("cncn").getText().trim()));
        score.setCnnn(parseBigDecimal(fields.get("cnnn").getText().trim()));
        score.setTi(parseBigDecimal(fields.get("ti").getText().trim()));
        score.setKtpl(parseBigDecimal(fields.get("ktpl").getText().trim()));
        score.setNl1(parseBigDecimal(fields.get("nl1").getText().trim()));
        score.setNk1(parseBigDecimal(fields.get("nk1").getText().trim()));
        score.setNk2(parseBigDecimal(fields.get("nk2").getText().trim()));
        saved = true;
        dispose();
    }

    private XtDiemThiXetTuyen copyScore(XtDiemThiXetTuyen source) {
        XtDiemThiXetTuyen copy = new XtDiemThiXetTuyen();
        copy.setIdDiemThi(source.getIdDiemThi());
        copy.setCccd(source.getCccd());
        copy.setSoBaoDanh(source.getSoBaoDanh());
        copy.setPhuongThuc(source.getPhuongThuc());
        copy.setTo(source.getTo());
        copy.setLi(source.getLi());
        copy.setHo(source.getHo());
        copy.setSi(source.getSi());
        copy.setSu(source.getSu());
        copy.setDi(source.getDi());
        copy.setVa(source.getVa());
        copy.setN1Thi(source.getN1Thi());
        copy.setN1Cc(source.getN1Cc());
        copy.setCncn(source.getCncn());
        copy.setCnnn(source.getCnnn());
        copy.setTi(source.getTi());
        copy.setKtpl(source.getKtpl());
        copy.setNl1(source.getNl1());
        copy.setNk1(source.getNk1());
        copy.setNk2(source.getNk2());
        return copy;
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String phuongThucText(PhuongThuc method) {
        return method == null ? "" : method.getLabel();
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
