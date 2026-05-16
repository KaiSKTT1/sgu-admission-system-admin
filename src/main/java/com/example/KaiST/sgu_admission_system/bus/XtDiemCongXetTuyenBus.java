package com.example.KaiST.sgu_admission_system.bus;

import com.example.KaiST.sgu_admission_system.dao.XtDiemCongXetTuyenDao;
import com.example.KaiST.sgu_admission_system.entity.XtDiemCongXetTuyen;
import java.util.List;

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

    public XtDiemCongXetTuyen save(XtDiemCongXetTuyen entity) {
        return dao.save(entity);
    }

    public List<XtDiemCongXetTuyen> saveAll(List<XtDiemCongXetTuyen> entities) {
        return dao.saveAll(entities);
    }

    public void deleteById(Integer id) {
        dao.deleteById(id);
    }
}
