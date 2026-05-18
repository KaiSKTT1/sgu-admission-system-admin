package com.example.KaiST.sgu_admission_system.gui.dialogs;

import com.example.KaiST.sgu_admission_system.commen.PhuongThuc;
import com.example.KaiST.sgu_admission_system.entity.XtDiemThiXetTuyen;
import com.example.KaiST.sgu_admission_system.entity.XtThiSinhXetTuyen25;
import com.example.KaiST.sgu_admission_system.gui.components.CardPanel;
import com.example.KaiST.sgu_admission_system.gui.theme.UiTheme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class ThiSinhScoreDialog extends JDialog {
    public ThiSinhScoreDialog(Window owner, XtThiSinhXetTuyen25 candidate, List<XtDiemThiXetTuyen> scores) {
        super(owner, "Điểm thí sinh", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(16, 16, 16, 16));
        wrapper.setBackground(UiTheme.PAGE_BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(12, 12, 12, 12));
        content.setOpaque(false);

        JLabel header = new JLabel(buildHeader(candidate));
        content.add(header);
        content.add(new JLabel(" "));

        if (scores == null || scores.isEmpty()) {
            content.add(new JLabel("Chưa có dữ liệu điểm."));
        } else {
            List<XtDiemThiXetTuyen> thpt = new ArrayList<>();
            List<XtDiemThiXetTuyen> dgnl = new ArrayList<>();
            List<XtDiemThiXetTuyen> vsat = new ArrayList<>();
            List<XtDiemThiXetTuyen> other = new ArrayList<>();

            for (XtDiemThiXetTuyen score : scores) {
                PhuongThuc method = score.getPhuongThuc();
                if (method == PhuongThuc.THPT) {
                    thpt.add(score);
                } else if (method == PhuongThuc.DGNL) {
                    dgnl.add(score);
                } else if (method == PhuongThuc.VSAT) {
                    vsat.add(score);
                } else {
                    other.add(score);
                }
            }

            addSection(content, "Điểm thi THPT", thpt);
            addSection(content, "Điểm ĐGNL", dgnl);
            addSection(content, "Điểm VSAT", vsat);
            addSection(content, "Khác", other);
        }

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);

        JPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.add(scrollPane, BorderLayout.CENTER);

        wrapper.add(card, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        JButton closeButton = new JButton("Đóng");
        closeButton.putClientProperty("JButton.buttonType", "roundRect");
        closeButton.addActionListener(event -> dispose());
        footer.add(closeButton);
        card.add(footer, BorderLayout.SOUTH);

        setSize(520, 540);
        setLocationRelativeTo(owner);
    }

    private void addSection(JPanel parent, String title, List<XtDiemThiXetTuyen> scores) {
        if (scores == null || scores.isEmpty()) {
            return;
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setBorder(new EmptyBorder(6, 0, 6, 0));
        parent.add(titleLabel);

        for (XtDiemThiXetTuyen score : scores) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(new EmptyBorder(4, 12, 8, 12));
            panel.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 6, 3, 6);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;

            int row = 0;
            row = addRow(panel, gbc, row, "Phương thức", phuongThucText(score.getPhuongThuc()));
            row = addRow(panel, gbc, row, "TO", valueOf(score.getTo()));
            row = addRow(panel, gbc, row, "LI", valueOf(score.getLi()));
            row = addRow(panel, gbc, row, "HO", valueOf(score.getHo()));
            row = addRow(panel, gbc, row, "SI", valueOf(score.getSi()));
            row = addRow(panel, gbc, row, "SU", valueOf(score.getSu()));
            row = addRow(panel, gbc, row, "DI", valueOf(score.getDi()));
            row = addRow(panel, gbc, row, "VA", valueOf(score.getVa()));
            row = addRow(panel, gbc, row, "N1_THI", valueOf(score.getN1Thi()));
            row = addRow(panel, gbc, row, "N1_CC", valueOf(score.getN1Cc()));
            row = addRow(panel, gbc, row, "CNCN", valueOf(score.getCncn()));
            row = addRow(panel, gbc, row, "CNNN", valueOf(score.getCnnn()));
            row = addRow(panel, gbc, row, "TI", valueOf(score.getTi()));
            row = addRow(panel, gbc, row, "KTPL", valueOf(score.getKtpl()));
            row = addRow(panel, gbc, row, "NL1", valueOf(score.getNl1()));
            row = addRow(panel, gbc, row, "NK1", valueOf(score.getNk1()));
            addRow(panel, gbc, row, "NK2", valueOf(score.getNk2()));

            parent.add(panel);
        }
    }

    private int addRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        if (value == null || value.isBlank()) {
            return row;
        }
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(new JLabel(value), gbc);
        return row + 1;
    }

    private String buildHeader(XtThiSinhXetTuyen25 candidate) {
        if (candidate == null) {
            return "Thông tin thí sinh";
        }
        String cccd = safeText(candidate.getCccd());
        String sbd = safeText(candidate.getSoBaoDanh());
        String ten = safeText(candidate.getTen());
        return "CCCD: " + cccd + " | SBD: " + sbd + " | Họ tên: " + ten;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String phuongThucText(PhuongThuc method) {
        return method == null ? "" : method.getLabel();
    }

    private String valueOf(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }
}
