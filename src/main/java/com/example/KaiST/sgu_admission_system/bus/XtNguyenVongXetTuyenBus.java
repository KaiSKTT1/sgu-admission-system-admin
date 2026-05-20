package com.example.KaiST.sgu_admission_system.bus;

import com.example.KaiST.sgu_admission_system.dao.XtNguyenVongXetTuyenDao;
import com.example.KaiST.sgu_admission_system.dao.XtThiSinhXetTuyen25Dao;
import com.example.KaiST.sgu_admission_system.dto.NguyenVongXetTuyenRow;
import com.example.KaiST.sgu_admission_system.entity.XtNguyenVongXetTuyen;
import com.example.KaiST.sgu_admission_system.entity.XtThiSinhXetTuyen25;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XtNguyenVongXetTuyenBus {
    private final XtNguyenVongXetTuyenDao dao;
    private final XtThiSinhXetTuyen25Dao thiSinhDao;

    public XtNguyenVongXetTuyenBus() {
        this(new XtNguyenVongXetTuyenDao(), new XtThiSinhXetTuyen25Dao());
    }

    public XtNguyenVongXetTuyenBus(XtNguyenVongXetTuyenDao dao, XtThiSinhXetTuyen25Dao thiSinhDao) {
        this.dao = dao;
        this.thiSinhDao = thiSinhDao;
    }

    public List<XtNguyenVongXetTuyen> findAll() {
        return dao.findAll();
    }

    public List<NguyenVongXetTuyenRow> findAllWithThiSinhInfo() {
        List<XtNguyenVongXetTuyen> nguyenVongs = dao.findAll();
        List<XtThiSinhXetTuyen25> thiSinhs = thiSinhDao.findAll();
        
        Map<String, String> tenByNormCccd = new HashMap<>();
        for (XtThiSinhXetTuyen25 thiSinh : thiSinhs) {
            if (thiSinh.getCccd() != null) {
                tenByNormCccd.put(normalizeCccd(thiSinh.getCccd()), thiSinh.getTen());
            }
        }
        
        List<NguyenVongXetTuyenRow> result = new ArrayList<>();
        for (XtNguyenVongXetTuyen nv : nguyenVongs) {
            String normCccd = nv.getNnCccd() != null ? normalizeCccd(nv.getNnCccd()) : "";
            String tenThiSinh = tenByNormCccd.getOrDefault(normCccd, "");
            
            result.add(new NguyenVongXetTuyenRow(
                    nv.getIdNv(),
                    nv.getNnCccd(),
                    tenThiSinh,
                    nv.getNvMaNganh(),
                    nv.getNvTt(),
                    nv.getTtPhuongThuc(),
                    nv.getTtThm(),
                    nv.getDiemThxt(),
                    nv.getDiemUtqd(),
                    nv.getDiemCong(),
                    nv.getDiemXetTuyen(),
                    nv.getNvKetQua(),
                    nv.getNvKeys()));
        }
        
        return result;
    }

    private String normalizeCccd(String cccd) {
        return cccd != null ? cccd.trim().toUpperCase() : "";
    }
}
