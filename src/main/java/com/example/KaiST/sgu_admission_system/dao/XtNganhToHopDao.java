package com.example.KaiST.sgu_admission_system.dao;

import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.entity.XtNganhToHop;
import java.util.List;
import org.hibernate.Session;

public class XtNganhToHopDao {
    public List<XtNganhToHop> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from XtNganhToHop", XtNganhToHop.class).list();
        }
    }
}
