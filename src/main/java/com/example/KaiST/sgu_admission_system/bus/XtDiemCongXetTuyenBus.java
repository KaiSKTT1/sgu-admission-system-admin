package com.example.KaiST.sgu_admission_system.bus;

import com.example.KaiST.sgu_admission_system.dao.XtDiemCongXetTuyenDao;
import com.example.KaiST.sgu_admission_system.dto.DiemCongXetTuyenRow;
import com.example.KaiST.sgu_admission_system.entity.XtDiemCongXetTuyen;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class XtDiemCongXetTuyenBus {
    private final XtDiemCongXetTuyenDao dao;

    public XtDiemCongXetTuyenBus() {
        this(new XtDiemCongXetTuyenDao());
    }

    public XtDiemCongXetTuyenBus(XtDiemCongXetTuyenDao dao) {
        this.dao = dao;
    }

    public List<XtDiemCongXetTuyen> findAll() {
        return dao.findAll();
    }

    public List<DiemCongXetTuyenRow> findAllRows() {
        return dao.findAllRows();
    }

    public XtDiemCongXetTuyen findById(Integer id) {
        return dao.findById(id);
    }

    public XtDiemCongXetTuyen save(XtDiemCongXetTuyen entity) {
        return dao.save(entity);
    }

    public List<XtDiemCongXetTuyen> saveAll(List<XtDiemCongXetTuyen> entities) {
        return dao.saveAll(entities);
    }

    public void deleteById(Integer id) {
        dao.deleteById(id);
    }

    /**
     * Map tổng điểm cộng theo CCCD + mã tổ hợp (cùng nguồn dữ liệu panel Điểm cộng).
     * Khóa: {@code normalizeCccd(cccd)|normalizeToHopCode(maToHop)}.
     */
    public Map<String, BigDecimal> buildDiemTongByCccdAndToHop() {
        Map<String, BigDecimal> map = new HashMap<>();
        for (DiemCongXetTuyenRow row : findAllRows()) {
            if (row.getTsCccd() == null || row.getMaToHop() == null || row.getDiemTong() == null) {
                continue;
            }
            map.put(buildLookupKey(row.getTsCccd(), row.getMaToHop()), row.getDiemTong());
        }
        return map;
    }

    public static String buildLookupKey(String cccd, String maToHopOrThm) {
        return normalizeCccd(cccd) + "|" + normalizeToHopCode(maToHopOrThm);
    }

    public static String normalizeToHopCode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim().toUpperCase(Locale.ROOT);
        if (trimmed.startsWith("DGNL_") || trimmed.startsWith("VSAT_")) {
            String[] parts = trimmed.split("_");
            return parts.length > 1 ? parts[1] : trimmed;
        }
        return trimmed;
    }

    private static String normalizeCccd(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
