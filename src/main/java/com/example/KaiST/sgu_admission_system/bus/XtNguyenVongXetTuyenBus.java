package com.example.KaiST.sgu_admission_system.bus;

import com.example.KaiST.sgu_admission_system.dao.XtNguyenVongXetTuyenDao;
import com.example.KaiST.sgu_admission_system.entity.XtNguyenVongXetTuyen;
import java.util.List;

public class XtNguyenVongXetTuyenBus {
    private final XtNguyenVongXetTuyenDao dao;

    public XtNguyenVongXetTuyenBus() {
        this(new XtNguyenVongXetTuyenDao());
    }

    public XtNguyenVongXetTuyenBus(XtNguyenVongXetTuyenDao dao) {
        this.dao = dao;
    }

    public List<XtNguyenVongXetTuyen> findAll() {
        return dao.findAll();
    }
}
