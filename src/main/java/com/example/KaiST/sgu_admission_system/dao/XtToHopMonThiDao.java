package com.example.KaiST.sgu_admission_system.dao;

import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.entity.XtToHopMonThi;
import java.util.List;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class XtToHopMonThiDao {
    public List<XtToHopMonThi> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from XtToHopMonThi", XtToHopMonThi.class).list();
        }
    }

    public Optional<XtToHopMonThi> findByMaToHop(String maToHop) {
        if (maToHop == null || maToHop.isBlank()) {
            return Optional.empty();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            XtToHopMonThi result = session.createQuery(
                    "from XtToHopMonThi where maToHop = :ma",
                    XtToHopMonThi.class)
                    .setParameter("ma", maToHop)
                    .uniqueResult();
            return Optional.ofNullable(result);
        }
    }

    public XtToHopMonThi save(XtToHopMonThi entity) {
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

    public List<XtToHopMonThi> saveAll(List<XtToHopMonThi> entities) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            for (XtToHopMonThi entity : entities) {
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
            XtToHopMonThi entity = session.get(XtToHopMonThi.class, id);
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
