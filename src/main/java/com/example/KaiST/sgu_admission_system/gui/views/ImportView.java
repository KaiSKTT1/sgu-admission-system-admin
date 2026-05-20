package com.example.KaiST.sgu_admission_system.gui.views;

import com.example.KaiST.sgu_admission_system.gui.theme.UiTheme;
import com.example.KaiST.sgu_admission_system.gui.controllers.ImportController;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.util.List;

public class ImportView extends JPanel {

    private ImportController controller;

    private JLabel titleLabel;
    private JComboBox<String> dataTypeCombo;

    private JPanel dropZonePanel;
    private JLabel dropZoneIcon;
    private JLabel dropZoneText;
    private JLabel dropZoneHint;
    private JButton chooseFileBtn;
    private JLabel selectedFileLabel;

    private JButton importBtn;
    private JButton clearBtn;
    private JButton downloadTemplateBtn;

    private JLabel statusLabel;
    private JProgressBar progressBar;

    public ImportView() {
        initComponents();
        layoutComponents();
        setupDragAndDrop();
    }

    private void initComponents() {
        setBackground(UiTheme.PAGE_BG);

        titleLabel = new JLabel("Import dữ liệu từ Excel");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(UiTheme.TEXT_DARK);

        String[] dataTypes = {
                "Thí sinh xét tuyển",
                "Điểm thi xét tuyển",
                "Điểm cộng xét tuyển",
                "Ngành",
                "Tổ hợp môn thi",
                "Nguyện vọng xét tuyển"
        };
        dataTypeCombo = new JComboBox<>(dataTypes);
        dataTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dataTypeCombo.setBackground(Color.WHITE);

        dropZonePanel = new JPanel();
        dropZonePanel.setBackground(new Color(245, 248, 255));
        dropZonePanel.setBorder(BorderFactory.createDashedBorder(
                new Color(100, 149, 237), 2, 8, 4, true));
        dropZonePanel.setLayout(new BoxLayout(dropZonePanel, BoxLayout.Y_AXIS));
        dropZonePanel.setPreferredSize(new Dimension(600, 180));
        dropZonePanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        dropZoneIcon = new JLabel("📂", SwingConstants.CENTER);
        dropZoneIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        dropZoneIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        dropZoneText = new JLabel("Kéo thả file Excel vào đây", SwingConstants.CENTER);
        dropZoneText.setFont(new Font("Segoe UI", Font.BOLD, 15));
        dropZoneText.setForeground(new Color(60, 90, 180));
        dropZoneText.setAlignmentX(Component.CENTER_ALIGNMENT);

        dropZoneHint = new JLabel("Hỗ trợ định dạng: .xlsx, .xls", SwingConstants.CENTER);
        dropZoneHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dropZoneHint.setForeground(UiTheme.TEXT_MUTED);
        dropZoneHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        chooseFileBtn = new JButton("Chọn file");
        chooseFileBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chooseFileBtn.setBackground(UiTheme.PRIMARY);
        chooseFileBtn.setForeground(Color.WHITE);
        chooseFileBtn.setBorderPainted(false);
        chooseFileBtn.setFocusPainted(false);
        chooseFileBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chooseFileBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        chooseFileBtn.setMaximumSize(new Dimension(120, 34));

        selectedFileLabel = new JLabel("Chưa chọn file", SwingConstants.LEFT);
        selectedFileLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        selectedFileLabel.setForeground(UiTheme.TEXT_MUTED);

        importBtn = new JButton("⬆  Import");
        importBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        importBtn.setBackground(new Color(34, 139, 34));
        importBtn.setForeground(Color.WHITE);
        importBtn.setBorderPainted(false);
        importBtn.setFocusPainted(false);
        importBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        importBtn.setEnabled(false);

        clearBtn = new JButton("✕  Xóa");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clearBtn.setBackground(new Color(200, 60, 60));
        clearBtn.setForeground(Color.WHITE);
        clearBtn.setBorderPainted(false);
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.setEnabled(false);

        downloadTemplateBtn = new JButton("⬇  Tải template");
        downloadTemplateBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        downloadTemplateBtn.setBackground(new Color(0, 120, 180));
        downloadTemplateBtn.setForeground(Color.WHITE);
        downloadTemplateBtn.setBorderPainted(false);
        downloadTemplateBtn.setFocusPainted(false);
        downloadTemplateBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(UiTheme.TEXT_DARK);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(400, 22));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(downloadTemplateBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // CENTER
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Step 1: Loại dữ liệu
        JPanel step1Panel = createStepPanel("1. Chọn loại dữ liệu cần import");
        JPanel comboRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        comboRow.setOpaque(false);
        dataTypeCombo.setPreferredSize(new Dimension(280, 32));
        comboRow.add(dataTypeCombo);
        step1Panel.add(comboRow);
        centerPanel.add(step1Panel);
        centerPanel.add(Box.createVerticalStrut(18));

        // Step 2: Upload file
        JPanel step2Panel = createStepPanel("2. Chọn hoặc kéo thả file Excel");
        dropZonePanel.add(Box.createVerticalStrut(18));
        dropZonePanel.add(dropZoneIcon);
        dropZonePanel.add(Box.createVerticalStrut(8));
        dropZonePanel.add(dropZoneText);
        dropZonePanel.add(Box.createVerticalStrut(4));
        dropZonePanel.add(dropZoneHint);
        dropZonePanel.add(Box.createVerticalStrut(14));
        dropZonePanel.add(chooseFileBtn);
        dropZonePanel.add(Box.createVerticalStrut(12));
        step2Panel.add(dropZonePanel);
        step2Panel.add(Box.createVerticalStrut(6));

        JPanel fileInfoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fileInfoRow.setOpaque(false);
        fileInfoRow.add(new JLabel("📄 "));
        fileInfoRow.add(selectedFileLabel);
        step2Panel.add(fileInfoRow);

        centerPanel.add(step2Panel);
        centerPanel.add(Box.createVerticalStrut(18));

        // Step 3: Action buttons
        JPanel step3Panel = createStepPanel("3. Thực hiện import");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnPanel.setOpaque(false);
        importBtn.setPreferredSize(new Dimension(130, 36));
        clearBtn.setPreferredSize(new Dimension(100, 36));
        btnPanel.add(importBtn);
        btnPanel.add(clearBtn);
        step3Panel.add(btnPanel);
        step3Panel.add(Box.createVerticalStrut(10));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPanel.setOpaque(false);
        statusPanel.add(statusLabel);
        step3Panel.add(statusPanel);

        JPanel progressRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        progressRow.setOpaque(false);
        progressRow.add(progressBar);
        step3Panel.add(progressRow);

        centerPanel.add(step3Panel);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createStepPanel(String title) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(40, 60, 140));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(200, 210, 235));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    private void setupDragAndDrop() {
        new DropTarget(dropZonePanel, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                dropZonePanel.setBackground(new Color(210, 228, 255));
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                dropZonePanel.setBackground(new Color(245, 248, 255));
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                dropZonePanel.setBackground(new Color(245, 248, 255));
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> files = (List<File>) dtde.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty() && controller != null) {
                        controller.onFileSelected(files.get(0));
                    }
                } catch (Exception ex) {
                    setStatus("Lỗi kéo thả file: " + ex.getMessage(), false);
                }
            }
        });
    }

    // ===== PUBLIC API =====

    public void setController(ImportController controller) {
        this.controller = controller;
        chooseFileBtn.addActionListener(e -> controller.onChooseFileClicked());
        importBtn.addActionListener(e -> controller.onImportClicked());
        clearBtn.addActionListener(e -> controller.onClearClicked());
        downloadTemplateBtn.addActionListener(e -> controller.onDownloadTemplate());
        dataTypeCombo.addActionListener(e -> controller.onDataTypeChanged(getSelectedDataType()));
        dropZonePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                controller.onChooseFileClicked();
            }
        });
    }

    public String getSelectedDataType() {
        return (String) dataTypeCombo.getSelectedItem();
    }

    public void setSelectedFile(File file) {
        if (file != null) {
            selectedFileLabel.setText(file.getName() + "  (" + (file.length() / 1024) + " KB)");
            selectedFileLabel.setForeground(new Color(30, 130, 30));
            dropZoneIcon.setText("✅");
            importBtn.setEnabled(true);
            clearBtn.setEnabled(true);
        }
    }

    public void clearAll() {
        selectedFileLabel.setText("Chưa chọn file");
        selectedFileLabel.setForeground(UiTheme.TEXT_MUTED);
        dropZoneIcon.setText("📂");
        importBtn.setEnabled(false);
        clearBtn.setEnabled(false);
        setStatus(" ", true);
        progressBar.setVisible(false);
    }

    public void setStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setForeground(success ? new Color(30, 130, 30) : new Color(180, 30, 30));
    }

    public void showProgress(boolean visible) {
        progressBar.setVisible(visible);
        progressBar.setValue(0);
    }

    public void setProgress(int value) {
        progressBar.setValue(value);
        progressBar.setString(value + "%");
    }

    public void setImportEnabled(boolean enabled) {
        importBtn.setEnabled(enabled);
    }
}