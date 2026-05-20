package com.example.KaiST.sgu_admission_system.bus;

import com.example.KaiST.sgu_admission_system.dao.XtDiemThiXetTuyenDao;
import com.example.KaiST.sgu_admission_system.entity.XtDiemThiXetTuyen;
import java.util.List;

public class XtDiemThiXetTuyenBus {
    private final XtDiemThiXetTuyenDao dao;

    public XtDiemThiXetTuyenBus() {
        this(new XtDiemThiXetTuyenDao());
    }

    public XtDiemThiXetTuyenBus(XtDiemThiXetTuyenDao dao) {
        this.dao = dao;
    }

    public List<XtDiemThiXetTuyen> findAll() {
        return dao.findAll();
    }

    public List<XtDiemThiXetTuyen> findByCccdOrSbd(String cccd, String soBaoDanh) {
        return dao.findByCccdOrSbd(cccd, soBaoDanh);
    }

    public List<XtDiemThiXetTuyen> findByCccd(String cccd) {
        return dao.findByCccd(cccd);
    }

    public XtDiemThiXetTuyen save(XtDiemThiXetTuyen entity) {
        return dao.save(entity);
    }

    public List<XtDiemThiXetTuyen> saveAll(List<XtDiemThiXetTuyen> entities) {
        return dao.saveAll(entities);
    }

    public void deleteById(Integer id) {
        dao.deleteById(id);
    }
}
