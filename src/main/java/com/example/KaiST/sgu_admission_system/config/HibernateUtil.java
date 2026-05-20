package com.example.KaiST.sgu_admission_system.config;

import com.example.KaiST.sgu_admission_system.entity.XtNganh;
import com.example.KaiST.sgu_admission_system.entity.XtThiSinhXetTuyen25;
import com.example.KaiST.sgu_admission_system.entity.XtDiemThiXetTuyen;
import com.example.KaiST.sgu_admission_system.entity.XtDiemCongXetTuyen;
import com.example.KaiST.sgu_admission_system.entity.XtNguyenVongXetTuyen;
import com.example.KaiST.sgu_admission_system.entity.XtBangQuyDoi;
import com.example.KaiST.sgu_admission_system.entity.XtNganhToHop;
import com.example.KaiST.sgu_admission_system.entity.XtToHopMonThi;
import com.example.KaiST.sgu_admission_system.entity.XtUser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {
    private static final Map<String, String> FILE_ENV = loadEnvFile();
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil() {
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        SESSION_FACTORY.close();
    }

    private static SessionFactory buildSessionFactory() {
        Configuration configuration = new Configuration();

        String dbUrl = getEnv("DB_URL", "jdbc:mysql://localhost:3306/sgu-test");
        String dbUsername = getEnv("DB_USERNAME", "root");
        String dbPassword = getEnv("DB_PASSWORD", "");
        String dbDriver = getEnv("DB_DRIVER", "com.mysql.cj.jdbc.Driver");
        String dbDialect = getEnv("DB_DIALECT", "org.hibernate.dialect.MySQLDialect");
        String dbDdlAuto = getEnv("DB_DDL_AUTO", "update");
        String dbShowSql = getEnv("DB_SHOW_SQL", "false");

        configuration.setProperty("hibernate.connection.url", dbUrl);
        configuration.setProperty("hibernate.connection.username", dbUsername);
        configuration.setProperty("hibernate.connection.password", dbPassword);
        configuration.setProperty("hibernate.connection.driver_class", dbDriver);
        configuration.setProperty("hibernate.dialect", dbDialect);
        configuration.setProperty("hibernate.hbm2ddl.auto", dbDdlAuto);
        configuration.setProperty("hibernate.show_sql", dbShowSql);
        configuration.setProperty("hibernate.current_session_context_class", "thread");

        configuration.setProperty("hibernate.jdbc.batch_size", "50");
        configuration.setProperty("hibernate.order_inserts", "true");
        configuration.setProperty("hibernate.order_updates", "true");
        configuration.setProperty("hibernate.jdbc.batch_versioned_data", "true");

        configuration.addAnnotatedClass(XtThiSinhXetTuyen25.class);
        configuration.addAnnotatedClass(XtNganh.class);
        configuration.addAnnotatedClass(XtToHopMonThi.class);
        configuration.addAnnotatedClass(XtNganhToHop.class);
        configuration.addAnnotatedClass(XtDiemThiXetTuyen.class);
        configuration.addAnnotatedClass(XtDiemCongXetTuyen.class);
        configuration.addAnnotatedClass(XtNguyenVongXetTuyen.class);
        configuration.addAnnotatedClass(XtBangQuyDoi.class);
        configuration.addAnnotatedClass(XtUser.class);

        return configuration.buildSessionFactory();
    }

    private static String getEnv(String key, String fallback) {
        // Nếu file .env CÓ CHỨA key (kể cả gán bằng rỗng như DB_PASSWORD=) thì lấy luôn
        // giá trị đó
        if (FILE_ENV.containsKey(key)) {
            return FILE_ENV.get(key);
        }

        // Nếu file .env không có key đó mới tìm đến hệ thống hoặc fallback
        String envValue = System.getenv(key);
        if (envValue != null) {
            return envValue;
        }

        return fallback;
    }

    private static Map<String, String> loadEnvFile() {
        Map<String, String> values = new HashMap<>();
        Path envPath = Paths.get(System.getProperty("user.dir"), ".env");
        if (!Files.exists(envPath)) {
            return values;
        }

        try {
            List<String> lines = Files.readAllLines(envPath);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int equalsIndex = trimmed.indexOf('=');
                if (equalsIndex <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, equalsIndex).trim();
                String value = trimmed.substring(equalsIndex + 1).trim();
                values.put(key, value);
            }
        } catch (Exception ex) {
            return values;
        }

        return values;
    }
}
