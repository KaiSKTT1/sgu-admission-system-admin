package com.example.KaiST.sgu_admission_system.commen;

import java.util.Locale;

public enum PhuongThuc {
    THPT("1", "THPT"),
    DGNL("2", "DGNL"),
    VSAT("3", "VSAT");

    private final String code;
    private final String label;

    PhuongThuc(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static PhuongThuc fromCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        for (PhuongThuc method : values()) {
            if (method.code.equalsIgnoreCase(trimmed) || method.label.equalsIgnoreCase(trimmed)) {
                return method;
            }
        }
        return null;
    }

    public static PhuongThuc fromText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (PhuongThuc method : values()) {
            if (method.label.equalsIgnoreCase(normalized) || method.code.equals(normalized)) {
                return method;
            }
        }
        return null;
    }
}
