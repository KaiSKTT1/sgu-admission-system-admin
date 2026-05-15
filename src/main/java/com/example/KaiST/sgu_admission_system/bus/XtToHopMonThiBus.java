package com.example.KaiST.sgu_admission_system.bus;

import com.example.KaiST.sgu_admission_system.dao.XtToHopMonThiDao;
import com.example.KaiST.sgu_admission_system.entity.XtToHopMonThi;
import java.util.List;
import java.util.Optional;

public class XtToHopMonThiBus {
    private final XtToHopMonThiDao dao;

    public XtToHopMonThiBus() {
        this(new XtToHopMonThiDao());
    }

    public XtToHopMonThiBus(XtToHopMonThiDao dao) {
        this.dao = dao;
    }

    public List<XtToHopMonThi> findAll() {
        return dao.findAll();
    }

    public Optional<XtToHopMonThi> findByMaToHop(String maToHop) {
        return dao.findByMaToHop(maToHop);
    }

    public XtToHopMonThi save(XtToHopMonThi entity) {
        return dao.save(entity);
    }

    public List<XtToHopMonThi> saveAll(List<XtToHopMonThi> entities) {
        return dao.saveAll(entities);
    }

    public void deleteById(Integer id) {
        dao.deleteById(id);
    }
}
