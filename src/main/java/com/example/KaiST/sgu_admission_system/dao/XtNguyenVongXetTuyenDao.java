package com.example.KaiST.sgu_admission_system.dao;

import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.entity.XtNguyenVongXetTuyen;
import java.util.List;
import org.hibernate.Session;

public class XtNguyenVongXetTuyenDao {
    public List<XtNguyenVongXetTuyen> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from XtNguyenVongXetTuyen", XtNguyenVongXetTuyen.class).list();
        }
    }
}
