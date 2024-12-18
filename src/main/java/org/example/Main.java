package org.example;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Конфигурация Hibernate
        Configuration configuration = new Configuration()
                .setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")
                .setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/sqlandhibernate?useSSL=false&allowPublicKeyRetrieval=true")
                .setProperty("hibernate.connection.username", "root")
                .setProperty("hibernate.connection.password", "****") // тут пароль
                .setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect")
                .setProperty("hibernate.show_sql", "true")
                .setProperty("hibernate.hbm2ddl.auto", "update")
                .addAnnotatedClass(Courses.class)
                .addAnnotatedClass(Student.class)
                .addAnnotatedClass(Subscription.class)
                .addAnnotatedClass(Teachers.class)
                .addAnnotatedClass(PurchaseList.class)
                .addAnnotatedClass(LinkedPurchaseList.class);

        // Создание фабрики сессий и выполнение операций с базой данных
        try (SessionFactory sessionFactory = configuration.buildSessionFactory();
             Session session = sessionFactory.openSession()) {

            session.beginTransaction();

            List<PurchaseList> purchaseLists = session.createQuery("from PurchaseList", PurchaseList.class).list();
            for (PurchaseList purchase : purchaseLists) {
                Student student = session.createQuery("from Student where name = :name", Student.class)
                        .setParameter("name", purchase.getStudentName())
                        .uniqueResult();
                Courses course = session.createQuery("from Courses where name = :name", Courses.class)
                        .setParameter("name", purchase.getCourseName())
                        .uniqueResult();

                LinkedPurchaseListKey key = new LinkedPurchaseListKey();
                key.setStudentId(student.getId().intValue());
                key.setCourseId(course.getId().intValue());

                LinkedPurchaseList linkedPurchase = session.get(LinkedPurchaseList.class, key);
                if (linkedPurchase == null) {
                    linkedPurchase = new LinkedPurchaseList();
                    linkedPurchase.setId(key);
                    linkedPurchase.setStudent(student);
                    linkedPurchase.setCourse(course);
                }

                session.merge(linkedPurchase);
            }

            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
