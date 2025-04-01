package com.library.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.logging.Logger;
import java.util.logging.Level;

public class HibernateUtil {
    private static final Logger LOGGER = Logger.getLogger(HibernateUtil.class.getName());
    private static SessionFactory sessionFactory;

    private static SessionFactory buildSessionFactory() {
        try {
            LOGGER.info("Initializing Hibernate SessionFactory...");
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");

            // Add additional configuration if needed
            configuration.setProperty("hibernate.connection.provider_disables_autocommit", "true");

            sessionFactory = configuration.buildSessionFactory();
            LOGGER.info("Hibernate SessionFactory initialized successfully");
            return sessionFactory;
        } catch (Throwable ex) {
            LOGGER.log(Level.SEVERE, "Initial SessionFactory creation failed", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    public static void cleanupReservationsTable() {
        try (Session session = getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                // Drop the foreign key constraint
                session.createNativeQuery("ALTER TABLE reservations DROP FOREIGN KEY FKrsdd3ib3landfpmgoolccjakt")
                        .executeUpdate();

                // Drop the reservations table
                session.createNativeQuery("DROP TABLE IF EXISTS reservations")
                        .executeUpdate();

                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                LOGGER.log(Level.SEVERE, "Error cleaning up reservations table", e);
                throw e;
            }
        }
    }
}