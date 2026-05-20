package com.example.KaiST.sgu_admission_system.bus;

import com.example.KaiST.sgu_admission_system.dao.XtNganhToHopDao;
import com.example.KaiST.sgu_admission_system.entity.XtNganhToHop;
import java.util.List;
import java.util.Optional;

public class XtNganhToHopBus {
    private final XtNganhToHopDao dao;

    public XtNganhToHopBus() {
        this(new XtNganhToHopDao());
    }

    public XtNganhToHopBus(XtNganhToHopDao dao) {
        this.dao = dao;
    }

    public List<XtNganhToHop> findAll() {
        return dao.findAll();
    }

    public void deleteById(Integer id) {
        dao.deleteById(id);
    }

    public void save(XtNganhToHop entity) {
        dao.save(entity);
    }

    public void saveAll(List<XtNganhToHop> entities) {
        dao.saveAll(entities);
    }

    public Optional<XtNganhToHop> findByMaNganhAndMaToHop(String maNganh, String maToHop) {
        return dao.findByMaNganhAndMaToHop(maNganh, maToHop);
    }

    public List<XtNganhToHop> findByMaNganh(String maNganh) {
        return dao.findByMaNganh(maNganh);
    }
}
