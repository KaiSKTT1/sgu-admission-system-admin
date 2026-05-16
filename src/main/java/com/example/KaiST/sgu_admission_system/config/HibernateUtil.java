package com.example.KaiST.sgu_admission_system.config;

import com.example.KaiST.sgu_admission_system.entity.XtNganh;
import com.example.KaiST.sgu_admission_system.entity.XtThiSinhXetTuyen25;
import com.example.KaiST.sgu_admission_system.entity.XtDiemThiXetTuyen;
import com.example.KaiST.sgu_admission_system.entity.XtNganhToHop;
import com.example.KaiST.sgu_admission_system.entity.XtToHopMonThi;
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

        String dbUrl = getEnv("DB_URL", "jdbc:mysql://localhost:3306/sgu-admission");
        String dbUsername = getEnv("DB_USERNAME", "hbstudent");
        String dbPassword = getEnv("DB_PASSWORD", "hbstudent");
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

        configuration.addAnnotatedClass(XtThiSinhXetTuyen25.class);
        configuration.addAnnotatedClass(XtNganh.class);
        configuration.addAnnotatedClass(XtToHopMonThi.class);
        configuration.addAnnotatedClass(XtNganhToHop.class);
        configuration.addAnnotatedClass(XtDiemThiXetTuyen.class);

        return configuration.buildSessionFactory();
    }

    private static String getEnv(String key, String fallback) {
        String fileValue = FILE_ENV.get(key);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue;
        }
        return Optional.ofNullable(System.getenv(key))
                .filter(value -> !value.isBlank())
                .orElse(fallback);
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
