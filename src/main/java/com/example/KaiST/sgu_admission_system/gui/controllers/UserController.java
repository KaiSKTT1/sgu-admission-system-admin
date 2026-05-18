package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XtUserBus;
import com.example.KaiST.sgu_admission_system.entity.XtUser;
import com.example.KaiST.sgu_admission_system.gui.dialogs.UserDialog;
import com.example.KaiST.sgu_admission_system.gui.views.UserView;
import com.example.KaiST.sgu_admission_system.utils.ExcelUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserController {
    private final UserView view;
    private final XtUserBus bus;
    private List<XtUser> allUsers = new ArrayList<>();
    private List<XtUser> filteredUsers = new ArrayList<>();
    private int currentPage = 1;

    public UserController(UserView view, XtUserBus bus) {
        this.view = view;
        this.bus = bus;
    }

    public void init() {
        onRefresh();
    }

    public void onRefresh() {
        allUsers = bus.findAll();
        onSearch();
    }

    public void onSearch() {
        String keyword = view.getSearchKeyword().trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            filteredUsers = new ArrayList<>(allUsers);
        } else {
            filteredUsers = new ArrayList<>();
            for (XtUser user : allUsers) {
                if (containsKeyword(user, keyword)) {
                    filteredUsers.add(user);
                }
            }
        }
        currentPage = 1;
        updateTable();
    }

    public void onPageChange(int page) {
        currentPage = page;
        updateTable();
    }

    public void onAdd() {
        UserDialog dialog = new UserDialog(
                view.getWindow(),
                "Thêm người dùng",
                null,
                UserDialog.Mode.ADD);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getUser());
            onRefresh();
        }
    }

    public void onViewRow(int row) {
        XtUser user = getUserAt(row);
        if (user == null) {
            view.showInfo("Vui lòng chọn người dùng cần xem.");
            return;
        }
        UserDialog dialog = new UserDialog(
                view.getWindow(),
                "Xem chi tiết",
                user,
                UserDialog.Mode.VIEW);
        dialog.setVisible(true);
    }

    public void onEditRow(int row) {
        XtUser user = getUserAt(row);
        if (user == null) {
            view.showInfo("Vui lòng chọn người dùng cần sửa.");
            return;
        }
        UserDialog dialog = new UserDialog(
                view.getWindow(),
                "Sửa người dùng",
                user,
                UserDialog.Mode.EDIT);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getUser());
            onRefresh();
        }
    }

    public void onDeleteRow(int row) {
        XtUser user = getUserAt(row);
        if (user == null) {
            view.showInfo("Vui lòng chọn người dùng cần xóa.");
            return;
        }
        if (view.confirm("Bạn có chắc chắn muốn xóa người dùng này?")) {
            bus.deleteById(user.getIdUser());
            onRefresh();
        }
    }

    public void onToggleRow(int row) {
        XtUser user = getUserAt(row);
        if (user == null) {
            view.showInfo("Vui lòng chọn người dùng cần đổi trạng thái.");
            return;
        }
        boolean enabled = user.getEnabled() == null || user.getEnabled();
        user.setEnabled(!enabled);
        bus.save(user);
        onRefresh();
    }

    public void onImport() {
        File file = view.chooseExcelFile();
        if (file == null) {
            return;
        }

        List<Map<String, String>> rows;
        try {
            rows = ExcelUtils.readRows(file, List.of(
                    "cccd",
                    "hoten",
                    "ten",
                    "ngaysinh",
                    "email",
                    "dienthoai",
                    "role",
                    "quyen",
                    "enabled",
                    "trangthai",
                    "password"));
        } catch (Exception ex) {
            view.showError("Không thể đọc file Excel: " + ex.getMessage());
            return;
        }

        List<XtUser> imported = new ArrayList<>();
        for (Map<String, String> row : rows) {
            XtUser user = new XtUser();
            boolean hasData = false;

            hasData |= applyValue(row, user::setCccd, "cccd");
            hasData |= applyValue(row, user::setHoTen, "hoten", "ten");
            hasData |= applyValue(row, user::setNgaySinh, "ngaysinh");
            hasData |= applyValue(row, user::setEmail, "email");
            hasData |= applyValue(row, user::setDienThoai, "dienthoai", "sodienthoai");
            hasData |= applyValue(row, user::setRole, "role", "quyen");
            hasData |= applyEnabled(row, user::setEnabled, "enabled", "trangthai");
            hasData |= applyValue(row, user::setPassword, "password", "matkhau");

            if (hasData) {
                normalizeUser(user);
                imported.add(user);
            }
        }

        if (imported.isEmpty()) {
            view.showInfo("Không có dữ liệu hợp lệ để import.");
            return;
        }

        bus.saveAll(imported);
        onRefresh();
        view.showInfo("Đã import " + imported.size() + " người dùng.");
    }

    private void updateTable() {
        int pageSize = view.getPageSize();
        int total = filteredUsers.size();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
        currentPage = Math.min(Math.max(1, currentPage), totalPages);
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        List<Object[]> rows = new ArrayList<>();
        for (int i = start; i < end; i++) {
            XtUser user = filteredUsers.get(i);
            int stt = i + 1;
            rows.add(new Object[] {
                    stt,
                    safeText(user.getCccd()),
                    safeText(user.getHoTen()),
                    safeText(user.getNgaySinh()),
                    safeText(user.getRole()),
                    user.getEnabled() != null && user.getEnabled() ? "Đang hoạt động" : "Đã khóa",
                    ""
            });
        }

        view.setTableRows(rows);
        view.updatePagination(currentPage, totalPages);
    }

    private XtUser getUserAt(int row) {
        if (row < 0) {
            return null;
        }
        int pageSize = view.getPageSize();
        int globalIndex = (currentPage - 1) * pageSize + row;
        if (globalIndex < 0 || globalIndex >= filteredUsers.size()) {
            return null;
        }
        return filteredUsers.get(globalIndex);
    }

    private boolean containsKeyword(XtUser user, String keyword) {
        return containsIgnoreCase(user.getCccd(), keyword)
                || containsIgnoreCase(user.getHoTen(), keyword)
                || containsIgnoreCase(user.getEmail(), keyword)
                || containsIgnoreCase(user.getRole(), keyword);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String getValue(Map<String, String> row, String... headers) {
        for (String header : headers) {
            String normalized = ExcelUtils.normalizeHeader(header);
            String value = row.get(normalized);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean applyValue(Map<String, String> row, java.util.function.Consumer<String> setter,
            String... headers) {
        String value = getValue(row, headers);
        if (value == null || value.isEmpty()) {
            return false;
        }
        setter.accept(value);
        return true;
    }

    private boolean applyEnabled(Map<String, String> row, java.util.function.Consumer<Boolean> setter,
            String... headers) {
        String value = getValue(row, headers);
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        boolean enabled = normalized.equals("1")
                || normalized.equals("true")
                || normalized.equals("yes")
                || normalized.equals("enable")
                || normalized.equals("active")
                || normalized.equals("danghoatdong")
                || normalized.equals("hoatdong");
        setter.accept(enabled);
        return true;
    }

    private void normalizeUser(XtUser user) {
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("user");
        }
        String role = user.getRole().trim().toLowerCase(Locale.ROOT);
        user.setRole(role);
        if (user.getEnabled() == null) {
            user.setEnabled(Boolean.TRUE);
        }
    }

    private String safeText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
