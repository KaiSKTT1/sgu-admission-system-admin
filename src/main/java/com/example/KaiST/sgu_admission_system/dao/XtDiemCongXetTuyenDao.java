package com.example.KaiST.sgu_admission_system.dao;

import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.dto.DiemCongXetTuyenRow;
import com.example.KaiST.sgu_admission_system.entity.XtDiemCongXetTuyen;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class XtDiemCongXetTuyenDao {
    public List<XtDiemCongXetTuyen> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from XtDiemCongXetTuyen", XtDiemCongXetTuyen.class).list();
        }
    }

    public List<DiemCongXetTuyenRow> findAllRows() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "select dc.iddiemcong, dc.ts_cccd, dc.manganh, nv.nv_keys, nv.nv_tt, th.tentohop, "
                    + "dc.matohop, dc.diemCC, dc.diemUtxt, dc.diemTong "
                    + "from xt_diemcongxetuyen dc "
                    + "left join xt_nguyenvongxettuyen nv "
                    + "on nv.nn_cccd = dc.ts_cccd "
                    + "and nv.nv_manganh = dc.manganh "
                    + "and nv.tt_phuongthuc = dc.phuongthuc "
                    + "and nv.tt_thm = dc.matohop "
                    + "left join xt_tohop_monthi th on th.matohop = dc.matohop";
            List<Object[]> rows = session.createNativeQuery(sql).list();
            List<DiemCongXetTuyenRow> result = new ArrayList<>();
            for (Object[] row : rows) {
                Integer id = row[0] == null ? null : ((Number) row[0]).intValue();
                String cccd = row[1] == null ? null : row[1].toString();
                String maNganh = row[2] == null ? null : row[2].toString();
                String nvKeys = row[3] == null ? null : row[3].toString();
                Integer nvTt = row[4] == null ? null : ((Number) row[4]).intValue();
                String tenToHop = row[5] == null ? null : row[5].toString();
                String maToHop = row[6] == null ? null : row[6].toString();
                BigDecimal diemCc = toBigDecimal(row[7]);
                BigDecimal diemUtxt = toBigDecimal(row[8]);
                BigDecimal diemTong = toBigDecimal(row[9]);
                result.add(new DiemCongXetTuyenRow(id, cccd, maNganh, nvKeys, nvTt, tenToHop, maToHop,
                        diemCc, diemUtxt, diemTong));
            }
            return result;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public XtDiemCongXetTuyen findById(Integer id) {
        if (id == null) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(XtDiemCongXetTuyen.class, id);
        }
    }

    public XtDiemCongXetTuyen save(XtDiemCongXetTuyen entity) {
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

    public List<XtDiemCongXetTuyen> saveAll(List<XtDiemCongXetTuyen> entities) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            for (XtDiemCongXetTuyen entity : entities) {
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
            XtDiemCongXetTuyen entity = session.get(XtDiemCongXetTuyen.class, id);
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
