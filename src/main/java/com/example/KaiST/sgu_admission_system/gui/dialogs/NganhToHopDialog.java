package com.example.KaiST.sgu_admission_system.gui.dialogs;

import com.example.KaiST.sgu_admission_system.entity.XtNganh;
import com.example.KaiST.sgu_admission_system.entity.XtNganhToHop;
import com.example.KaiST.sgu_admission_system.entity.XtToHopMonThi;
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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

public class NganhToHopDialog extends JDialog {
    public enum Mode {
        VIEW
    }

    public NganhToHopDialog(Window owner, String title, XtNganhToHop data, 
            XtNganh nganh, XtToHopMonThi toHop, Mode mode) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.data = data;
        this.nganh = nganh;
        this.toHop = toHop;

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

    private final Map<String, JTextField> fields = new HashMap<>();
    private final XtNganhToHop data;
    private final XtNganh nganh;
    private final XtToHopMonThi toHop;

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        int row = 0;
        
        // Mã ngành và Tên ngành
        row = addField(panel, constraints, row, "Mã ngành", "manganh", data.getMaNganh());
        row = addField(panel, constraints, row, "Tên ngành", "tennganh", 
                nganh != null ? nganh.getTenNganh() : "");

        // Mã tổ hợp và Tên tổ hợp
        row = addField(panel, constraints, row, "Mã tổ hợp", "matohop", data.getMaToHop());
        row = addField(panel, constraints, row, "Tên tổ hợp", "tentohop", 
                toHop != null ? toHop.getTenToHop() : "");

        // Môn 1
        row = addField(panel, constraints, row, "Môn 1", "thmon1", data.getThMon1());
        row = addField(panel, constraints, row, "Hệ số môn 1", "hsmon1", 
                data.getHsMon1() != null ? String.valueOf(data.getHsMon1()) : "");

        // Môn 2
        row = addField(panel, constraints, row, "Môn 2", "thmon2", data.getThMon2());
        row = addField(panel, constraints, row, "Hệ số môn 2", "hsmon2", 
                data.getHsMon2() != null ? String.valueOf(data.getHsMon2()) : "");

        // Môn 3
        row = addField(panel, constraints, row, "Môn 3", "thmon3", data.getThMon3());
        row = addField(panel, constraints, row, "Hệ số môn 3", "hsmon3", 
                data.getHsMon3() != null ? String.valueOf(data.getHsMon3()) : "");

        // TB Keys
        row = addField(panel, constraints, row, "TB Keys", "tbkeys", data.getTbKeys());

        // Tất cả fields không editable vì chỉ VIEW mode
        for (JTextField field : fields.values()) {
            field.setEditable(false);
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
        field.setText(value != null ? value : "");
        field.setEditable(false);
        fields.put(key, field);

        constraints.gridx = 1;
        constraints.gridy = row;
        constraints.weightx = 1;
        panel.add(field, constraints);

        return row + 1;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);

        JButton closeButton = new JButton("Đóng");
        closeButton.addActionListener(event -> dispose());

        panel.add(closeButton);
        return panel;
    }
}
