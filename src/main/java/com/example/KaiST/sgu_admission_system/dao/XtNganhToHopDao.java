package com.example.KaiST.sgu_admission_system.dao;

import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.entity.XtNganhToHop;
import java.util.List;
import java.util.Optional;
import org.hibernate.Session;

public class XtNganhToHopDao {
    public List<XtNganhToHop> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from XtNganhToHop", XtNganhToHop.class).list();
        }
    }

    public void deleteById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var transaction = session.beginTransaction();
            try {
                XtNganhToHop entity = session.get(XtNganhToHop.class, id);
                if (entity != null) {
                    session.remove(entity);
                    transaction.commit();
                }
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public void save(XtNganhToHop entity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var transaction = session.beginTransaction();
            try {
                session.merge(entity);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public void saveAll(List<XtNganhToHop> entities) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var transaction = session.beginTransaction();
            try {
                for (XtNganhToHop entity : entities) {
                    session.persist(entity);
                }
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public Optional<XtNganhToHop> findByMaNganhAndMaToHop(String maNganh, String maToHop) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from XtNganhToHop where maNganh = :maNganh and maToHop = :maToHop",
                    XtNganhToHop.class)
                    .setParameter("maNganh", maNganh)
                    .setParameter("maToHop", maToHop)
                    .uniqueResultOptional();
        }
    }

    public List<XtNganhToHop> findByMaNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from XtNganhToHop where maNganh = :maNganh",
                    XtNganhToHop.class)
                    .setParameter("maNganh", maNganh)
                    .list();
        }
    }
}
