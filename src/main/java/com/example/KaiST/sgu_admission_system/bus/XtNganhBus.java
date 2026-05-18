package com.example.KaiST.sgu_admission_system.bus;

import com.example.KaiST.sgu_admission_system.dao.XtNganhDao;
import com.example.KaiST.sgu_admission_system.entity.XtNganh;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class XtNganhBus {
    private final XtNganhDao dao;

    public XtNganhBus() {
        this(new XtNganhDao());
    }

    public XtNganhBus(XtNganhDao dao) {
        this.dao = dao;
    }

    public List<XtNganh> findAll() {
        return dao.findAll();
    }

    public Optional<XtNganh> findByMaNganh(String maNganh) {
        return dao.findByMaNganh(maNganh);
    }

    public XtNganh save(XtNganh entity) {
        return dao.save(entity);
    }

    public Map<String, Long> countNguyenVongByMaNganh() {
        return dao.countNguyenVongByMaNganh();
    }

    public List<XtNganh> saveAll(List<XtNganh> entities) {
        return dao.saveAll(entities);
    }

    public void deleteById(Integer id) {
        dao.deleteById(id);
    }
}
