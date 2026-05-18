package com.example.KaiST.sgu_admission_system.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class ExcelUtils {
    private ExcelUtils() {
    }

    public static List<Map<String, String>> readRows(File file) throws Exception {
        return readRows(file, List.of());
    }

    public static List<Map<String, String>> readRows(File file, List<String> expectedHeaders) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream inputStream = new FileInputStream(file);
                Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return rows;
            }

            Row headerRow = findHeaderRow(sheet, formatter, expectedHeaders);
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

            int startRow = headerRow.getRowNum() + 1;
            for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
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

    private static Row findHeaderRow(Sheet sheet, DataFormatter formatter, List<String> expectedHeaders) {
        if (sheet == null) {
            return null;
        }
        if (expectedHeaders == null || expectedHeaders.isEmpty()) {
            return sheet.getRow(0);
        }

        List<String> normalizedExpected = new ArrayList<>();
        for (String header : expectedHeaders) {
            String normalized = normalizeHeader(header);
            if (!normalized.isEmpty()) {
                normalizedExpected.add(normalized);
            }
        }

        int maxScan = Math.min(20, sheet.getLastRowNum());
        for (int rowIndex = 0; rowIndex <= maxScan; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            int matches = 0;
            for (int i = 0; i < row.getLastCellNum(); i++) {
                String rawHeader = formatter.formatCellValue(row.getCell(i));
                String normalized = normalizeHeader(rawHeader);
                if (normalizedExpected.contains(normalized)) {
                    matches++;
                }
            }
            if (matches >= 2) {
                return row;
            }
        }
        return sheet.getRow(0);
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

    public static void writeRows(File file, List<String> headers, List<List<Object>> rows) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");
            int rowIndex = 0;

            if (headers != null && !headers.isEmpty()) {
                Row headerRow = sheet.createRow(rowIndex++);
                for (int i = 0; i < headers.size(); i++) {
                    headerRow.createCell(i).setCellValue(headers.get(i));
                }
            }

            if (rows != null) {
                for (List<Object> row : rows) {
                    Row dataRow = sheet.createRow(rowIndex++);
                    for (int i = 0; i < row.size(); i++) {
                        Object value = row.get(i);
                        dataRow.createCell(i).setCellValue(value == null ? "" : value.toString());
                    }
                }
            }

            for (int i = 0; headers != null && i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
        }
    }
}
