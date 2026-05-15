package com.example.KaiST.sgu_admission_system.bus;

import com.example.KaiST.sgu_admission_system.dao.XtThiSinhXetTuyen25Dao;
import com.example.KaiST.sgu_admission_system.entity.XtThiSinhXetTuyen25;
import java.util.List;

public class XtThiSinhXetTuyen25Bus {
    private final XtThiSinhXetTuyen25Dao dao;

    public XtThiSinhXetTuyen25Bus() {
        this(new XtThiSinhXetTuyen25Dao());
    }

    public XtThiSinhXetTuyen25Bus(XtThiSinhXetTuyen25Dao dao) {
        this.dao = dao;
    }

    public List<XtThiSinhXetTuyen25> findAll() {
        return dao.findAll();
    }

    public XtThiSinhXetTuyen25 save(XtThiSinhXetTuyen25 entity) {
        return dao.save(entity);
    }

    public List<XtThiSinhXetTuyen25> saveAll(List<XtThiSinhXetTuyen25> entities) {
        return dao.saveAll(entities);
    }

    public void deleteById(Integer id) {
        dao.deleteById(id);
    }
}
