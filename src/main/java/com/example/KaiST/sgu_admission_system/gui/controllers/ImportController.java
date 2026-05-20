package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.gui.views.ImportView;
import com.example.KaiST.sgu_admission_system.utils.CandidateImporter;
import com.example.KaiST.sgu_admission_system.utils.DiemTohopCalculator;
import com.example.KaiST.sgu_admission_system.utils.NganhToHopImporter;
import com.example.KaiST.sgu_admission_system.utils.NguyenVongImporter;
import com.example.KaiST.sgu_admission_system.utils.NguyenVongScoreUpdater;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ImportController {

    private final ImportView view;
    private File selectedFile;
    private String currentDataType;

    private static final Map<String, String[]> DATA_TYPE_COLUMNS = new LinkedHashMap<>();

    static {
        DATA_TYPE_COLUMNS.put("Thí sinh xét tuyển",
                new String[] { "STT", "CCCD", "Họ tên", "Ngày sinh", "Giới tính",
                        "Đối tượng", "Khu vực", "Toán", "Văn", "Lý", "Hóa", "Sinh",
                        "Sử", "Địa", "Ngoại ngữ", "Mã môn N1", "Điểm N1", "KTPL",
                        "Tiếng Anh", "CNCN", "CNNN", "NK1", "NK2", "Nơi sinh" });
        DATA_TYPE_COLUMNS.put("Nguyện vọng xét tuyển",
                new String[] { "STT", "CCCD", "Thứ tự NV", "Mã trường",
                        "Tên trường", "Mã xét tuyển", "Tên mã xét tuyển",
                        "Nguyện vọng tuyển thẳng (điều 8)" });
        DATA_TYPE_COLUMNS.put("Ngành - Tổ hợp môn",
                new String[] { "STT", "MANGANH", "TEN_NGANHCHUAN", "MA_TO_HOP",
                        "tb_keys", "TEN_TO_HOP", "Gốc", "Độ lệch" });
    }

    public ImportController(ImportView view) {
        this.view = view;
    }

    public void init() {
        // Đồng bộ currentDataType với item đang chọn trên view
        currentDataType = view.getSelectedDataType();
    }

    // ── Chọn file ────────────────────────────────────────────────────────────

    public void onChooseFileClicked() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file Excel");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Excel files (*.xlsx, *.xls)", "xlsx", "xls"));
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
            onFileSelected(chooser.getSelectedFile());
        }
    }

    public void onFileSelected(File file) {
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".xlsx") && !name.endsWith(".xls")) {
            view.setStatus("❌ Chỉ chấp nhận file .xlsx hoặc .xls!", false);
            return;
        }
        selectedFile = file;
        view.setSelectedFile(file);
        view.setStatus("✔ Đã chọn: " + file.getName(), true);
    }

    // ── Import ───────────────────────────────────────────────────────────────

    public void onImportClicked() {
        if (selectedFile == null) {
            view.setStatus("❌ Vui lòng chọn file trước!", false);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Bạn có chắc muốn import \"" + currentDataType + "\"\ntừ file: " + selectedFile.getName() + "?",
                "Xác nhận import",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        view.setImportEnabled(false);
        view.showProgress(true);
        view.setStatus("Đang import...", true);

        final String filePath = selectedFile.getAbsolutePath();
        final String dataType = currentDataType;

        SwingWorker<String, Integer> worker = new SwingWorker<>() {

            @Override
            protected String doInBackground() {
                publish(10);

                return switch (dataType) {
                    case "Thí sinh xét tuyển" -> {
                        publish(30);
                        CandidateImporter.ImportResult result = CandidateImporter.importFromExcel(filePath);
                        publish(90);
                        yield buildResultMessage(dataType, result.totalSuccess,
                                result.totalSkipDuplicate, result.totalSkipError,
                                result.logPath);
                    }

                    case "Nguyện vọng xét tuyển" -> {
                        // Bước 1: Import nguyện vọng từ Excel
                        publish(20);
                        SwingUtilities.invokeLater(() -> view.setStatus("Đang import nguyện vọng...", true));
                        NguyenVongImporter.ImportResult importResult = NguyenVongImporter.importFromExcel(filePath);

                        // Bước 2: Điền tt_thm từ n_tohopgoc
                        publish(50);
                        SwingUtilities.invokeLater(() -> view.setStatus("Đang điền tổ hợp môn...", true));
                        NguyenVongScoreUpdater.fillTtThmFromNganhGoc();

                        // Bước 3: Tính điểm tổ hợp → cập nhật diem_thxt, diem_xettuyen
                        publish(70);
                        SwingUtilities.invokeLater(() -> view.setStatus("Đang tính điểm tổ hợp...", true));
                        DiemTohopCalculator.calculateAndUpdate();

                        publish(90);
                        yield buildResultMessage(dataType, importResult.totalSuccess,
                                importResult.totalSkipDuplicate, importResult.totalSkipError,
                                importResult.logPath);
                    }

                    case "Ngành - Tổ hợp môn" -> {
                        publish(30);
                        NganhToHopImporter.ImportResult result = NganhToHopImporter.importFromExcel(filePath);
                        publish(90);
                        yield buildResultMessage(dataType, result.totalSuccess,
                                result.totalSkipDuplicate, result.totalSkipError,
                                result.logPath);
                    }

                    default -> "❌ Loại dữ liệu \"" + dataType + "\" chưa được hỗ trợ.";
                };
            }

            @Override
            protected void process(List<Integer> chunks) {
                view.setProgress(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                view.setProgress(100);
                try {
                    String message = get();
                    boolean success = message.startsWith("✅");
                    view.setStatus(message, success);
                    if (success) {
                        JOptionPane.showMessageDialog(view, message,
                                "Import hoàn thành", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    view.setStatus("❌ Lỗi import: " + ex.getMessage(), false);
                } finally {
                    view.setImportEnabled(true);
                    view.showProgress(false);
                }
            }
        };

        worker.execute();
    }

    // ── Xóa / reset ──────────────────────────────────────────────────────────

    public void onClearClicked() {
        selectedFile = null;
        view.clearAll();
    }

    // ── Tải template ─────────────────────────────────────────────────────────

    public void onDownloadTemplate() {
        String[] columns = DATA_TYPE_COLUMNS.getOrDefault(currentDataType, new String[] { "Cột 1" });

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu template");
        chooser.setSelectedFile(new File("template_" + currentDataType.replace(" ", "_") + ".xlsx"));

        if (chooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
            File saveFile = chooser.getSelectedFile();

            try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

                org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Data");
                org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);

                org.apache.poi.ss.usermodel.CellStyle headerStyle = wb.createCellStyle();
                org.apache.poi.ss.usermodel.Font font = wb.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                headerStyle.setFillForegroundColor(
                        org.apache.poi.ss.usermodel.IndexedColors.LIGHT_YELLOW.getIndex());
                headerStyle.setFillPattern(
                        org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

                for (int i = 0; i < columns.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = header.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerStyle);
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream out = new FileOutputStream(saveFile)) {
                    wb.write(out);
                }

                view.setStatus("✅ Đã lưu template: " + saveFile.getName(), true);
                Desktop.getDesktop().open(saveFile.getParentFile());

            } catch (Exception ex) {
                view.setStatus("❌ Lỗi lưu template: " + ex.getMessage(), false);
            }
        }
    }

    // ── Đổi loại dữ liệu ─────────────────────────────────────────────────────

    public void onDataTypeChanged(String dataType) {
        currentDataType = dataType;
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private static String buildResultMessage(String dataType, int success,
            int skipDup, int skipErr, String logPath) {
        return String.format(
                "✅ Import \"%s\" hoàn thành!%n" +
                        "   • Thành công  : %d bản ghi%n" +
                        "   • Bỏ qua trùng: %d bản ghi%n" +
                        "   • Lỗi / bỏ qua: %d bản ghi%n" +
                        "   • Log chi tiết : %s",
                dataType, success, skipDup, skipErr, logPath);
    }
}