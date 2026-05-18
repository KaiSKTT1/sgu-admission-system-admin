package com.example.KaiST.sgu_admission_system.dao;

import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.entity.XtUser;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class XtUserDao {
    public List<XtUser> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from XtUser", XtUser.class).list();
        }
    }

    public XtUser findByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from XtUser where cccd = :cccd", XtUser.class)
                    .setParameter("cccd", cccd)
                    .uniqueResult();
        }
    }

    public XtUser save(XtUser entity) {
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

    public List<XtUser> saveAll(List<XtUser> entities) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            for (XtUser entity : entities) {
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
            XtUser entity = session.get(XtUser.class, id);
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
