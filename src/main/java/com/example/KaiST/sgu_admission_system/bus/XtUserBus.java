package com.example.KaiST.sgu_admission_system.bus;

import com.example.KaiST.sgu_admission_system.dao.XtUserDao;
import com.example.KaiST.sgu_admission_system.entity.XtUser;
import java.util.List;

public class XtUserBus {
    private final XtUserDao dao;

    public XtUserBus() {
        this(new XtUserDao());
    }

    public XtUserBus(XtUserDao dao) {
        this.dao = dao;
    }

    public List<XtUser> findAll() {
        return dao.findAll();
    }

    public XtUser findByCccd(String cccd) {
        return dao.findByCccd(cccd);
    }

    public XtUser save(XtUser entity) {
        return dao.save(entity);
    }

    public List<XtUser> saveAll(List<XtUser> entities) {
        return dao.saveAll(entities);
    }

    public void deleteById(Integer id) {
        dao.deleteById(id);
    }

    public XtUser login(String cccd, String ngaySinh) {
        XtUser user = dao.findByCccd(cccd);
        if (user == null) {
            return null;
        }
        String stored = user.getNgaySinh();
        if (stored == null || ngaySinh == null) {
            return null;
        }
        if (!stored.trim().equalsIgnoreCase(ngaySinh.trim())) {
            return null;
        }
        if (Boolean.FALSE.equals(user.getEnabled())) {
            return null;
        }
        return user;
    }
}
