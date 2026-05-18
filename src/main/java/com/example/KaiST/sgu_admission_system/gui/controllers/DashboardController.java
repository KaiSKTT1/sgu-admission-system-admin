package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XtThiSinhXetTuyen25Bus;
import com.example.KaiST.sgu_admission_system.entity.XtThiSinhXetTuyen25;
import com.example.KaiST.sgu_admission_system.gui.views.DashboardView;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;

public class DashboardController {
    private final DashboardView view;
    private final XtThiSinhXetTuyen25Bus bus;

    public DashboardController(DashboardView view, XtThiSinhXetTuyen25Bus bus) {
        this.view = view;
        this.bus = bus;
    }

    public void init() {
        onRefresh();
    }

    public void onRefresh() {
        List<XtThiSinhXetTuyen25> all = bus.findAll();
        int total = all.size();
        int approved = countApproved(all);
        int pending = Math.max(0, total - approved);
        Map<String, Integer> regionCounts = countByKhuVuc(all);
        String regionSummary = summarizeTopRegions(regionCounts);

        view.setOverview(total, approved, pending, regionSummary, regionCounts);
    }

    private Map<String, Integer> countByKhuVuc(List<XtThiSinhXetTuyen25> all) {
        Map<String, Integer> counts = new TreeMap<>();
        for (XtThiSinhXetTuyen25 candidate : all) {
            String key = normalizeKey(candidate.getKhuVuc());
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }
        return counts;
    }

    private String normalizeKey(String value) {
        if (value == null || value.isBlank()) {
            return "Khác";
        }
        return value.trim();
    }

    private int countApproved(List<XtThiSinhXetTuyen25> all) {
        int count = 0;
        for (XtThiSinhXetTuyen25 candidate : all) {
            if (candidate.getSoBaoDanh() != null && !candidate.getSoBaoDanh().isBlank()) {
                count++;
            }
        }
        return count;
    }

    private String summarizeTopRegions(Map<String, Integer> counts) {
        if (counts.isEmpty()) {
            return "Chưa có";
        }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        StringBuilder builder = new StringBuilder();
        int limit = Math.min(3, entries.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(entry.getKey()).append(": ").append(entry.getValue());
        }
        if (entries.size() > limit) {
            int remaining = 0;
            for (int i = limit; i < entries.size(); i++) {
                remaining += entries.get(i).getValue();
            }
            builder.append("\nKhác: ").append(remaining);
        }
        return builder.toString();
    }
}
