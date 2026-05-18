package com.example.KaiST.sgu_admission_system.bus;

import com.example.KaiST.sgu_admission_system.dao.XtNganhToHopDao;
import com.example.KaiST.sgu_admission_system.entity.XtNganhToHop;
import java.util.List;

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
}
