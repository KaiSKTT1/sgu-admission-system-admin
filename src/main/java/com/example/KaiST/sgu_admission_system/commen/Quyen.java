package com.example.KaiST.sgu_admission_system.commen;

import java.util.Locale;

public enum Quyen {
    USER(0, false, "user"),
    ADMIN(1, true, "admin");

    private final int value;
    private final boolean admin;
    private final String code;

    Quyen(int value, boolean admin, String code) {
        this.value = value;
        this.admin = admin;
        this.code = code;
    }

    public int getValue() {
        return value;
    }

    public boolean isAdmin() {
        return admin;
    }

    public String getCode() {
        return code;
    }

    public static Quyen fromRole(String role) {
        if (role == null || role.isBlank()) {
            return USER;
        }
        String normalized = role.trim().toLowerCase(Locale.ROOT);
        return normalized.equals(ADMIN.code) || normalized.equals("1") ? ADMIN : USER;
    }

    public static Quyen fromValue(Integer value) {
        if (value == null) {
            return USER;
        }
        return value == 1 ? ADMIN : USER;
    }
}
