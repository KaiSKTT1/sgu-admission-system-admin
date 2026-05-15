package com.example.KaiST.sgu_admission_system.dao;

import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.entity.XtThiSinhXetTuyen25;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class XtThiSinhXetTuyen25Dao {
    public List<XtThiSinhXetTuyen25> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from XtThiSinhXetTuyen25", XtThiSinhXetTuyen25.class).list();
        }
    }

    public XtThiSinhXetTuyen25 save(XtThiSinhXetTuyen25 entity) {
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

    public List<XtThiSinhXetTuyen25> saveAll(List<XtThiSinhXetTuyen25> entities) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            for (XtThiSinhXetTuyen25 entity : entities) {
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
            XtThiSinhXetTuyen25 entity = session.get(XtThiSinhXetTuyen25.class, id);
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
