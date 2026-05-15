package com.example.KaiST.sgu_admission_system.utils;

import java.io.File;
import java.io.FileInputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public final class ExcelUtils {
    private ExcelUtils() {
    }

    public static List<Map<String, String>> readRows(File file) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream inputStream = new FileInputStream(file);
                Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return rows;
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return rows;
            }

            Map<Integer, String> indexToHeader = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                String rawHeader = formatter.formatCellValue(headerRow.getCell(i));
                String normalized = normalizeHeader(rawHeader);
                if (!normalized.isEmpty()) {
                    indexToHeader.put(i, normalized);
                }
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                Map<String, String> rowValues = new HashMap<>();
                boolean hasData = false;

                for (Map.Entry<Integer, String> entry : indexToHeader.entrySet()) {
                    String value = formatter.formatCellValue(row.getCell(entry.getKey()));
                    if (value != null) {
                        String trimmed = value.trim();
                        if (!trimmed.isEmpty()) {
                            rowValues.put(entry.getValue(), trimmed);
                            hasData = true;
                        }
                    }
                }

                if (hasData) {
                    rows.add(rowValues);
                }
            }
        }

        return rows;
    }

    public static String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }
        String normalized = Normalizer.normalize(header, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        return normalized.replaceAll("[^a-z0-9]", "");
    }
}
