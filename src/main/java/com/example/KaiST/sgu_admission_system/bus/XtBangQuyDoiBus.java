package com.example.KaiST.sgu_admission_system.bus;

import com.example.KaiST.sgu_admission_system.dao.XtBangQuyDoiDao;
import com.example.KaiST.sgu_admission_system.entity.XtBangQuyDoi;
import java.util.List;

public class XtBangQuyDoiBus {
    private final XtBangQuyDoiDao dao;

    public XtBangQuyDoiBus() {
        this(new XtBangQuyDoiDao());
    }

    public XtBangQuyDoiBus(XtBangQuyDoiDao dao) {
        this.dao = dao;
    }

    public List<XtBangQuyDoi> findAll() {
        return dao.findAll();
    }

    public XtBangQuyDoi save(XtBangQuyDoi entity) {
        return dao.save(entity);
    }

    public List<XtBangQuyDoi> saveAll(List<XtBangQuyDoi> entities) {
        return dao.saveAll(entities);
    }

    public void deleteById(Integer id) {
        dao.deleteById(id);
    }
}
