package com.example.KaiST.sgu_admission_system.dao;

import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.entity.XtDiemThiXetTuyen;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class XtDiemThiXetTuyenDao {
    public List<XtDiemThiXetTuyen> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from XtDiemThiXetTuyen", XtDiemThiXetTuyen.class).list();
        }
    }

    public List<XtDiemThiXetTuyen> findByCccdOrSbd(String cccd, String soBaoDanh) {
        boolean hasCccd = cccd != null && !cccd.isBlank();
        boolean hasSbd = soBaoDanh != null && !soBaoDanh.isBlank();
        if (!hasCccd && !hasSbd) {
            return java.util.Collections.emptyList();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (hasCccd && hasSbd) {
                return session.createQuery(
                        "from XtDiemThiXetTuyen where cccd = :cccd or soBaoDanh = :sbd",
                        XtDiemThiXetTuyen.class)
                        .setParameter("cccd", cccd)
                        .setParameter("sbd", soBaoDanh)
                        .list();
            }
            if (hasCccd) {
                return session.createQuery(
                        "from XtDiemThiXetTuyen where cccd = :cccd",
                        XtDiemThiXetTuyen.class)
                        .setParameter("cccd", cccd)
                        .list();
            }
            return session.createQuery(
                    "from XtDiemThiXetTuyen where soBaoDanh = :sbd",
                    XtDiemThiXetTuyen.class)
                    .setParameter("sbd", soBaoDanh)
                    .list();
        }
    }

    public XtDiemThiXetTuyen save(XtDiemThiXetTuyen entity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(entity);
            transaction.commit();
            return entity;
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
        }
    }

    public List<XtDiemThiXetTuyen> saveAll(List<XtDiemThiXetTuyen> entities) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            for (XtDiemThiXetTuyen entity : entities) {
                session.saveOrUpdate(entity);
            }
            transaction.commit();
            return entities;
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
        }
    }

    public void deleteById(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            XtDiemThiXetTuyen entity = session.get(XtDiemThiXetTuyen.class, id);
            if (entity != null) {
                session.delete(entity);
            }
            transaction.commit();
        } catch (Exception ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw ex;
        }
    }
}
