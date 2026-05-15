package com.example.KaiST.sgu_admission_system.dao;

import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.entity.XtNganh;
import java.util.List;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class XtNganhDao {
    public List<XtNganh> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from XtNganh", XtNganh.class).list();
        }
    }

    public Optional<XtNganh> findByMaNganh(String maNganh) {
        if (maNganh == null || maNganh.isBlank()) {
            return Optional.empty();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            XtNganh result = session.createQuery(
                    "from XtNganh where maNganh = :ma",
                    XtNganh.class)
                    .setParameter("ma", maNganh)
                    .uniqueResult();
            return Optional.ofNullable(result);
        }
    }

    public XtNganh save(XtNganh entity) {
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

    public List<XtNganh> saveAll(List<XtNganh> entities) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            for (XtNganh entity : entities) {
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
            XtNganh entity = session.get(XtNganh.class, id);
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
